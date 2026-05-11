package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.BossEventHandler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.logic.ProfitRewardAttributionHandler;
import dannypx.foe.handler.fetch.QuestScreenHandler;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.FishTagObject;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.placeholder.ComponentValue;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class QuestDataHandler extends Handler {
    private static QuestDataHandler INSTANCE = new QuestDataHandler();

    public static QuestDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new QuestDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private QuestDataModel questData = new QuestDataModel();
    private boolean needsUpdate = false;

    public QuestDataModel getQuestData() {
        return questData;
    }

    public Pair<Boolean, PlaceholderValue> getQuestData(String[] params) {
        if(params.length > 0) {
            Pattern intPattern = Pattern.compile("^-?\\d+$");
            Pattern questPattern = Pattern.compile("^(goal|max|current)$");

            if(Objects.equals(params[0], "data")
                    && params.length == 3
                    && intPattern.matcher(params[1]).matches()
                    && questPattern.matcher(params[2]).matches()
            ) {
                String location = BossEventHandler.instance().getLocation().getString();
                List<Quest> questData = this.getQuestData().questList.getOrDefault(location, new ArrayList<>());
                int index = Integer.parseInt(params[1]);
                if(questData.size() > index) {
                    return switch (params[2]) {
                        case "goal" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(ConstantDataHandler.instance().getConstantFishComponent(questData.get(index).goal)));
                        case "max" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(questData.get(index).max)));
                        case "current" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(questData.get(index).current)));
                        default -> PlaceholderHandler.noResult();
                    };
                }
            }
        }
        return PlaceholderHandler.noResult();
    }

    public void setQuestData(QuestDataModel questData) {
        this.questData = questData;
        this.updateQuestData();
    }

    private void updateQuestData() {
        if(needsUpdate) {
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.QUEST_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(questData.uuid == null && minecraft.player != null) {
            questData.uuid = minecraft.player.getUUID();
        } else if(questData.uuid != null && this.needsUpdate) {
            this.updateQuestData();
        } else if(!QuestDataModel.QUEST_DATA_MODEL_VERSION.equals(questData.version)) {
            questData.version = QuestDataModel.QUEST_DATA_MODEL_VERSION;
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraft.player != null) this.setUUID(minecraft.player.getUUID());
    }

    private void setUUID(UUID uuid) {
        this.questData.uuid = uuid;
    }

    public void setQuest(List<Quest> questList) {
        String location = BossEventHandler.instance().getLocation().getString();

        questData.questList.put(location, questList);
        LoggerHandler._debug("Quests updated");
        this.needsUpdate = true;
    }

    public void setFish(FishTagObject fishNbtObject) {
        boolean completedQuest = false;
        for (Quest quest : questData.questList.getOrDefault(BossEventHandler.instance().getLocation().getString(), new ArrayList<>())) {
            if(Objects.equals(quest.goal, fishNbtObject.getFishSize()) || Objects.equals(quest.goal, fishNbtObject.getRarity())) {
                int previous = quest.current;
                quest.addCurrent();
                this.needsUpdate = true;
                if (previous < quest.max && quest.current == quest.max) {
                    completedQuest = true;
                }
            }
        }
        if (completedQuest) {
            ProfitRewardAttributionHandler.instance().openQuestContext();
        }
        QuestScreenHandler.instance().checkForCompletedQuests();
    }
    //endregion

    //region Model
    public static class QuestDataModel extends DataModels.DataModel {
        private static final String QUEST_DATA_MODEL_VERSION = "0.2";

        // Location, Quests
        public Map<String, List<Quest>> questList = new HashMap<>();

        public QuestDataModel() {
            super(QUEST_DATA_MODEL_VERSION, null);
        }

    }
    //endregion

    //region Quest Object
    public static class Quest {
        public final String goal;
        public final int max;
        public int current;

        public Quest(String goal, int max, int current) {
            this.goal = goal;
            this.max = max;
            this.current = current;
        }

        public void addCurrent() {
            if(this.current < this.max) {
                this.current++;
            }
        }

        public boolean isDone() {
            return current == max;
        }
    }
    //

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "questData", Pair.of(Component.literal("[questData]"), ComponentHelper.literal(getQuestData()))
        );
    }
    //endregion
}
