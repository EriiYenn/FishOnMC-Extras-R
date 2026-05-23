package dannypx.foe.type.custom_value;

public sealed interface TrackerValue permits NumberValue, BooleanValue, EmptyValue, ErrorValue {
    default TrackerValue setValue(boolean value) { return null; }
    default TrackerValue toggleValue() { return null; }

    default TrackerValue setValue(int value) { return null; }
    default TrackerValue addValue(int value) { return null; }
    default TrackerValue subtractValue(int value) { return null; }
}
