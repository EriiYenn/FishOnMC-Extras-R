package dannypx.foe.handler.store;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.type.tuple.Pair;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class ProfitPricingDataHandler extends Handler {
    private static ProfitPricingDataHandler INSTANCE = new ProfitPricingDataHandler();

    public static ProfitPricingDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfitPricingDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private ProfitPricingDataModel profitPricingData = new ProfitPricingDataModel();
    private boolean needsUpdate = false;

    public ProfitPricingDataModel getProfitPricingData() {
        return profitPricingData;
    }

    public void setProfitPricingData(ProfitPricingDataModel profitPricingData) {
        this.profitPricingData = ProfitPricingDataModel.sanitize(profitPricingData);
        this.updateProfitPricingData();
    }

    private void updateProfitPricingData() {
        if (needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.PROFIT_PRICING);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    @Override
    public void tick() {
        if (profitPricingData.uuid == null && minecraft.player != null) {
            profitPricingData.uuid = minecraft.player.getUUID();
        } else if (profitPricingData.uuid != null && this.needsUpdate) {
            this.updateProfitPricingData();
        } else if (!ProfitPricingDataModel.PROFIT_PRICING_DATA_MODEL_VERSION.equals(profitPricingData.version)) {
            profitPricingData.version = ProfitPricingDataModel.PROFIT_PRICING_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    @Override
    public void init() {
        if (minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
    }

    private void setUUID(UUID uuid) {
        this.profitPricingData.uuid = uuid;
    }

    public boolean reloadFromFile() {
        if (profitPricingData.uuid == null && minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
        if (profitPricingData.uuid == null) {
            return false;
        }
        return DataFileHandler.instance().loadDataToMemory(DataModels.DataModelType.PROFIT_PRICING);
    }

    public Path getPricingDirectory() {
        Path dataDirectory = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FishOnMCExtras.MOD_ID)
                .resolve("data");
        if (profitPricingData.uuid == null) {
            return dataDirectory;
        }
        return dataDirectory.resolve(profitPricingData.uuid.toString());
    }

    public Path getPricingFilePath() {
        return getPricingDirectory().resolve(DataModels.DataModelType.PROFIT_PRICING.FILENAME + ".json");
    }

    public float getValue(ItemStack itemStack) {
        float byItemId = this.getValueByItemId(itemStack);
        if (byItemId > 0f) {
            return byItemId;
        }
        return this.getValueByName(itemStack.getHoverName().getString());
    }

    private float getValueByItemId(ItemStack itemStack) {
        Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        String exactKey = id.toString();
        Float exactMatch = profitPricingData.byItemId.get(exactKey);
        if (exactMatch != null && exactMatch > 0f) {
            return exactMatch;
        }

        String normalizedKey = normalizeKey(exactKey);
        for (Map.Entry<String, Float> entry : profitPricingData.byItemId.entrySet()) {
            if (normalizeKey(entry.getKey()).equals(normalizedKey) && entry.getValue() != null && entry.getValue() > 0f) {
                return entry.getValue();
            }
        }
        return 0f;
    }

    private float getValueByName(String label) {
        Float exactMatch = profitPricingData.byName.get(label);
        if (exactMatch != null && exactMatch > 0f) {
            return exactMatch;
        }

        String normalizedLabel = normalizeKey(label);
        for (Map.Entry<String, Float> entry : profitPricingData.byName.entrySet()) {
            if (normalizeKey(entry.getKey()).equals(normalizedLabel) && entry.getValue() != null && entry.getValue() > 0f) {
                return entry.getValue();
            }
        }
        return 0f;
    }

    private static String normalizeKey(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.US);
    }
    //endregion

    //region Model
    public static class ProfitPricingDataModel extends DataModels.DataModel {
        private static final String PROFIT_PRICING_DATA_MODEL_VERSION = "0.1";

        public Map<String, Float> byItemId = new LinkedHashMap<>();
        public Map<String, Float> byName = defaultByName();

        public ProfitPricingDataModel() {
            super(PROFIT_PRICING_DATA_MODEL_VERSION, null);
        }

        private static ProfitPricingDataModel sanitize(ProfitPricingDataModel input) {
            if (input == null) {
                return new ProfitPricingDataModel();
            }

            ProfitPricingDataModel sanitized = new ProfitPricingDataModel();
            sanitized.version = input.version == null ? PROFIT_PRICING_DATA_MODEL_VERSION : input.version;
            sanitized.uuid = input.uuid;
            sanitized.byItemId = input.byItemId == null ? new LinkedHashMap<>() : new LinkedHashMap<>(input.byItemId);
            sanitized.byName = input.byName == null ? defaultByName() : new LinkedHashMap<>(input.byName);
            return sanitized;
        }

        private static LinkedHashMap<String, Float> defaultByName() {
            LinkedHashMap<String, Float> defaults = new LinkedHashMap<>();
            defaults.put("Lightning in a Bottle", 50_000f);
            defaults.put("Subtropical Shard", 0f);
            defaults.put("Savanna Shard", 0f);
            return defaults;
        }
    }
    //endregion

    //region Dev
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "configuredNames", Pair.of(Component.literal(Integer.toString(profitPricingData.byName.size())), Component.empty()),
                "configuredItemIds", Pair.of(Component.literal(Integer.toString(profitPricingData.byItemId.size())), Component.empty())
        );
    }
    //endregion
}
