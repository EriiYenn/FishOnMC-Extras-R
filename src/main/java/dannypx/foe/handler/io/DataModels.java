package dannypx.foe.handler.io;

import java.util.UUID;

public class DataModels {

    public static abstract class DataModel {
        public String version;
        public UUID uuid;

        protected DataModel(String version, UUID uuid) {
            this.version = version;
            this.uuid = uuid;
        }
    }

    public enum DataModelType {
        PROFILE_DATA("profile"),
        STATS_DATA("stats"),
        CONSTANT_DATA("constant"),
        QUEST_DATA("quest"),
        CREW_DATA("crew"),
        PROFIT_PRICING("profit_pricing"),
        PROFIT_CATEGORY_RULES("profit_categories"),
        PROFIT_REWARD_TRIGGERS("profit_reward_triggers"),
        CUSTOM_HUD_DATA("custom_hud"),
        CUSTOM_BUTTON_DATA("custom_button"),
        CUSTOM_NOTIFICATION_DATA("custom_notification"),
        CUSTOM_CHAT_TRIGGER_DATA("custom_chat_trigger"),
        CUSTOM_TIMER_DATA("custom_timer");

        public final String FILENAME;

        DataModelType(String fileName) {
            FILENAME = fileName;
        }
    }
}
