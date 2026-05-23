package dannypx.foe.handler.logic;

import com.mojang.brigadier.StringReader;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.*;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.PetTagObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import java.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class NotifierHandler extends Handler {
    private static NotifierHandler INSTANCE = new NotifierHandler();

    public static NotifierHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new NotifierHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final List<Notification> notifications = new ArrayList<>();
    private final List<UUID> removeQueue = new ArrayList<>();
    private final Map<String, UUID> persistentNotifications = new HashMap<>();

    public static final String IMPORT_STATS_KEY = "importStatsKey";
    public static final String IMPORT_CREW_KEY = "importCrewKey";
    public static final String EMPTY_SLOTS_KEY = "emptySlotsKey";

    private final int WIDTH = 200;
    private final int BOX_PADDING = 7;
    private final int CONTENT_WIDTH = WIDTH - BOX_PADDING * 2 - 2;
    private final int ICON_CONTENT_WIDTH = CONTENT_WIDTH - 18;

    public List<Notification> getNotifications() {
        return notifications;
    }
    //endregion

    //region Methods
    public void init() {
        if(!ProfileDataHandler.instance().getProfileData().hasImportedStats) {
            this.notifyImportStats();
        }
    }

    public void tick() {
        boolean isRemoved = notifications.removeIf(notification -> removeQueue.contains(notification.uuid));
        if(isRemoved) removeQueue.clear();

        notifications.forEach(notification -> {
            if(System.currentTimeMillis() > notification.startTime + notification.notificationTime * 1000L) {
                this.removeNotification(notification.uuid);
            }
        });
    }

    public UUID addNotification(Notification notification) {
        notifications.add(notification);
        return notification.uuid;
    }

    public void removeNotification(UUID uuid) {
        removeQueue.add(uuid);
    }

    public void removeNotification(String key) {
        if(persistentNotifications.containsKey(key)) {
            UUID uuid = persistentNotifications.remove(key);
            this.removeNotification(uuid);
        }
    }

    public void notifyFish(
            FishTagObject fish,
            Pair<String, Integer> rarityDrystreak,
            Pair<String, Integer> variantDrystreak,
            Pair<String, Integer> sizeDryStreak
    ) {
        if(!Configs.hudConfig.showFishCatchNotification.get()) {
            return;
        }

        Component tagComponent = !Objects.equals(fish.getVariant(), "normal")
                ? ComponentHelper.concat(fish.getVariantComponent(), Component.literal(" "), fish.getRarityComponent())
                : ComponentHelper.concat(fish.getRarityComponent());
        Component rarityComponent = fish.getRarityComponent();
        Component variantComponent = fish.getVariantComponent();
        Component sizeComponent = fish.getFishSizeComponent();

        Component lengthComponent = ComponentHelper.concat(
                Component.literal(ComponentHelper.floatToString(fish.getLength(), 2)).withStyle(ChatFormatting.GRAY),
                Component.literal("in ").withStyle(ChatFormatting.GRAY)
        );

        Component weightComponent = ComponentHelper.concat(
                Component.literal(ComponentHelper.floatToString(fish.getWeight(), 2)).withStyle(ChatFormatting.GRAY),
                Component.literal("lb ").withStyle(ChatFormatting.GRAY)
        );

        List<Component> notificationComponentList = new ArrayList<>(Arrays.asList(
                tagComponent,
                fish.getName(),
                ComponentHelper.concat(fish.getFishSizeComponent(), Component.literal(" "), lengthComponent, weightComponent),
                Component.empty(),
                Component.literal(" - Drystreaks before catch").withStyle(ChatFormatting.GRAY),
                ComponentHelper.concat(rarityComponent, Component.literal(" "), ComponentHelper.literal(rarityDrystreak.value2())),
                ComponentHelper.concat(sizeComponent, Component.literal(" "), ComponentHelper.literal(sizeDryStreak.value2()))
        ));

        if(!Objects.equals(fish.getVariant(), "normal")) {
            notificationComponentList.add(ComponentHelper.concat(variantComponent, Component.literal(" "), ComponentHelper.literal(variantDrystreak.value2())));
        }

        int rows = !Configs.hudConfig.showFishDrystreakNotification.get() ? 3 : notificationComponentList.size();

        this.addNotification(
                new NotifierHandler.Notification(fish.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.fishDismissalTime.get(),
                        notificationComponentList
                )
        );
    }

    public void notifyPet(PetTagObject pet, Pair<String, Integer> rarityDrystreak, Pair<String, Integer> ratingDrystreak) {
        if(!Configs.hudConfig.showPetCatchNotification.get()) {
            return;
        }

        Component petComponent = ComponentHelper.concat(pet.getRarityComponent(), Component.literal(" ") , pet.getName());

        List<Component> notificationComponentList =  new ArrayList<>(Arrays.asList(
                petComponent,
                pet.getRatingComponent(),
                Component.empty(),
                Component.literal(" - Drystreaks before catch").withStyle(ChatFormatting.GRAY),
                ComponentHelper.concat(pet.getRarityComponent(), Component.literal(" "), ComponentHelper.literal(rarityDrystreak.value2())),
                ComponentHelper.concat(pet.getRatingComponent(), Component.literal(" "), ComponentHelper.literal(ratingDrystreak.value2()))
        ));

        int rows = !Configs.hudConfig.showPetsDrystreakNotification.get() ? 2 : notificationComponentList.size();

        this.addNotification(
                new Notification(pet.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.petDismissalTime.get(),
                        notificationComponentList
                )
        );
    }

    public void notifyItem(TagObject item, int count, Pair<String, Integer> itemDrystreak) {
        if(!Configs.hudConfig.showOtherItemCatchNotification.get()) {
            return;
        }

        Component itemComponent = ComponentHelper.concat(item.getName(), Component.literal(" "), ComponentHelper.literal(count), Component.literal("x").withStyle(ChatFormatting.GRAY));
        Component typeComponent = Component.literal(ComponentHelper.convertField(itemDrystreak.value1()));


        List<Component> notificationComponentList =  new ArrayList<>(Arrays.asList(
                itemComponent,
                Component.empty(),
                Component.literal(" - Drystreak before catch").withStyle(ChatFormatting.GRAY),
                ComponentHelper.concat(typeComponent, Component.literal(" ") , ComponentHelper.literal(itemDrystreak.value2()))
        ));

        int rows = !Configs.hudConfig.showOtherItemDrystreakNotification.get() ? 1 : notificationComponentList.size();

        this.addNotification(
                new Notification(item.getItemStack(),
                        rows, 1,
                        Configs.hudConfig.otherDismissalTime.get(),
                        notificationComponentList
                )
        );
    }

    public void notifyQuest(QuestDataHandler.Quest quest) {
        if(!Configs.hudConfig.showQuestCompletionNotification.get()) {
            return;
        }

        Component completionComponent = Component.literal("Completed quest").withStyle(ChatFormatting.GREEN);
        Component goalComponent = ComponentHelper.concat(
                ConstantDataHandler.instance().getConstantFishComponent(quest.goal),
                Component.literal(" "),
                ComponentHelper.literal(quest.current).withStyle(ChatFormatting.YELLOW),
                Component.literal("/").withStyle(ChatFormatting.GRAY),
                ComponentHelper.literal(quest.max).withStyle(ChatFormatting.WHITE)
        );

        this.addNotification(
                new Notification(
                        2, 1,
                        Configs.hudConfig.questDismissalTime.get(),
                        new ArrayList<>(Arrays.asList(completionComponent, goalComponent))
                )
        );
    }

    public void notifyImportStats() {
        UUID importStatsUUID = this.addNotification(
                new Notification(11, 1,
                        new ArrayList<>(Arrays.asList(
                                Component.literal("You have yet to import your stats").withStyle(ChatFormatting.GOLD),
                                Component.empty(),
                                ComponentHelper.concat(
                                        Component.literal("Do "),
                                        Component.literal("/foe stats import ").withStyle(ChatFormatting.GREEN),
                                        Component.literal("to import")
                                ),
                                Component.literal("your stats"),
                                Component.literal("This will open the stats screen").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                Component.literal("and import your stats").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                Component.empty(),
                                ComponentHelper.concat(
                                        Component.literal("Do "),
                                        Component.literal("/foe stats cancel ").withStyle(ChatFormatting.GREEN),
                                        Component.literal("to cancel")
                                ),
                                Component.literal("this notification"),
                                ComponentHelper.concat(
                                        Component.literal("You can still do ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                        Component.literal("/foe stats ").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
                                ),
                                ComponentHelper.concat(
                                        Component.literal("import ").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                                        Component.literal("to import at a later time").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                                )
                        ))
                )
        );

        this.persistentNotifications.put(IMPORT_STATS_KEY, importStatsUUID);
    }

    public void notifyImportCrew() {
        UUID importCrewUUID = this.addNotification(
                new Notification(12, 1,
                        new ArrayList<>(Arrays.asList(
                                Component.literal("You have yet to import ").withStyle(ChatFormatting.GOLD),
                                Component.literal("your crew info").withStyle(ChatFormatting.GOLD),
                                Component.empty(),
                                ComponentHelper.concat(
                                        Component.literal("Do "),
                                        Component.literal("/foe crew import ").withStyle(ChatFormatting.GREEN),
                                        Component.literal("to import")
                                ),
                                Component.literal("crew info"),
                                Component.literal("This will open the crew screen").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                Component.literal("and import your crew info").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                Component.empty(),
                                ComponentHelper.concat(
                                        Component.literal("Do "),
                                        Component.literal("/foe crew cancel ").withStyle(ChatFormatting.GREEN),
                                        Component.literal("to cancel")
                                ),
                                Component.literal("this notification"),
                                ComponentHelper.concat(
                                        Component.literal("You can still do ").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
                                        Component.literal("/foe crew ").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)
                                ),
                                ComponentHelper.concat(
                                        Component.literal("import ").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC),
                                        Component.literal("to import at a later time").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                                )
                        ))
                )
        );

        this.persistentNotifications.put(IMPORT_CREW_KEY, importCrewUUID);
    }

    public void notifyImportStatsCompleted() {
        this.addNotification(
                new Notification(1, 1,
                        10,
                        Collections.singletonList(
                                Component.literal("✔ Stats imported successfully").withStyle(ChatFormatting.GREEN)
                        )
                )
        );
    }

    public void notifyImportCrewCompleted() {
        this.addNotification(
                new Notification(1, 1,
                        10,
                        Collections.singletonList(
                                Component.literal("✔ Crew imported successfully").withStyle(ChatFormatting.GREEN)
                        )
                )
        );
    }

    public void notifyEmptySlots(int currentEmptySlots) {
        this.removeNotification(EMPTY_SLOTS_KEY);

        if(!Configs.hudConfig.showEmptySlotsNotification.get()) {
            return;
        }

        if(currentEmptySlots <= Configs.hudConfig.showNotificationAtEmptySlots.get()) {
            Component notificationComponent = currentEmptySlots == 0
                    ? Component.literal("You have a full inventory!").withStyle(ChatFormatting.DARK_RED)
                    : Component.literal("You nearly have a full inventory").withStyle(ChatFormatting.RED);

            UUID emptySlotsUUID = this.addNotification(new Notification(
                2, 1,
                    new ArrayList<>(Arrays.asList(
                            notificationComponent,
                            ComponentHelper.concat(
                                    Component.literal("Slots left: ").withStyle(ChatFormatting.GRAY),
                                    ComponentHelper.literal(currentEmptySlots)
                            )
                    ))
            ));

            this.persistentNotifications.put(EMPTY_SLOTS_KEY, emptySlotsUUID);
        }
    }

    public void notifyPlayerStatus(boolean isJoin, Pair<String, ItemStack> player) {
        if(!Configs.hudConfig.showCrewStatusNotification.get()) {
            return;
        }

        Component icon = isJoin
                ? Component.literal("+ ").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_GREEN)
                : Component.literal("- ").withStyle(ChatFormatting.BOLD, ChatFormatting.DARK_RED);

        Component status = isJoin
                ? Component.literal(" has joined").withStyle(ChatFormatting.GREEN)
                : Component.literal(" has left").withStyle(ChatFormatting.RED);

        this.addNotification(
                new Notification(
                        player.value2(), 1, 1, Configs.hudConfig.crewDismissalTime.get(),
                        List.of(ComponentHelper.concat(
                                icon,
                                Component.literal(player.value1()).withStyle(ChatFormatting.YELLOW),
                                status
                        ))
                )
        );
    }

    public void notifyOnTrigger(String notificationId) {
        CustomNotificationDataHandler.CustomNotification notification = CustomNotificationDataHandler.instance().getCustomNotificationData().notificationList.getOrDefault(notificationId, null);

        if(notification != null && minecraft.player != null) {
            ItemStack itemStack = ItemStack.EMPTY;

            if(!notification.getIcon().isBlank()) {
                HolderLookup.Provider lookup = minecraft.player.registryAccess();

                ItemParser itemParser = new ItemParser(lookup);
                StringReader stringReader = new StringReader(notification.getIcon());
                try {
                    ItemParser.ItemResult result = itemParser.parse(stringReader);

                    itemStack = new ItemStack(result.item(), 1);
                    itemStack.applyComponents(result.components());
                } catch (Exception e) {
                    LoggerHandler.error(e);
                }
            }

            List<MutableComponent> lines = notification.getStringLines().stream().map(string -> string.replace("&", "§")).map(PlaceholderHandler::parsePlaceholderFromString).filter(Pair::value1).map(Pair::value2).toList();
            List<Component> newLines = new ArrayList<>();

            lines.forEach(line -> newLines.addAll(ComponentHelper.wrapStyledComponent(line, notification.getIcon().isBlank() ? CONTENT_WIDTH : ICON_CONTENT_WIDTH, true, minecraft.font)));

            if(itemStack == ItemStack.EMPTY) {
                this.addNotification(
                        new Notification(
                            newLines.size(), 1, 10,
                            newLines
                        )
                );
            } else {
                this.addNotification(
                        new Notification(
                                itemStack,
                                newLines.size(), 1, 10,
                                newLines
                        )
                );
            }


        }
    }
    //endregion

    //region Notification Object
    public static class Notification {
        public final ItemStack item;
        public final int rows;
        public final int columns;
        public final List<Component> componentList;
        protected final long startTime;
        protected final int notificationTime;
        protected final UUID uuid;

        public Notification(
                ItemStack item,
                int rows,
                int columns,
                int notificationTime,
                List<Component> componentList
        ) {
            this.item = item;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = notificationTime;
            this.componentList = componentList;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                int rows,
                int columns,
                int notificationTime,
                List<Component> componentList
        ) {
            this.item = ItemStack.EMPTY;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = notificationTime;
            this.componentList = componentList;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                ItemStack item,
                int rows,
                int columns,
                List<Component> componentList
        ) {
            this.item = item;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = Integer.MAX_VALUE;
            this.componentList = componentList;
            this.uuid = UUID.randomUUID();
        }

        public Notification(
                int rows,
                int columns,
                List<Component> componentList
        ) {
            this.item = ItemStack.EMPTY;
            this.rows = rows;
            this.columns = columns;
            this.startTime = System.currentTimeMillis();
            this.notificationTime = Integer.MAX_VALUE;
            this.componentList = componentList;
            this.uuid = UUID.randomUUID();
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "notifications", Pair.of(Component.literal("[notifications]]"), ComponentHelper.literal(getNotifications()))
        );
    }
    //endregion
}
