package dannypx.foe.type.tracker;

import java.util.List;

public enum TrackerAction {
    SET,
    TOGGLE,
    ADD,
    SUBTRACT;

    public static List<TrackerAction> getActions(TrackerType trackerType) {
        return switch (trackerType) {
            case BOOLEAN -> List.of(SET, TOGGLE);
            case INTEGER -> List.of(SET, ADD, SUBTRACT);
        };
    }
}
