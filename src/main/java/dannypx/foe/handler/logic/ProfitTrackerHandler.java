package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.tuple.Pair;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ProfitTrackerHandler extends Handler {
    private static ProfitTrackerHandler INSTANCE = new ProfitTrackerHandler();

    public static ProfitTrackerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfitTrackerHandler();
        }
        return INSTANCE;
    }

    private long sessionStartMillis;
    private double sessionTotal;
    private double lastCatchValue;
    private Component lastCatchLabel = Component.empty();

    private ProfitTrackerHandler() {
        resetSession();
    }

    public void resetSession() {
        sessionStartMillis = System.currentTimeMillis();
        sessionTotal = 0d;
        lastCatchValue = 0d;
        lastCatchLabel = Component.empty();
    }

    public void onSessionStart() {
        sessionStartMillis = System.currentTimeMillis();
    }

    public void recordCatch(TagObject tag, int count) {
        if (count <= 0) {
            return;
        }
        float unit = ItemValueResolver.unitValueFor(tag);
        double add = unit * (double) count;
        sessionTotal += add;
        lastCatchValue = add;
        lastCatchLabel = tag.getName().plainCopy();
    }

    public void recordFishCatch(FishTagObject fish) {
        recordCatch(fish, 1);
    }

    public Pair<Boolean, PlaceholderValue> getProfitTracker(String[] params) {
        if (params.length == 0) {
            return PlaceholderHandler.noResult();
        }
        return switch (params[0]) {
            case "session_total" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(formatCompact(sessionTotal)));
            case "session_total_raw" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) sessionTotal, 2)));
            case "per_hour" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(formatCompact(perHour())));
            case "per_hour_raw" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) perHour(), 2)));
            case "last" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(formatCompact(lastCatchValue)));
            case "last_raw" -> PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) lastCatchValue, 2)));
            case "last_name" -> lastCatchLabel.getString().isEmpty()
                    ? PlaceholderHandler.noResult()
                    : PlaceholderHandler.getPlaceholderValue(new StringValue(lastCatchNameTruncated()));
            default -> PlaceholderHandler.noResult();
        };
    }

    private double perHour() {
        long elapsed = System.currentTimeMillis() - sessionStartMillis;
        if (elapsed < 1000L) {
            return 0d;
        }
        return sessionTotal * 3600_000d / (double) elapsed;
    }

    public String sessionTotalFormatted() {
        return formatCompact(sessionTotal);
    }

    public String perHourFormatted() {
        return formatCompact(perHour());
    }

    public String lastCatchFormatted() {
        return formatCompact(lastCatchValue);
    }

    public String lastCatchNameTruncated() {
        String s = lastCatchLabel.getString();
        if (s.isEmpty()) {
            return "—";
        }
        return s.length() > 18 ? s.substring(0, 17) + "\u2026" : s;
    }

    private static String formatCompact(double v) {
        if (v < 0d) {
            v = 0d;
        }
        if (v >= 1_000_000d) {
            return String.format(Locale.US, "%.2fM", v / 1_000_000d);
        }
        if (v >= 1000d) {
            return String.format(Locale.US, "%.2fK", v / 1000d);
        }
        if (v == Math.rint(v)) {
            return String.format(Locale.US, "%.0f", v);
        }
        return String.format(Locale.US, "%.2f", v);
    }

    //region Dev
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "sessionTotal", Pair.of(Component.literal(String.valueOf(sessionTotal)), Component.empty()),
                "lastCatch", Pair.of(Component.literal(String.valueOf(lastCatchValue)), Component.empty())
        );
    }
    //endregion
}
