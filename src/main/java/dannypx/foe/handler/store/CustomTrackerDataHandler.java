package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.custom_value.*;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.tracker.TrackerAction;
import dannypx.foe.type.tracker.TrackerType;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public class CustomTrackerDataHandler extends Handler {
    private static CustomTrackerDataHandler INSTANCE = new CustomTrackerDataHandler();

    public static CustomTrackerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomTrackerDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomTrackerDataModel customTrackerData = new CustomTrackerDataModel();
    private boolean needsUpdate = false;

    public CustomTrackerDataModel getCustomTrackerData() {
        return customTrackerData;
    }

    public void setCustomTrackerData(CustomTrackerDataModel customTrackerData) {
        Map<String, CustomTracker> trackerList = customTrackerData.trackerList;

        trackerList.forEach((key, customTracker) -> {
            if(!customTracker.isPersistent) {
                customTracker.value = customTracker.defaultValue;
                trackerList.put(key, customTracker);
            }
        });

        customTrackerData.trackerList = trackerList;

        this.customTrackerData = customTrackerData;
        this.updateCustomTrackerData();
    }

    private void updateCustomTrackerData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_TRACKER_DATA);
        }
        this.needsUpdate = false;
    }

    public Pair<Boolean, PlaceholderValue> getCustomTrackerData(String[] params) {
        if(params.length == 3
                && Objects.equals(params[0], "data")
                && customTrackerData.trackerList.containsKey(params[1])
        ) {
            CustomTracker tracker = customTrackerData.trackerList.get(params[1]);

            return switch (params[2]) {
                case "value" -> switch (tracker.value) {
                    case BooleanValue booleanValue -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(booleanValue.value())));
                    case NumberValue numberValue -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(numberValue.value())));
                    default -> PlaceholderHandler.noResult();
                };
                default -> PlaceholderHandler.noResult();
            };
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        if(customTrackerData.uuid == null && minecraft.player != null) {
            customTrackerData.uuid = minecraft.player.getUUID();
        } else if(customTrackerData.uuid != null && this.needsUpdate) {
            this.updateCustomTrackerData();
        } else if(!CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION.equals(customTrackerData.version)) {
            customTrackerData.version = CustomTrackerDataModel.CUSTOM_TRACKER_DATA_MODEL_VERSION;
            this.updateDefault();
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraft.player != null) this.setUUID(minecraft.player.getUUID());
    }

    private void setUUID(UUID uuid) {
        this.customTrackerData.uuid = uuid;
    }

    public void createNewCustomTracker(String id) {
        customTrackerData.trackerList.put(id, new CustomTracker(id));
        needsUpdate = true;
    }

    public void createNewCustomTracker(String id, CustomTracker customTracker) {
        customTrackerData.trackerList.put(id, customTracker);
        needsUpdate = true;
    }

    public CustomTracker deleteCustomTracker(String id) {
        needsUpdate = true;
        return customTrackerData.trackerList.remove(id);
    }

    public void updateTracker(String currentSelectedTracker,
                              String newName,
                              TrackerType trackerType,
                              TrackerValue defaultValue,
                              TrackerValue value,
                              boolean isPersistent,
                              boolean useTracker,
                              Map<String, Triplet<TrackerAction, String, TrackerValue>> actions) {
        CustomTracker newTracker = customTrackerData.trackerList.get(currentSelectedTracker);

        if(!Objects.equals(currentSelectedTracker, newName)) {
            newTracker = deleteCustomTracker(currentSelectedTracker);
            currentSelectedTracker = newName;
        }

        newTracker.name = newName;
        newTracker.trackerType = trackerType;
        newTracker.defaultValue = defaultValue;
        newTracker.value = value;
        newTracker.isPersistent = isPersistent;
        newTracker.useTracker = useTracker;
        newTracker.actions = actions;

        customTrackerData.trackerList.put(currentSelectedTracker, newTracker);
        needsUpdate = true;

    }

    public void updateTracker(String trackerAndActionCode) {
        if(trackerAndActionCode != null && !trackerAndActionCode.isEmpty()) {
            String[] trackerAndActionSplitString = trackerAndActionCode.split("\\.");

            if(trackerAndActionSplitString.length == 2 && customTrackerData.trackerList.containsKey(trackerAndActionSplitString[0])) {
                CustomTracker tracker = customTrackerData.trackerList.get(trackerAndActionSplitString[0]);

                if(tracker.actions.containsKey(trackerAndActionSplitString[1])) {
                    Triplet<TrackerAction, String, TrackerValue> action = tracker.actions.get(trackerAndActionSplitString[1]);

                    Pair<Boolean, MutableComponent> condition = PlaceholderHandler.parsePlaceholderFromString(action.value2());
                    if(condition.value1()) {
                        switch (tracker.trackerType) {
                            case BOOLEAN -> {
                                BooleanValue valueToUse = action.value3() instanceof EmptyValue
                                        ? (BooleanValue) BooleanValue.getFalse()
                                        : (BooleanValue) action.value3();
                                BooleanValue value = (BooleanValue) tracker.value;

                                switch (action.value1()) {
                                    case SET -> tracker.value = value.setValue(valueToUse.value());
                                    case TOGGLE -> tracker.value =  value.toggleValue();
                                }
                            }
                            case INTEGER -> {
                                NumberValue valueToUse = (NumberValue) action.value3();
                                NumberValue value = (NumberValue) tracker.value;

                                switch (action.value1()) {
                                    case SET -> tracker.value = value.setValue(valueToUse.value());
                                    case ADD -> tracker.value = value.addValue(valueToUse.value());
                                    case SUBTRACT -> tracker.value = value.subtractValue(valueToUse.value());
                                }
                            }
                        }
                    }
                }

                customTrackerData.trackerList.put(trackerAndActionSplitString[0], tracker);

                if(tracker.isPersistent) {
                    needsUpdate = true;
                }
            }
        }
    }

    public void updateTracker(String tracker, TrackerAction trackerAction, TrackerValue value) {
        if(customTrackerData.trackerList.containsKey(tracker)) {
            switch (trackerAction) {
                case SET -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    newTracker.value = value;

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case TOGGLE -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof BooleanValue currentValue) {
                        newTracker.value = currentValue.toggleValue();
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case ADD -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof NumberValue currentValue
                            && value instanceof NumberValue(int value1)
                    ) {
                        newTracker.value = currentValue.addValue(value1);
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
                case SUBTRACT -> {
                    CustomTracker newTracker = customTrackerData.trackerList.get(tracker);

                    if(newTracker.value instanceof NumberValue currentValue
                            && value instanceof NumberValue(int value1)
                    ) {
                        newTracker.value = currentValue.subtractValue(value1);
                    }

                    customTrackerData.trackerList.put(tracker, newTracker);
                    needsUpdate = true;
                }
            }
        }
    }

    public void updateDefault() {
        CustomTrackerDataModel.defaultTrackers.forEach((key, timer) -> {
            customTrackerData.trackerList.putIfAbsent(key, timer);
        });
    }

    public void fixDefault() {
        customTrackerData.trackerList.putAll(CustomTrackerDataModel.defaultTrackers);
        needsUpdate = true;
    }

    public void resetTrackers() {
        customTrackerData.trackerList = new HashMap<>(CustomTrackerDataModel.defaultTrackers);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomTrackerDataModel extends DataModels.DataModel {
        private static final String CUSTOM_TRACKER_DATA_MODEL_VERSION = "0.1";

        private static final Map<String, CustomTracker> defaultTrackers = Map.of(
                "FabledEvent", new CustomTracker(
                        "FabledEvent",
                        TrackerType.BOOLEAN,
                        BooleanValue.getFalse(),
                        BooleanValue.getFalse(),
                        true,
                        true,
                        new HashMap<>(Map.of(
                                "SetFalse", Triplet.of(TrackerAction.SET, "", BooleanValue.getFalse()),
                                "SetTrue", Triplet.of(TrackerAction.SET, "", BooleanValue.getTrue())
                        ))
                ),
                "FabledDrystreak", new CustomTracker(
                        "FabledDrystreak",
                        TrackerType.INTEGER,
                        NumberValue.of(0),
                        NumberValue.of(0),
                        true,
                        true,
                        new HashMap<>(Map.of(
                                "Add", Triplet.of(TrackerAction.ADD, "%condition.(<tracker_data.data.FabledEvent.value>==true)%%condition.(<catch.last_caught.fish.variant.fabled.name>!=fabled)%", NumberValue.of(1)),
                                "Set", Triplet.of(TrackerAction.SET, "%condition.(<tracker_data.data.FabledEvent.value>==true)%%condition.(<catch.last_caught.fish.variant.fabled.name>==fabled)%", NumberValue.of(0))
                        ))
                )
        );

        //Name Tracker, Tracker
        public Map<String, CustomTracker> trackerList = new HashMap<>(defaultTrackers);

        public CustomTrackerDataModel() {
            super(CUSTOM_TRACKER_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Tracker Object
    public static class CustomTracker {
        private String name;
        private TrackerType trackerType;
        private TrackerValue defaultValue;
        private TrackerValue value;
        private boolean isPersistent;
        private boolean useTracker;

        public String getName() {
            return name != null ? name : "";
        }

        public TrackerType getTrackerType() {
            return trackerType != null ? trackerType : TrackerType.BOOLEAN;
        }

        public TrackerValue getDefaultValue() {
            return defaultValue != null ? defaultValue : BooleanValue.getFalse();
        }

        public TrackerValue getValue() {
            return value != null ? value : BooleanValue.getFalse();
        }

        public boolean isPersistent() {
            return isPersistent;
        }

        public boolean isUseTracker() {
            return useTracker;
        }

        public Map<String, Triplet<TrackerAction, String, TrackerValue>> getActions() {
            return actions != null ? actions : new HashMap<>();
        }

        // ActionID, ActionType, Placeholder Condition, Placeholder Value
        public Map<String, Triplet<TrackerAction, String, TrackerValue>> actions;

        public CustomTracker(
                String name,
                TrackerType trackerType,
                TrackerValue defaultValue,
                TrackerValue value,
                boolean isPersistent,
                boolean useTracker,
                Map<String, Triplet<TrackerAction, String, TrackerValue>> actions
        ) {
            this.name = name;
            this.trackerType = trackerType;
            this.defaultValue = defaultValue;
            this.value = value;
            this.isPersistent = isPersistent;
            this.useTracker = useTracker;
            this.actions = actions;
        }

        public CustomTracker(String name) {
            this.name = name;
            this.trackerType = TrackerType.BOOLEAN;
            this.defaultValue = BooleanValue.getFalse();
            this.value = BooleanValue.getFalse();
            this.isPersistent = true;
            this.useTracker = true;
            this.actions = new HashMap<>(Map.of(
                    "Action #" + UUID.randomUUID(), Triplet.of(TrackerAction.SET, "", BooleanValue.getFalse())
            ));
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "customTrackerData", Pair.of(Component.literal("[customTrackerData]"), ComponentHelper.literal(getCustomTrackerData()))
        );
    }
    //endregion
}
