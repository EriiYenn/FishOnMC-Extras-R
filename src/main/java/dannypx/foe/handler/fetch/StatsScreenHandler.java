package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.handler.store.StatsDataHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class StatsScreenHandler extends Handler {
    private static StatsScreenHandler INSTANCE = new StatsScreenHandler();

    public static StatsScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new StatsScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private boolean importStats = false;
    private boolean isScanned = false;
    private List<Component> statsLore = new ArrayList<>();

    public void setImportStats(boolean importStats) {
        this.importStats = importStats;
    }

    public List<Component> getStatsLore() {
        return statsLore;
    }
    //endregion

    //region Methods
    public void init(ContainerScreen screen) {
        isScanned = false;

        ScreenEvents.afterTick(screen).register(screen1 -> {
            this.tick(screen);
        });
    }

    public void tick(ContainerScreen screen) {
        if(!isScanned
                && screen.getMenu().slots.get(12).getItem().is(ItemTags.SHULKER_BOXES)
        ) {
            isScanned = true;
            this.checkStats(screen.getMenu());
        }
    }

    public void checkStats(ChestMenu chestMenu) {
        if(this.importStats) {
            CodeExecuterHandler.runLater(2, () -> {
                Slot statSlot = chestMenu.getSlot(23);
                Pair<Boolean, Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>>> completed = this.extractData(statSlot.getItem());

                if(completed.value1()) {
                    ProfileDataHandler.instance().updateImportStats(true);
                    StatsDataHandler.instance().updateImportStats(true, completed.value2());
                    NotifierHandler.instance().notifyImportStatsCompleted();
                }
            });

            this.importStats = false;
        }
    }

    private Pair<Boolean, Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>>> extractData(ItemStack stack) {
        if(stack.get(DataComponents.LORE) != null) {
            List<Component> loreLines = stack.get(DataComponents.LORE).lines();
            this.statsLore = loreLines;
            if(loreLines.size() > 7) {
                Map<String, Map<String, StatsDataHandler.Stat<Integer, Integer>>> newData = StatsDataHandler.instance().getStatsData().fishData;

                int totalFish = this.extractTotal(loreLines.get(5));
                StatsDataHandler.instance().getStatsData().fishTotal = totalFish;

                // Rarity
                for (int i = 7; i < 12; i++) {
                    Component line = loreLines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishTagObject.RARITY, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishTagObject.RARITY, new HashMap<>());

                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishTagObject.RARITY, newCategoryData);
                    }
                }

                // Fish Size
                for (int i = 13; i < 18; i++) {
                    Component line = loreLines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishTagObject.FISH_SIZE, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishTagObject.FISH_SIZE, new HashMap<>());

                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishTagObject.FISH_SIZE, newCategoryData);
                    }
                }

                // Variant
                AtomicInteger normalCount = new AtomicInteger(totalFish);
                for (int i = 19; i < 23; i++) {
                    Component line = loreLines.get(i);
                    Triplet<Boolean, String, Integer> data =
                            this.extractStat(ConstantDataHandler.instance().getConstantData().fishData.getOrDefault(FishTagObject.VARIANT, new HashMap<>()), line);

                    if(data.value1()) {
                        Map<String, StatsDataHandler.Stat<Integer, Integer>> newCategoryData = newData
                                .getOrDefault(FishTagObject.VARIANT, new HashMap<>());

                        normalCount.set(normalCount.get() - data.value3());
                        newCategoryData.put(data.value2(), new StatsDataHandler.Stat<>(data.value3(), totalFish));

                        newData.put(FishTagObject.VARIANT, newCategoryData);
                    }
                }

                newData.getOrDefault(FishTagObject.VARIANT, new HashMap<>())
                        .put("normal", new StatsDataHandler.Stat<>(normalCount.get(), totalFish));

                return Pair.of(true, newData);
            }
        }
        return Pair.of(false, new HashMap<>());
    }

    private Triplet<Boolean, String, Integer> extractStat(Map<String, Component> constants, Component line) {
        if(line.getSiblings().size() > 2) {
            String field = line.getSiblings().get(1).getString().trim();
            String key = ConstantDataHandler.keysFromField(constants, field).findFirst().orElse(null);
            if(key != null) {
                int amount = ComponentHelper.toIntFromString(line.getSiblings().get(2).getString());

                return Triplet.of(key, amount);
            }
        }
        return Triplet.ofFalse("", 0);
    }

    private int extractTotal(Component component) {
        return ComponentHelper.toIntFromString(component.getSiblings().get(2).getString());
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "statsLore", Pair.of(Component.literal("[statsLore]"), ComponentHelper.literal(getStatsLore()))
        );
    }
    //endregion
}
