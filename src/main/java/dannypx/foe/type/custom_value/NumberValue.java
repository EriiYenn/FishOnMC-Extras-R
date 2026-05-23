package dannypx.foe.type.custom_value;

public record NumberValue(int value) implements TrackerValue {
    @Override
    public TrackerValue setValue(int value) {
        return new NumberValue(value);
    }

    @Override
    public TrackerValue addValue(int value) {
        return new NumberValue(this.value + value);
    }

    @Override
    public TrackerValue subtractValue(int value) {
        return new NumberValue(this.value - value);
    }

    public static TrackerValue getDefault() {
        return new NumberValue(0);
    }

    public static TrackerValue of(int value) {
        return new NumberValue(value);
    }
}
