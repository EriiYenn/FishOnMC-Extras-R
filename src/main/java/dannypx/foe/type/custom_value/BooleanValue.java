package dannypx.foe.type.custom_value;

public record BooleanValue(boolean value) implements TrackerValue {
    @Override
    public TrackerValue setValue(boolean value) {
        return new BooleanValue(value);
    }

    @Override
    public TrackerValue toggleValue() {
        return new BooleanValue(!this.value);
    }

    public static TrackerValue getFalse() {
        return new BooleanValue(false);
    }

    public static TrackerValue getTrue() {
        return new BooleanValue(true);
    }

    public static TrackerValue of(boolean value) {
        return new BooleanValue(value);
    }
}
