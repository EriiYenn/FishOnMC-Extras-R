package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.tuple.Pair;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final Map<String, BreakdownBucket> categoryBreakdown = new LinkedHashMap<>();

    private ProfitTrackerHandler() {
        resetSession();
    }

    public void resetSession() {
        sessionStartMillis = System.currentTimeMillis();
        sessionTotal = 0d;
        lastCatchValue = 0d;
        lastCatchLabel = Component.empty();
        categoryBreakdown.clear();
    }

    public void onSessionStart() {
        sessionStartMillis = System.currentTimeMillis();
    }

    public void recordCatch(TagObject tag, int count) {
        recordItemReward(tag, count, null);
    }

    public void recordItemReward(TagObject tag, int count, ProfitSourceClassifier.ProfitSource sourceOverride) {
        if (count <= 0) {
            return;
        }
        float unit = ItemValueResolver.unitValueFor(tag);
        double add = unit * (double) count;
        sessionTotal += add;
        lastCatchValue = add;
        lastCatchLabel = tag.getName().plainCopy();
        if (add > 0d) {
            this.addBreakdown(sourceOverride == null ? ProfitSourceClassifier.classify(tag) : sourceOverride, add);
        }
    }

    public void recordFishCatch(FishTagObject fish) {
        recordCatch(fish, 1);
    }

    public void recordMoneyReward(double amount, ProfitSourceClassifier.ProfitSource source) {
        if (amount <= 0d || source == null) {
            return;
        }
        sessionTotal += amount;
        lastCatchValue = amount;
        lastCatchLabel = Component.literal(source.categoryLabel + " reward");
        this.addBreakdown(source, amount);
    }

    public Pair<Boolean, PlaceholderValue> getProfitTracker(String[] params) {
        if (params.length == 0) {
            return PlaceholderHandler.noResult();
        }
        if ("breakdown_total".equals(params[0])) {
            return PlaceholderHandler.getPlaceholderValue(new StringValue(formatCompact(sessionTotal)));
        }
        if ("breakdown_total_raw".equals(params[0])) {
            return PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) sessionTotal, 2)));
        }
        if ("breakdown_line".equals(params[0]) && params.length >= 2) {
            return PlaceholderHandler.getPlaceholderValue(new StringValue(formatCompact(categoryTotal(params[1]))));
        }
        if ("breakdown_line_raw".equals(params[0]) && params.length >= 2) {
            return PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) categoryTotal(params[1]), 2)));
        }
        if ("breakdown_percent".equals(params[0]) && params.length >= 2) {
            return PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) categoryPercent(params[1]), 1)));
        }
        if ("breakdown_sub".equals(params[0]) && params.length >= 3) {
            return PlaceholderHandler.getPlaceholderValue(
                    new StringValue(formatCompact(subTotal(params[1], params[2]))));
        }
        if ("breakdown_sub_raw".equals(params[0]) && params.length >= 3) {
            return PlaceholderHandler.getPlaceholderValue(
                    new StringValue(ComponentHelper.floatToString((float) subTotal(params[1], params[2]), 2)));
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

    public static String formatValue(double value) {
        return formatCompact(value);
    }

    public String lastCatchNameTruncated() {
        String s = lastCatchLabel.getString();
        if (s.isEmpty()) {
            return "—";
        }
        return s.length() > 18 ? s.substring(0, 17) + "\u2026" : s;
    }

    public boolean hasBreakdown() {
        return !categoryBreakdown.isEmpty();
    }

    public List<ProfitBreakdownCategoryRow> getBreakdownRows() {
        return sortedBuckets(categoryBreakdown)
                .stream()
                .map(bucket -> new ProfitBreakdownCategoryRow(
                        bucket.id,
                        bucket.label,
                        bucket.total,
                        sessionTotal <= 0d ? 0d : bucket.total * 100d / sessionTotal,
                        sortedBuckets(bucket.children).stream()
                                .map(child -> new ProfitBreakdownSubRow(child.id, child.label, child.total))
                                .toList()
                ))
                .toList();
    }

    private void addBreakdown(ProfitSourceClassifier.ProfitSource source, double add) {
        BreakdownBucket category = categoryBreakdown.computeIfAbsent(
                source.categoryId,
                key -> new BreakdownBucket(source.categoryId, source.categoryLabel)
        );
        category.total += add;

        BreakdownBucket subBucket = category.children.computeIfAbsent(
                source.subId,
                key -> new BreakdownBucket(source.subId, source.subLabel)
        );
        subBucket.total += add;
    }

    private double categoryTotal(String categoryId) {
        BreakdownBucket category = categoryBreakdown.get(ProfitSourceClassifier.toKey(categoryId));
        return category == null ? 0d : category.total;
    }

    private double categoryPercent(String categoryId) {
        if (sessionTotal <= 0d) {
            return 0d;
        }
        return categoryTotal(categoryId) * 100d / sessionTotal;
    }

    private double subTotal(String categoryId, String subId) {
        BreakdownBucket category = categoryBreakdown.get(ProfitSourceClassifier.toKey(categoryId));
        if (category == null) {
            return 0d;
        }
        BreakdownBucket sub = category.children.get(ProfitSourceClassifier.toKey(subId));
        return sub == null ? 0d : sub.total;
    }

    private static List<BreakdownBucket> sortedBuckets(Map<String, BreakdownBucket> buckets) {
        List<BreakdownBucket> sorted = new ArrayList<>(buckets.values());
        sorted.sort(Comparator
                .comparingDouble((BreakdownBucket bucket) -> bucket.total)
                .reversed()
                .thenComparing(bucket -> bucket.label, String.CASE_INSENSITIVE_ORDER));
        return sorted;
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
                "lastCatch", Pair.of(Component.literal(String.valueOf(lastCatchValue)), Component.empty()),
                "breakdownCategories", Pair.of(Component.literal(Integer.toString(categoryBreakdown.size())), Component.empty())
        );
    }
    //endregion

    private static class BreakdownBucket {
        private final String id;
        private final String label;
        private double total;
        private final Map<String, BreakdownBucket> children = new LinkedHashMap<>();

        private BreakdownBucket(String id, String label) {
            this.id = id;
            this.label = label;
        }
    }

    public static class ProfitBreakdownCategoryRow {
        public final String id;
        public final String label;
        public final double total;
        public final double percentOfSession;
        public final List<ProfitBreakdownSubRow> subRows;

        public ProfitBreakdownCategoryRow(String id, String label, double total, double percentOfSession, List<ProfitBreakdownSubRow> subRows) {
            this.id = id;
            this.label = label;
            this.total = total;
            this.percentOfSession = percentOfSession;
            this.subRows = subRows;
        }
    }

    public static class ProfitBreakdownSubRow {
        public final String id;
        public final String label;
        public final double total;

        public ProfitBreakdownSubRow(String id, String label, double total) {
            this.id = id;
            this.label = label;
            this.total = total;
        }
    }
}
