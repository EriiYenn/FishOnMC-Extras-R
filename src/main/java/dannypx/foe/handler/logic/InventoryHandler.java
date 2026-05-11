package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.NetworkHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.*;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.placeholder.ComponentValue;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class InventoryHandler extends Handler {
    private static InventoryHandler INSTANCE = new InventoryHandler();

    public static InventoryHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new InventoryHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final List<UUID> trackedFish = new ArrayList<>();
    private NonNullList<ItemStack> snapshotInventory = NonNullList.createWithCapacity(0);
    private List<Triplet<Long, ItemStack, Integer>> snapshottedItems = new ArrayList<>();
    private List<Triplet<Long, ItemStack, Integer>> snapshottedRemovedItems = new ArrayList<>();
    private FishingRodTagObject currentFishingRod = FishingRodTagObject.empty();
    private PetTagObject currentPet = PetTagObject.empty();

    private boolean currentlyLoading = false;

    private int currentEmptySlots = 27;

    public List<UUID> getTrackedFish() {
        return trackedFish;
    }

    public NonNullList<ItemStack> getSnapshotInventory() {
        return snapshotInventory.isEmpty() ? NonNullList.createWithCapacity(0) : snapshotInventory;
    }

    public List<Triplet<Long, ItemStack, Integer>> getSnapshottedItems() {
        return snapshottedItems;
    }

    public List<ItemStack> getSnapshottedItemstacks() {
        return this.snapshottedItems.stream().map(Triplet::value2).toList();
    }

    protected void setCurrentFishingRod(FishingRodTagObject currentFishingRod) {
        this.currentFishingRod = currentFishingRod;
    }

    public FishingRodTagObject getCurrentFishingRod() {
        return this.currentFishingRod;
    }

    protected  void setCurrentPet(PetTagObject currentPet) {
        this.currentPet = currentPet;
    }

    public PetTagObject getCurrentPet() {
        return this.currentPet;
    }

    public boolean hasPet() {
        return this.currentPet.getItemStack() != ItemStack.EMPTY;
    }

    public int getCurrentEmptySlots() {
        return currentEmptySlots;
    }

    public Pair<Boolean, PlaceholderValue> getInventory(String[] params) {
        if(params.length > 0) {
            Pattern fieldPattern = Pattern.compile("^(fishing_rod|pet|armor|empty_slots|held_item|slot)$");

            if(fieldPattern.matcher(params[0]).matches()) {
                return switch(params[0]) {
                    case "fishing_rod" -> {
                        if(params.length >= 2) {
                            yield switch(params[1]) {
                                case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(getCurrentFishingRod().getName()));
                                case "line" -> {
                                    List<TagObject> list = getCurrentFishingRod().getLineItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                case "reel" -> {
                                    List<TagObject> list = getCurrentFishingRod().getReelItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                case "pole" -> {
                                    List<TagObject> list = getCurrentFishingRod().getPoleItem();
                                    if(!list.isEmpty()) {
                                        yield switch(params[2]) {
                                            case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(list.getFirst().getName()));
                                            default -> PlaceholderHandler.getNbtValue(list.getFirst(), params[2]);
                                        };
                                    }
                                    yield PlaceholderHandler.noResult();
                                }
                                default -> PlaceholderHandler.getNbtValue(getCurrentFishingRod(), params[1]);
                            };
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "pet" -> {
                        if(params.length == 2
                                && hasPet()
                        ) {
                            yield switch(params[1]) {
                                case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(getCurrentPet().getName()));
                                case "level" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(getCurrentPet().getLevel())));
                                case "level_progress" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getProgress() * 100, 2)));
                                case "rating" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(getCurrentPet().getRatingComponent()));
                                case "rating_percent" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getTotalPercent() * 100, 2)));
                                case "rarity" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(getCurrentPet().getRarityComponent()));
                                case "location_luck_percent" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getLocationPercentMaxLuck() * 100, 2)));
                                case "location_scale_percent" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getLocationPercentMaxScale() * 100, 2)));
                                case "climate_luck_percent" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getClimatePercentMaxLuck() * 100, 2)));
                                case "climate_scale_percent" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getClimatePercentMaxScale() * 100, 2)));
                                case "location_luck" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getLocationMaxLuck(), 0)));
                                case "location_scale" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getLocationMaxScale(), 0)));
                                case "climate_luck" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getClimateMaxLuck(), 0)));
                                case "climate_scale" -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(getCurrentPet().getClimateMaxScale(), 0)));
                                default -> PlaceholderHandler.getNbtValue(getCurrentPet(), params[1]);
                            };
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "armor" -> {
                        if(params.length == 3
                                && minecraft.player != null
                        ) {
                            ItemStack stack = ItemStack.EMPTY;

                            switch(params[1]) {
                                case "chestplate" -> stack = minecraft.player.getItemBySlot(EquipmentSlot.CHEST);
                                case "leggings" -> stack = minecraft.player.getItemBySlot(EquipmentSlot.LEGS);
                                case "boots" -> stack = minecraft.player.getItemBySlot(EquipmentSlot.FEET);
                            }

                            Pair<Boolean, TagObject> validatedItem = ValidateItem.isServerItem(stack);

                            if(validatedItem.value1()) {
                                yield switch(params[2]) {
                                    case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(validatedItem.value2().getName()));
                                    default -> PlaceholderHandler.getNbtValue(validatedItem.value2(), params[2]);
                                };
                            }
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "empty_slots" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(getCurrentEmptySlots())));
                    case "held_item" -> {
                        if(params.length >= 2
                                && minecraft.player != null
                        ) {
                            ItemStack heldItem = minecraft.player.getInventory().getSelectedItem();
                            if(!heldItem.isEmpty()) {
                                yield switch (params[1]) {
                                    case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(heldItem.getHoverName()));
                                    case "tooltip" -> {
                                        if(params.length >= 3) {
                                            if(heldItem.get(DataComponents.LORE) != null) {
                                                List<Component> lines = heldItem.get(DataComponents.LORE).lines();
                                                try {
                                                    int index = Integer.parseInt(params[2]);

                                                    if(index < lines.size()) {
                                                        yield PlaceholderHandler.getPlaceholderValue(new ComponentValue(lines.get(index)));
                                                    }
                                                } catch (Exception ignored) {}
                                            }
                                        }
                                        yield PlaceholderHandler.noResult();
                                    }
                                    default -> PlaceholderHandler.getNbtValue(heldItem, params[1]);
                                };
                            }
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "slot" -> {
                        if(params.length >= 3
                                && minecraft.player != null
                        ) {
                            try {
                                int slot = Integer.parseInt(params[1]);
                                ItemStack stack = minecraft.player.getInventory().getItem(slot);

                                if(!stack.isEmpty()) {
                                    yield switch (params[2]) {
                                        case "name" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(stack.getHoverName()));
                                        case "tooltip" -> {
                                            if(params.length >= 4) {
                                                if(stack.get(DataComponents.LORE) != null) {
                                                    List<Component> lines = stack.get(DataComponents.LORE).lines();
                                                    try {
                                                        int index = Integer.parseInt(params[3]);

                                                        if(index < lines.size()) {
                                                            yield PlaceholderHandler.getPlaceholderValue(new ComponentValue(lines.get(index)));
                                                        }
                                                    } catch (Exception ignored) {}
                                                }
                                            }
                                            yield PlaceholderHandler.noResult();
                                        }
                                        default -> PlaceholderHandler.getNbtValue(stack, params[2]);
                                    };
                                }
                            } catch (Exception ignored) {}
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        if(minecraft.player != null) {
            this.tickInventory();
            this.snapshotFishingRod();
            this.snapshotPet();
            this.snapshotEmptySlots();
            this.checkSnapshottedItems();
        }
    }

    private void tickInventory() {
        if(!snapshotInventory.isEmpty()) {
            NonNullList<ItemStack> oldInventory = snapshotInventory;
            NonNullList<ItemStack> newInventory = minecraft.player.getInventory().getNonEquipmentItems();

            for(int i = 0; i < newInventory.size(); i++) {
                ItemStack oldStack = oldInventory.get(i);
                ItemStack newStack = newInventory.get(i);

                // New item in slot
                if ((oldStack.isEmpty() && !newStack.isEmpty())
                        || (newStack.isEmpty() && !oldStack.isEmpty())
                ) {
                    this.snapshotInventory();
                    int finalI = i;
                    if(!newStack.isEmpty() &&
                            snapshottedRemovedItems.stream()
                                    .noneMatch(
                                            removedItem -> removedItem.value3() == finalI
                                            && ItemStack.isSameItemSameComponents(removedItem.value2(), newStack)
                                    )
                    ) this.addToSnapshotItems(newStack, newStack.getCount());
                    if(newStack.isEmpty()) this.addToRemovedSnapshotItems(oldStack, i);
                }

                // Same item, stack size changed
                if (!newStack.isEmpty()
                        && !oldStack.isEmpty()
                        && oldStack.getCount() != newStack.getCount()) {
                    this.snapshotInventory();
                    this.addToSnapshotItems(newStack, newStack.getCount() - oldStack.getCount());
                }
            }
        } else {
            if(!currentlyLoading) {
                currentlyLoading = true;
                CodeExecuterHandler.runLater(100, this::snapshotInventory);
            }
        }
    }

    public void onLeave() {
        this.reset();
    }

    public void reset() {
        this.currentlyLoading = false;
        this.snapshotInventory.clear();
        this.snapshottedItems.clear();
    }

    private void checkSnapshottedItems() {
        snapshottedItems.removeIf(item -> item.value1() > System.currentTimeMillis() + 1000L);
        snapshottedRemovedItems.removeIf(item -> item.value1() > System.currentTimeMillis() + 60L + NetworkHandler.instance().getPing());
    }

    private void addToSnapshotItems(ItemStack newStack, int count) {
        LoggerHandler._debug("Snapshotted Item: " + newStack.getHoverName().getString() + " at " + System.currentTimeMillis());
        snapshottedItems.add(Triplet.of(System.currentTimeMillis(), newStack, count));
    }

    private void addToRemovedSnapshotItems(ItemStack oldStack, int slot) {
        snapshottedRemovedItems.add(Triplet.of(System.currentTimeMillis(), oldStack, slot));
    }

    private void snapshotEmptySlots() {
        int empty = 0;

        for (ItemStack stack : minecraft.player.getInventory().getNonEquipmentItems()) {
            if (stack.isEmpty()) {
                empty++;
            }
        }

        if(currentEmptySlots != empty) {
            currentEmptySlots = empty;
            NotifierHandler.instance().notifyEmptySlots(currentEmptySlots);
        }
    }

    private void snapshotPet() {
        if(ProfileDataHandler.instance().getProfileData().activePetSlot != -1) {
            ItemStack pet = minecraft.player.getInventory().getNonEquipmentItems().get(ProfileDataHandler.instance().getProfileData().activePetSlot);
            if(!pet.isEmpty() && !ItemStack.isSameItemSameComponents(currentPet.getItemStack(), pet)) {
                Pair<Boolean, @Nullable PetTagObject> validatedPet = ValidateItem.isPet(pet);
                if(validatedPet.value1()) {
                    this.setCurrentPet(validatedPet.value2());
                }
            }
        } else if(ProfileDataHandler.instance().getProfileData().activePetSlot == -1
                && currentPet.getItemStack() != ItemStack.EMPTY
        ) {
            currentPet = PetTagObject.empty();
        }
    }

    private void snapshotFishingRod() {
        ItemStack fishingRod = minecraft.player.getInventory().getNonEquipmentItems().getFirst();
        if(!fishingRod.isEmpty() && !ItemStack.isSameItemSameComponents(currentFishingRod.getItemStack(), fishingRod)) {
            Pair<Boolean, @Nullable FishingRodTagObject> validatedFishingRod = ValidateItem.isFishingRod(fishingRod);
            if(validatedFishingRod.value1()) {
                this.setCurrentFishingRod(validatedFishingRod.value2());
            }
        }
    }

    public void trackFishOffSide() {
        if(minecraft.player != null
                && CatchingHandler.instance().isScanDone()
        ) {
            this.trackAllFish();
        }
    }

    public void snapshotInventory() {
        if(minecraft.player != null) {
            snapshotInventory = ItemStackHelper.deepCopy(
                    minecraft.player.getInventory().getNonEquipmentItems(),
                    ItemStack.EMPTY,
                    stack -> stack.isEmpty() ? ItemStack.EMPTY : stack.copy()
            );
        }
    }

    public void addToTrackedFish(UUID uuid) {
        if (!trackedFish.contains(uuid)) {
            trackedFish.add(uuid);
        }
    }

    public boolean trackAllFish() {
        if(minecraft.player != null) {
            trackedFish.clear();
            minecraft.player.getInventory().getNonEquipmentItems().forEach(itemStack -> {
                Pair<Boolean, FishTagObject> validatedItem = ValidateItem.isFish(itemStack);
                if(validatedItem.value1() && validatedItem.value2().isOwn()) {
                    this.addToTrackedFish(validatedItem.value2().getID());
                }
            });
            LoggerHandler._debug("Tracked Fish: " + trackedFish.size());
            return true;
        }
        return false;
    }

    public TagObject getCurrentHeldItem() {
        if(minecraft.player != null) {
            ItemStack heldItem = minecraft.player.getMainHandItem();
            Pair<Boolean, TagObject> validatedItem = ValidateItem.isServerItem(heldItem);
            if(validatedItem.value1()) {
                return validatedItem.value2();
            }
        }
        return TagObject.empty();
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "trackedFish", Pair.of(Component.literal("[trackedFish]"), ComponentHelper.literal(getTrackedFish())),
                "snapshotInventory", Pair.of(Component.literal("[snapshotInventory]"), ComponentHelper.literal(
                        ItemStackHelper.itemStackListToJson(getSnapshotInventory())
                )),
                "currentFishingRod", Pair.of(Component.literal("[currentFishingRod]"), ComponentHelper.literal(getCurrentFishingRod().getItemStack())),
                "currentPet", Pair.of(Component.literal("[currentPet]"), ComponentHelper.literal(getCurrentPet().getItemStack())),
                "currentHeldItem", Pair.of(Component.literal("[currentHeldItem]"), ComponentHelper.literal(getCurrentHeldItem())),
                "snapshottedItems", Pair.of(Component.literal("[snapshottedItems]"), ComponentHelper.literal(getSnapshottedItemstacks()))
        );
    }
    //endregion
}
