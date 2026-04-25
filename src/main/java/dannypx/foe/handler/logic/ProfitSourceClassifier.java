package dannypx.foe.handler.logic;

import dannypx.foe.handler.store.ProfitCategoryDataHandler;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ProfitSourceClassifier {
    private static final Pattern FISH_GROUP_PATTERN = Pattern.compile("(?i)fish\\s*group\\s*:?\\s*(.+)");
    private static final List<String> FISH_GROUP_KEYS = List.of("fishGroup", "fish_group", "group");
    private static final Set<String> RARE_RARITIES = Set.of("epic", "legendary", "mythic", "mythical", "fabled");

    private ProfitSourceClassifier() {}

    public static ProfitSource classify(TagObject tag) {
        ProfitSource source = classifyByHeuristics(tag);
        ProfitCategoryDataHandler.CategoryRule rule = ProfitCategoryDataHandler.instance().findOverride(tag);
        if (rule == null) {
            return source;
        }

        String categoryLabel = isBlank(rule.category) ? source.categoryLabel : prettify(rule.category);
        String categoryId = isBlank(rule.category) ? source.categoryId : toKey(rule.category);
        String subLabel = isBlank(rule.sub) ? source.subLabel : prettify(rule.sub);
        String subId = isBlank(rule.sub) ? source.subId : toKey(rule.sub);

        return new ProfitSource(categoryId, categoryLabel, subId, subLabel);
    }

    private static ProfitSource classifyByHeuristics(TagObject tag) {
        if (isFish(tag)) {
            String fishGroup = fishGroup(tag);
            return new ProfitSource("fish", "Fish", toKey(fishGroup), fishGroup);
        }

        String itemName = cleanItemName(tag.getName().getString());
        if (isShard(tag, itemName)) {
            String shardName = itemName.endsWith(" Shard") ? itemName.substring(0, itemName.length() - " Shard".length()) : itemName;
            return new ProfitSource("shards", "Shards", toKey(shardName), shardName);
        }

        if (isRare(tag)) {
            return new ProfitSource("rare_items", "Rare Items", toKey(itemName), itemName);
        }

        String fallback = itemName.isBlank() ? "Other" : itemName;
        return new ProfitSource("other", "Other", toKey(fallback), fallback);
    }

    private static boolean isFish(TagObject tag) {
        return "fish".equalsIgnoreCase(tag.getType()) || ValidateItem.isFish(tag.getItemStack()).value1();
    }

    private static boolean isShard(TagObject tag, String itemName) {
        return !isFish(tag) && normalize(itemName).contains("shard");
    }

    private static boolean isRare(TagObject tag) {
        return RARE_RARITIES.contains(normalize(tag.getRarity()));
    }

    private static String fishGroup(TagObject tag) {
        for (String key : FISH_GROUP_KEYS) {
            if (tag.contains(key)) {
                String value = tag.getString(key);
                if (!value.isBlank()) {
                    return prettify(value);
                }
            }
        }

        for (net.minecraft.network.chat.Component line : tag.getLore()) {
            Matcher matcher = FISH_GROUP_PATTERN.matcher(line.getString());
            if (matcher.find()) {
                String value = matcher.group(1).trim();
                if (!value.isBlank()) {
                    return prettify(value);
                }
            }
        }

        if (tag instanceof FishTagObject fish && !fish.getFish().isBlank()) {
            return "Other";
        }
        return "Other";
    }

    public static String toKey(String value) {
        String normalized = normalize(value).replace(' ', '_');
        return normalized.isBlank() ? "other" : normalized;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ").toLowerCase(Locale.US);
    }

    private static String prettify(String value) {
        String trimmed = value == null ? "" : value.trim().replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ");
        if (trimmed.isBlank()) {
            return "Other";
        }

        if (trimmed.equals(trimmed.toUpperCase(Locale.US)) || trimmed.equals(trimmed.toLowerCase(Locale.US))) {
            String[] parts = trimmed.toLowerCase(Locale.US).split(" ");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].isBlank()) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append(' ');
                }
                builder.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    builder.append(parts[i].substring(1));
                }
            }
            return builder.toString();
        }
        return trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String cleanItemName(String itemName) {
        return itemName == null || itemName.isBlank() ? "Other" : itemName.trim();
    }

    public static class ProfitSource {
        public final String categoryId;
        public final String categoryLabel;
        public final String subId;
        public final String subLabel;

        public ProfitSource(String categoryId, String categoryLabel, String subId, String subLabel) {
            this.categoryId = categoryId;
            this.categoryLabel = categoryLabel;
            this.subId = subId;
            this.subLabel = subLabel;
        }
    }
}
