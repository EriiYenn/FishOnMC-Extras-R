package dannypx.foe.handler.store;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.tuple.Pair;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class ProfitCategoryDataHandler extends Handler {
    private static ProfitCategoryDataHandler INSTANCE = new ProfitCategoryDataHandler();

    public static ProfitCategoryDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfitCategoryDataHandler();
        }
        return INSTANCE;
    }

    private ProfitCategoryDataModel profitCategoryData = new ProfitCategoryDataModel();
    private boolean needsUpdate = false;

    public ProfitCategoryDataModel getProfitCategoryData() {
        return profitCategoryData;
    }

    public void setProfitCategoryData(ProfitCategoryDataModel profitCategoryData) {
        this.profitCategoryData = ProfitCategoryDataModel.sanitize(profitCategoryData);
        this.updateProfitCategoryData();
    }

    private void updateProfitCategoryData() {
        if (needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.PROFIT_CATEGORY_RULES);
        }
        this.needsUpdate = false;
    }

    @Override
    public void tick() {
        if (profitCategoryData.uuid == null && minecraft.player != null) {
            profitCategoryData.uuid = minecraft.player.getUUID();
        } else if (profitCategoryData.uuid != null && this.needsUpdate) {
            this.updateProfitCategoryData();
        } else if (!ProfitCategoryDataModel.PROFIT_CATEGORY_DATA_MODEL_VERSION.equals(profitCategoryData.version)) {
            profitCategoryData.version = ProfitCategoryDataModel.PROFIT_CATEGORY_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    @Override
    public void init() {
        if (minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
        this.profitCategoryData = ProfitCategoryDataModel.sanitize(this.profitCategoryData);
    }

    private void setUUID(UUID uuid) {
        this.profitCategoryData.uuid = uuid;
    }

    public boolean reloadFromFile() {
        if (profitCategoryData.uuid == null && minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
        if (profitCategoryData.uuid == null) {
            return false;
        }
        return DataFileHandler.instance().loadDataToMemory(DataModels.DataModelType.PROFIT_CATEGORY_RULES);
    }

    public Path getCategoryFilePath() {
        Path dataDirectory = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FishOnMCExtras.MOD_ID)
                .resolve("data");
        if (profitCategoryData.uuid == null) {
            return dataDirectory.resolve(DataModels.DataModelType.PROFIT_CATEGORY_RULES.FILENAME + ".json");
        }
        return dataDirectory
                .resolve(profitCategoryData.uuid.toString())
                .resolve(DataModels.DataModelType.PROFIT_CATEGORY_RULES.FILENAME + ".json");
    }

    public CategoryRule findOverride(TagObject tag) {
        CategoryRule byItemId = findByItemId(tag);
        if (byItemId != null) {
            return byItemId;
        }

        String combinedText = combinedText(tag);
        for (NamePatternRule rule : profitCategoryData.namePattern) {
            if (rule == null || rule.regex == null) {
                continue;
            }
            if (rule.regex.matcher(combinedText).find()) {
                if (isFishForCategoryRules(tag) && namePatternWouldLeaveFishCategory(rule)) {
                    continue;
                }
                return rule.toCategoryRule();
            }
        }

        if (skipsRarityMapOverride(tag)) {
            return null;
        }

        String rarity = normalizeKey(tag.getRarity());
        if (!rarity.isBlank()) {
            for (Map.Entry<String, CategoryRule> entry : profitCategoryData.rarityMap.entrySet()) {
                if (normalizeKey(entry.getKey()).equals(rarity)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Fish and shards get their own breakdown categories from heuristics; {@code rarityMap} is for other gear
     * so epic+ fish/shards are not forced into Rare Items.
     */
    private static boolean skipsRarityMapOverride(TagObject tag) {
        return isFishForCategoryRules(tag) || isShardForCategoryRules(tag);
    }

    private static boolean isFishForCategoryRules(TagObject tag) {
        return "fish".equalsIgnoreCase(tag.getType()) || ValidateItem.isFish(tag.getItemStack()).value1();
    }

    /** Matches {@link dannypx.foe.handler.logic.ProfitSourceClassifier} shard heuristic (name contains "shard", not fish). */
    private static boolean isShardForCategoryRules(TagObject tag) {
        if (isFishForCategoryRules(tag)) {
            return false;
        }
        String name = tag.getName().getString();
        if (name == null || name.isBlank()) {
            return false;
        }
        String normalized = name.trim().replaceAll("[_\\-]+", " ").replaceAll("\\s+", " ").toLowerCase(Locale.US);
        return normalized.contains("shard");
    }

    /**
     * Rare-item (and similar) name rules match on name + lore; fish lore can contain overlapping words.
     * Only apply a pattern to fish when it keeps category {@code fish} (blank category = sub-only override).
     */
    private static boolean namePatternWouldLeaveFishCategory(NamePatternRule rule) {
        String category = rule.category;
        if (category == null || category.isBlank()) {
            return false;
        }
        return !normalizeKey(category).equals("fish");
    }

    private CategoryRule findByItemId(TagObject tag) {
        Identifier id = BuiltInRegistries.ITEM.getKey(tag.getItemStack().getItem());
        String key = id.toString();
        CategoryRule exact = profitCategoryData.byItemId.get(key);
        if (exact != null) {
            return exact;
        }

        String normalizedKey = normalizeKey(key);
        for (Map.Entry<String, CategoryRule> entry : profitCategoryData.byItemId.entrySet()) {
            if (normalizeKey(entry.getKey()).equals(normalizedKey)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String combinedText(TagObject tag) {
        StringBuilder builder = new StringBuilder(tag.getName().getString());
        tag.getLore().forEach(line -> builder.append('\n').append(line.getString()));
        return builder.toString();
    }

    private static String normalizeKey(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
    }

    public static class ProfitCategoryDataModel extends DataModels.DataModel {
        private static final String PROFIT_CATEGORY_DATA_MODEL_VERSION = "0.2";

        public Map<String, CategoryRule> byItemId = new LinkedHashMap<>();
        public List<NamePatternRule> namePattern = new ArrayList<>();
        public Map<String, CategoryRule> rarityMap = new LinkedHashMap<>();

        public ProfitCategoryDataModel() {
            super(PROFIT_CATEGORY_DATA_MODEL_VERSION, null);
        }

        private static ProfitCategoryDataModel sanitize(ProfitCategoryDataModel input) {
            if (input == null) {
                input = new ProfitCategoryDataModel();
            }

            ProfitCategoryDataModel sanitized = new ProfitCategoryDataModel();
            sanitized.version = input.version == null ? PROFIT_CATEGORY_DATA_MODEL_VERSION : input.version;
            sanitized.uuid = input.uuid;
            sanitized.byItemId = input.byItemId == null ? new LinkedHashMap<>() : new LinkedHashMap<>(input.byItemId);
            sanitized.namePattern = input.namePattern == null ? new ArrayList<>() : new ArrayList<>(input.namePattern);
            sanitized.rarityMap = input.rarityMap == null ? new LinkedHashMap<>() : new LinkedHashMap<>(input.rarityMap);
            if (isUnsetTemplate(sanitized)) {
                applyDefaultCategoryRules(sanitized);
            }
            return sanitized;
        }

        private static boolean isUnsetTemplate(ProfitCategoryDataModel m) {
            return m.byItemId.isEmpty() && m.namePattern.isEmpty() && m.rarityMap.isEmpty();
        }

        private static void applyDefaultCategoryRules(ProfitCategoryDataModel m) {
            // Optional overrides for the profit breakdown; heuristics still apply when no rule matches.
            NamePatternRule lightning = new NamePatternRule();
            lightning.regex = Pattern.compile("(?i)lightning\\s+in\\s+a\\s+bottle");
            lightning.category = "rare_items";
            lightning.sub = "Lightning in a Bottle";
            m.namePattern.add(lightning);

            String[] rareKeys = { "epic", "legendary", "mythic", "mythical", "fabled" };
            for (String r : rareKeys) {
                String sub = r.length() <= 1 ? r : (Character.toUpperCase(r.charAt(0)) + r.substring(1).toLowerCase(Locale.US));
                m.rarityMap.put(r, new CategoryRule("rare_items", sub));
            }
        }
    }

    public static class CategoryRule {
        public String category;
        public String sub;

        public CategoryRule() {}

        public CategoryRule(String category, String sub) {
            this.category = category;
            this.sub = sub;
        }
    }

    public static class NamePatternRule {
        public Pattern regex;
        public String category;
        public String sub;

        public NamePatternRule() {}

        public CategoryRule toCategoryRule() {
            return new CategoryRule(category, sub);
        }
    }

    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "itemRules", Pair.of(Component.literal(Integer.toString(profitCategoryData.byItemId.size())), Component.empty()),
                "namePatternRules", Pair.of(Component.literal(Integer.toString(profitCategoryData.namePattern.size())), Component.empty()),
                "rarityRules", Pair.of(Component.literal(Integer.toString(profitCategoryData.rarityMap.size())), Component.empty())
        );
    }
}
