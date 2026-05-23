package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.tuple.Pair;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CustomNotificationDataHandler extends Handler {
    private static CustomNotificationDataHandler INSTANCE = new CustomNotificationDataHandler();

    public static CustomNotificationDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomNotificationDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomNotificationDataModel customNotificationData = new CustomNotificationDataModel();
    private boolean needsUpdate = false;

    public CustomNotificationDataModel getCustomNotificationData() {
        return customNotificationData;
    }

    public void setCustomNotificationData(CustomNotificationDataModel customNotificationData) {
        this.customNotificationData = customNotificationData;
        this.updateCustomNotificationData();
    }

    private void updateCustomNotificationData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_NOTIFICATION_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(customNotificationData.uuid == null && minecraft.player != null) {
            customNotificationData.uuid = minecraft.player.getUUID();
        } else if(customNotificationData.uuid != null && this.needsUpdate) {
            this.updateCustomNotificationData();
        } else if(!CustomNotificationDataModel.CUSTOM_NOTIFICATION_DATA_MODEL_VERSION.equals(customNotificationData.version)) {
            customNotificationData.version = CustomNotificationDataModel.CUSTOM_NOTIFICATION_DATA_MODEL_VERSION;
            this.updateDefault();
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraft.player != null) this.setUUID(minecraft.player.getUUID());
    }

    private void setUUID(UUID uuid) {
        this.customNotificationData.uuid = uuid;
    }

    public void createNewCustomNotification(String id) {
        customNotificationData.notificationList.put(id, new CustomNotification(id));
        needsUpdate = true;
    }

    public void createNewCustomNotification(String id, CustomNotification customNotification) {
        customNotificationData.notificationList.put(id, customNotification);
        needsUpdate = true;
    }

    public CustomNotification deleteCustomNotification(String id) {
        needsUpdate = true;
        return customNotificationData.notificationList.remove(id);
    }

    public void updateNotification(String currentSelectedNotification, String newName, String icon, List<String> list) {
        CustomNotification newNotification = customNotificationData.notificationList.get(currentSelectedNotification);


        if(!Objects.equals(currentSelectedNotification, newName)) {
            newNotification = deleteCustomNotification(currentSelectedNotification);
            currentSelectedNotification = newName;
        }

        newNotification.name = newName;
        newNotification.stringLines = list;
        newNotification.icon = icon;

        customNotificationData.notificationList.put(currentSelectedNotification, newNotification);
        needsUpdate = true;
    }

    public void updateDefault() {
        CustomNotificationDataModel.defaultNotifications.forEach((key, timer) -> {
            customNotificationData.notificationList.putIfAbsent(key, timer);
        });
    }

    public void fixDefault() {
        customNotificationData.notificationList.putAll(CustomNotificationDataModel.defaultNotifications);
        needsUpdate = true;
    }

    public void resetNotifications() {
        customNotificationData.notificationList = new HashMap<>(CustomNotificationDataModel.defaultNotifications);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomNotificationDataModel extends DataModels.DataModel {
        private static final String CUSTOM_NOTIFICATION_DATA_MODEL_VERSION = "0.1";

        private static final Map<String, CustomNotification> defaultNotifications = Map.of(
                "Contest Start", new CustomNotification(
                        "Contest Start",
                        "",
                        new ArrayList<>(List.of(
                                "&6A contest has started",
                                "%chat.trigger.Contest Type%",
                                "%chat.trigger.Contest Location%"
                        ))
                ),
                "Contest End", new CustomNotification(
                        "Contest End",
                        "",
                        new ArrayList<>(List.of(
                                "&6A contest has ended",
                                "&7- Placements",
                                "%chat.trigger.Contest 1st%",
                                "%chat.trigger.Contest 2nd%",
                                "%chat.trigger.Contest 3rd%"
                        ))
                )
        );

        //Name Notification, Notification
        public Map<String, CustomNotification> notificationList = new HashMap<>(defaultNotifications);

        public CustomNotificationDataModel() {
            super(CUSTOM_NOTIFICATION_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Notification Object
    public static class CustomNotification {
        private String name;
        private String icon;
        private List<String> stringLines;

        public String getName() {
            return name != null ? name : "";
        }

        public String getIcon() {
            return icon != null ? icon : "";
        }

        public List<String> getStringLines() {
            return stringLines != null ? stringLines : new ArrayList<>();
        }

        public CustomNotification(String name, String icon, List<String> stringLines) {
            this.name = name;
            this.icon = icon;
            this.stringLines = stringLines;
        }

        public CustomNotification(String name) {
            this.name = name;
            this.icon = "";
            this.stringLines = new ArrayList<>(List.of(
                    "Example line"
            ));
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "customNotificationData", Pair.of(Component.literal("[customNotificationData]"), ComponentHelper.literal(getCustomNotificationData()))
        );
    }
    //endregion
}
