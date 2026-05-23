package dannypx.foe.type.custom_value;

public record EmptyValue() implements TrackerValue {
    public static TrackerValue getDefault() {
        return new EmptyValue();
    }
}
