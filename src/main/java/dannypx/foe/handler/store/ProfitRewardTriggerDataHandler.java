package dannypx.foe.handler.store;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.type.tuple.Pair;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ProfitRewardTriggerDataHandler extends Handler {
    private static ProfitRewardTriggerDataHandler INSTANCE = new ProfitRewardTriggerDataHandler();

    public static ProfitRewardTriggerDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfitRewardTriggerDataHandler();
        }
        return INSTANCE;
    }

    private ProfitRewardTriggerDataModel profitRewardTriggerData = new ProfitRewardTriggerDataModel();
    private boolean needsUpdate = false;

    public ProfitRewardTriggerDataModel getProfitRewardTriggerData() {
        return profitRewardTriggerData;
    }

    public void setProfitRewardTriggerData(ProfitRewardTriggerDataModel profitRewardTriggerData) {
        this.profitRewardTriggerData = ProfitRewardTriggerDataModel.sanitize(profitRewardTriggerData);
        this.updateProfitRewardTriggerData();
    }

    private void updateProfitRewardTriggerData() {
        if (needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.PROFIT_REWARD_TRIGGERS);
        }
        this.needsUpdate = false;
    }

    @Override
    public void tick() {
        if (profitRewardTriggerData.uuid == null && minecraft.player != null) {
            profitRewardTriggerData.uuid = minecraft.player.getUUID();
        } else if (profitRewardTriggerData.uuid != null && this.needsUpdate) {
            this.updateProfitRewardTriggerData();
        } else if (!ProfitRewardTriggerDataModel.PROFIT_REWARD_TRIGGER_DATA_MODEL_VERSION.equals(profitRewardTriggerData.version)) {
            profitRewardTriggerData.version = ProfitRewardTriggerDataModel.PROFIT_REWARD_TRIGGER_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    @Override
    public void init() {
        if (minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
        this.profitRewardTriggerData = ProfitRewardTriggerDataModel.sanitize(this.profitRewardTriggerData);
    }

    private void setUUID(UUID uuid) {
        this.profitRewardTriggerData.uuid = uuid;
    }

    public boolean reloadFromFile() {
        if (profitRewardTriggerData.uuid == null && minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
        }
        if (profitRewardTriggerData.uuid == null) {
            return false;
        }
        return DataFileHandler.instance().loadDataToMemory(DataModels.DataModelType.PROFIT_REWARD_TRIGGERS);
    }

    public Path getRewardTriggerFilePath() {
        Path dataDirectory = FabricLoader.getInstance()
                .getConfigDir()
                .resolve(FishOnMCExtras.MOD_ID)
                .resolve("data");
        if (profitRewardTriggerData.uuid == null) {
            return dataDirectory.resolve(DataModels.DataModelType.PROFIT_REWARD_TRIGGERS.FILENAME + ".json");
        }
        return dataDirectory
                .resolve(profitRewardTriggerData.uuid.toString())
                .resolve(DataModels.DataModelType.PROFIT_REWARD_TRIGGERS.FILENAME + ".json");
    }

    public static class ProfitRewardTriggerDataModel extends DataModels.DataModel {
        private static final String PROFIT_REWARD_TRIGGER_DATA_MODEL_VERSION = "0.3";

        public List<Pattern> competitionOpenPatterns = new ArrayList<>();
        public List<Pattern> questOpenPatterns = new ArrayList<>();
        public List<Pattern> questMoneyPatterns = new ArrayList<>();
        public List<Pattern> competitionMoneyPatterns = new ArrayList<>();

        public ProfitRewardTriggerDataModel() {
            super(PROFIT_REWARD_TRIGGER_DATA_MODEL_VERSION, null);
        }

        private static ProfitRewardTriggerDataModel sanitize(ProfitRewardTriggerDataModel input) {
            if (input == null) {
                input = new ProfitRewardTriggerDataModel();
            }

            ProfitRewardTriggerDataModel sanitized = new ProfitRewardTriggerDataModel();
            sanitized.version = input.version == null ? PROFIT_REWARD_TRIGGER_DATA_MODEL_VERSION : input.version;
            sanitized.uuid = input.uuid;
            sanitized.competitionOpenPatterns = input.competitionOpenPatterns == null ? new ArrayList<>() : new ArrayList<>(input.competitionOpenPatterns);
            if (sanitized.competitionOpenPatterns.isEmpty()) {
                // Fish on MC: system chat, e.g. "FISHING CONTEST START (30m)"; placement payout block uses "FISHING CONTEST PLACEMENT".
                // Do not use broad "You received $" as open patterns: player PAY lines look different but money defaults stay line-only.
                sanitized.competitionOpenPatterns.add(Pattern.compile("FISHING CONTEST START"));
                sanitized.competitionOpenPatterns.add(Pattern.compile("FISHING CONTEST PLACEMENT"));
            }
            sanitized.questOpenPatterns = input.questOpenPatterns == null ? new ArrayList<>() : new ArrayList<>(input.questOpenPatterns);
            if (sanitized.questOpenPatterns.isEmpty()) {
                // Fish on MC: e.g. "QUEST Complete [#4]" (per-profile quest index).
                sanitized.questOpenPatterns.add(Pattern.compile("QUEST\\s+Complete"));
            }
            sanitized.questMoneyPatterns = input.questMoneyPatterns == null ? new ArrayList<>() : new ArrayList<>(input.questMoneyPatterns);
            if (sanitized.questMoneyPatterns.isEmpty()) {
                // Fish on MC: separate system chat line after "QUEST Complete", e.g. " + $3.41K" (not 12.86Kxp — no $).
                sanitized.questMoneyPatterns.add(Pattern.compile("^\\s*\\+\\s*\\$[0-9.,]+[KkMm]?$"));
            }
            sanitized.competitionMoneyPatterns = input.competitionMoneyPatterns == null ? new ArrayList<>() : new ArrayList<>(input.competitionMoneyPatterns);
            if (sanitized.competitionMoneyPatterns.isEmpty()) {
                // Fish on MC: after "FISHING CONTEST PLACEMENT" / "Rewards:" — a line that is only money, e.g. " $25K" (not " + $…" and not 2.5Klxp).
                sanitized.competitionMoneyPatterns.add(Pattern.compile("^\\s*\\$[0-9.,]+[KkMm]?$"));
            }
            return sanitized;
        }
    }

    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "competitionOpenPatterns", Pair.of(Component.literal(Integer.toString(profitRewardTriggerData.competitionOpenPatterns.size())), Component.empty()),
                "questOpenPatterns", Pair.of(Component.literal(Integer.toString(profitRewardTriggerData.questOpenPatterns.size())), Component.empty()),
                "questMoneyPatterns", Pair.of(Component.literal(Integer.toString(profitRewardTriggerData.questMoneyPatterns.size())), Component.empty()),
                "competitionMoneyPatterns", Pair.of(Component.literal(Integer.toString(profitRewardTriggerData.competitionMoneyPatterns.size())), Component.empty())
        );
    }
}
