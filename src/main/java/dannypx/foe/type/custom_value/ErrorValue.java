package dannypx.foe.type.custom_value;

public record ErrorValue() implements TrackerValue {
    public static TrackerValue getDefault() {
        return new ErrorValue();
    }
}
