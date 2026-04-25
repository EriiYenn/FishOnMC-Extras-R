package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.renderer.AuctionHouseScreenRenderHandler;
import dannypx.foe.handler.renderer.ChestScreenRenderHandler;
import dannypx.foe.handler.renderer.PersonalVaultScreenRenderHandler;
import dannypx.foe.handler.renderer.PresetsScreenRenderHandler;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class GenericContainerScreenHandler extends Handler {
    private static GenericContainerScreenHandler INSTANCE = new GenericContainerScreenHandler();

    public static GenericContainerScreenHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new GenericContainerScreenHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private String lastContainerScreen = "";

    public String getLastContainerScreen() {
        return lastContainerScreen;
    }

    public static final String QUEST_SCREEN_CONTAINER = "\uEEE4\uD539";
    public static final String STATS_SCREEN_CONTAINER = "\uEEE4\uD532";
    public static final String AUCTION_HOUSE_SCREEN_CONTAINER = "\uEEE4\uD543";
    public static final String PERSONAL_VAULT_SCREEN_CONTAINER = "Personal Vault #";
    public static final String ARMOR_MENU_SCREEN_CONTAINER = "Armor Menu";
    public static final String STORAGE_SCREEN_CONTAINER = " ";
    public static final String GENERIC_SCREEN_CONTAINER = "\uEEE4\uD552";
    public static final String PRESETS_SCREEN_CONTAINER = "Presets\uEEE6\uEEE5\uD572";
    //endregion

    //region Methods
    public void init(ContainerScreen genericContainerScreen) {
        if(!Configs.handlerConfig.genericContainerScreenHandler.get()) {
            return;
        }

        this.checkIsOfTitle(genericContainerScreen);
    }

    private void checkIsOfTitle(ContainerScreen genericContainerScreen) {
        this.lastContainerScreen = genericContainerScreen.getTitle().getString();

        if (Objects.equals(genericContainerScreen.getTitle().getString(), QUEST_SCREEN_CONTAINER)) {
            QuestScreenHandler.instance().init(genericContainerScreen);
//            QuestScreenHandler.instance().checkQuests(genericContainerScreen.getMenu());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STATS_SCREEN_CONTAINER)) {
            StatsScreenHandler.instance().init(genericContainerScreen);
//            StatsScreenHandler.instance().checkStats(genericContainerScreen.getMenu());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), AUCTION_HOUSE_SCREEN_CONTAINER)) {
            AuctionHouseScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (genericContainerScreen.getTitle().getString().startsWith(PERSONAL_VAULT_SCREEN_CONTAINER)) {
            PersonalVaultScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STORAGE_SCREEN_CONTAINER)) {
            ChestScreenRenderHandler.instance().init(genericContainerScreen);
        } else if (genericContainerScreen.getTitle().getString().startsWith(ARMOR_MENU_SCREEN_CONTAINER)) {
            ArmorRollScreenHandler.instance().checkArmorRolls(genericContainerScreen.getMenu());
        } else if (Objects.equals(genericContainerScreen.getTitle().getString(), PRESETS_SCREEN_CONTAINER)) {
            PresetsScreenRenderHandler.instance().init(genericContainerScreen);
        } else if(Objects.equals(genericContainerScreen.getTitle().getString(), GENERIC_SCREEN_CONTAINER)) {
            this.checkIsOfItem(genericContainerScreen);
        }
    }

    private void checkIsOfItem(ContainerScreen genericContainerScreen) {
        CodeExecuterHandler.runLater(2, () -> {
            net.minecraft.world.inventory.ChestMenu genericContainerScreenHandler = genericContainerScreen.getMenu();

            // Crew Info
            ItemStack crewInfoStack = genericContainerScreenHandler.getSlot(13).getItem();
            if(Objects.equals(crewInfoStack.getHoverName().getString(), "Crew Info")) {
                CrewScreenHandler.instance().checkCrewInfo(genericContainerScreenHandler);
            }
        });
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "lastContainerScreen", Pair.of(Component.literal(getLastContainerScreen()), Component.empty())
        );
    }

    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        if(screen instanceof ContainerScreen genericContainerScreen) {
            if (Objects.equals(genericContainerScreen.getTitle().getString(), AUCTION_HOUSE_SCREEN_CONTAINER)) {
                AuctionHouseScreenRenderHandler.instance().renderButtonHelp(guiGraphics, true, true);
                AuctionHouseScreenRenderHandler.instance().render(screen, guiGraphics, mouseX, mouseY, tickDelta);
            } else if (genericContainerScreen.getTitle().getString().startsWith(PERSONAL_VAULT_SCREEN_CONTAINER)) {
                PersonalVaultScreenRenderHandler.instance().renderButtonHelp(guiGraphics, true, true);
                PersonalVaultScreenRenderHandler.instance().render(screen, guiGraphics, mouseX, mouseY, tickDelta);
            } else if (Objects.equals(genericContainerScreen.getTitle().getString(), STORAGE_SCREEN_CONTAINER)) {
                ChestScreenRenderHandler.instance().renderButtonHelp(guiGraphics, true, false);
                ChestScreenRenderHandler.instance().render(screen, guiGraphics, mouseX, mouseY, tickDelta);
            } else if (Objects.equals(genericContainerScreen.getTitle().getString(), PRESETS_SCREEN_CONTAINER)) {
                PresetsScreenRenderHandler.instance().renderButtonHelp(guiGraphics, true, true);
            }
        }
    }
    //endregion
}
