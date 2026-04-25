package dannypx.foe.handler.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.*;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.type_adapter.CustomTimerAdapter;
import dannypx.foe.type.type_adapter.ItemStackAdapter;
import dannypx.foe.type.type_adapter.PatternAdapter;
import dannypx.foe.type.type_adapter.ComponentAdapter;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class DataFileHandler extends Handler {
    private static DataFileHandler INSTANCE = new DataFileHandler();

    public static DataFileHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new DataFileHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final Path DATA_FOLDER = Path.of("data");
    private boolean isDataLoaded = false;

    public boolean isDataLoaded() {
        return isDataLoaded;
    }
    //endregion

    //region Methods
    public void tick() {
        ProfileDataHandler.instance().tick();
        StatsDataHandler.instance().tick();
        ConstantDataHandler.instance().tick();
        QuestDataHandler.instance().tick();
        CrewDataHandler.instance().tick();
        ProfitPricingDataHandler.instance().tick();
        ProfitCategoryDataHandler.instance().tick();
        ProfitRewardTriggerDataHandler.instance().tick();
        CustomHudDataHandler.instance().tick();
        CustomButtonDataHandler.instance().tick();
        CustomNotificationDataHandler.instance().tick();
        CustomChatTriggerDataHandler.instance().tick();
        CustomTimerDataHandler.instance().tick();
    }

    public void init() {
        for (DataModels.DataModelType value : DataModels.DataModelType.values()) {
            this.loadDataToMemory(value);
        }
    }

    public boolean loadDataToMemory(DataModels.DataModelType dataModelType) {
        DataModels.DataModel data = this.getData(dataModelType);
        if (data.uuid == null) {
            return false;
        }
        try {
            Path configDir = getConfigDir(data.uuid);
            Files.createDirectories(configDir);
            Path filePath = configDir.resolve(dataModelType.FILENAME + ".json");
            if(!checkIfFileExist(filePath)) {
                Files.createFile(filePath);
                this.isDataLoaded = true;
                return this.saveToFile(dataModelType);
            }
            String jsonFromFile = Files.readString(filePath);
            setData(dataModelType, jsonFromFile);
            this.isDataLoaded = true;
            return true;

        } catch (Exception e) {
            LoggerHandler.error(e);
        }
        return false;
    }

    public boolean saveToFile(DataModels.DataModelType dataModelType) {
        DataModels.DataModel data = this.getData(dataModelType);
        try {
            Path configDir = getConfigDir(data.uuid);
            Files.createDirectories(configDir);
            Path filePath = configDir.resolve(dataModelType.FILENAME + ".json");
            String resultJson = dataModelToJson(data);
            Files.writeString(filePath, resultJson);

            LoggerHandler._debug("Updating file: " + dataModelType.FILENAME + ".json");
        } catch (IOException e) {
            LoggerHandler.error(e);
        }
        return true;
    }

    private Path getConfigDir(UUID uuid) {
        return FabricLoader
                .getInstance()
                .getConfigDir()
                .resolve(FishOnMCExtras.MOD_ID)
                .resolve(DATA_FOLDER)
                .resolve(uuid.toString());
    }

    private boolean checkIfFileExist(Path filePath) {
        return Files.exists(filePath);
    }

    private String dataModelToJson(DataModels.DataModel dataModel) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(Component.class, new ComponentAdapter())
                .registerTypeAdapter(Pattern.class, new PatternAdapter())
                .registerTypeAdapter(CustomTimerDataHandler.CustomTimer.class, new CustomTimerAdapter())
                .create();
        return gson.toJson(dataModel);
    }

    private DataModels.DataModel getData(DataModels.DataModelType dataModelType) {
        return switch (dataModelType) {
            case PROFILE_DATA -> ProfileDataHandler.instance().getProfileData();
            case STATS_DATA -> StatsDataHandler.instance().getStatsData();
            case CONSTANT_DATA -> ConstantDataHandler.instance().getConstantData();
            case QUEST_DATA -> QuestDataHandler.instance().getQuestData();
            case CREW_DATA -> CrewDataHandler.instance().getCrewData();
            case PROFIT_PRICING -> ProfitPricingDataHandler.instance().getProfitPricingData();
            case PROFIT_CATEGORY_RULES -> ProfitCategoryDataHandler.instance().getProfitCategoryData();
            case PROFIT_REWARD_TRIGGERS -> ProfitRewardTriggerDataHandler.instance().getProfitRewardTriggerData();
            case CUSTOM_HUD_DATA -> CustomHudDataHandler.instance().getCustomHudData();
            case CUSTOM_BUTTON_DATA -> CustomButtonDataHandler.instance().getCustomButtonData();
            case CUSTOM_NOTIFICATION_DATA -> CustomNotificationDataHandler.instance().getCustomNotificationData();
            case CUSTOM_CHAT_TRIGGER_DATA -> CustomChatTriggerDataHandler.instance().getCustomChatTriggerData();
            case CUSTOM_TIMER_DATA -> CustomTimerDataHandler.instance().getCustomTimerData();
        };
    }

    private void setData(DataModels.DataModelType dataModelType, String json) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
                .registerTypeAdapter(Component.class, new ComponentAdapter())
                .registerTypeAdapter(Pattern.class, new PatternAdapter())
                .registerTypeAdapter(CustomTimerDataHandler.CustomTimer.class, new CustomTimerAdapter())
                .create();

        LoggerHandler._debug("Setting data from: " + dataModelType.FILENAME + ".json");

        switch (dataModelType) {
            case PROFILE_DATA ->
                    ProfileDataHandler.instance().setProfileData(gson.fromJson(json, ProfileDataHandler.ProfileDataModel.class));
            case STATS_DATA ->
                    StatsDataHandler.instance().setStatsData(gson.fromJson(json, StatsDataHandler.StatsDataModel.class));
            case CONSTANT_DATA ->
                    ConstantDataHandler.instance().setConstantData(gson.fromJson(json, ConstantDataHandler.ConstantDataModel.class));
            case QUEST_DATA ->
                    QuestDataHandler.instance().setQuestData(gson.fromJson(json, QuestDataHandler.QuestDataModel.class));
            case CREW_DATA ->
                    CrewDataHandler.instance().setCrewData(gson.fromJson(json, CrewDataHandler.CrewDataModel.class));
            case PROFIT_PRICING ->
                    ProfitPricingDataHandler.instance().setProfitPricingData(gson.fromJson(json, ProfitPricingDataHandler.ProfitPricingDataModel.class));
            case PROFIT_CATEGORY_RULES ->
                    ProfitCategoryDataHandler.instance().setProfitCategoryData(gson.fromJson(json, ProfitCategoryDataHandler.ProfitCategoryDataModel.class));
            case PROFIT_REWARD_TRIGGERS ->
                    ProfitRewardTriggerDataHandler.instance().setProfitRewardTriggerData(gson.fromJson(json, ProfitRewardTriggerDataHandler.ProfitRewardTriggerDataModel.class));
            case CUSTOM_HUD_DATA ->
                    CustomHudDataHandler.instance().setCustomHudData(gson.fromJson(json, CustomHudDataHandler.CustomHudDataModel.class));
            case CUSTOM_BUTTON_DATA ->
                    CustomButtonDataHandler.instance().setCustomButtonData(gson.fromJson(json, CustomButtonDataHandler.CustomButtonDataModel.class));
            case CUSTOM_NOTIFICATION_DATA ->
                    CustomNotificationDataHandler.instance().setCustomNotificationData(gson.fromJson(json, CustomNotificationDataHandler.CustomNotificationDataModel.class));
            case CUSTOM_CHAT_TRIGGER_DATA ->
                    CustomChatTriggerDataHandler.instance().setCustomChatTriggerData(gson.fromJson(json, CustomChatTriggerDataHandler.CustomChatTriggerDataModel.class));
            case CUSTOM_TIMER_DATA ->
                    CustomTimerDataHandler.instance().setCustomTimerData(gson.fromJson(json, CustomTimerDataHandler.CustomTimerDataModel.class));
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "isDataLoaded", Pair.of(Component.literal(isDataLoaded() ? "true" : "false"), Component.empty())
        );
    }
    //endregion
}
