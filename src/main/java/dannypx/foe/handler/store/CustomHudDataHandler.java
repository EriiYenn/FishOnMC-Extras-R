package dannypx.foe.handler.store;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.io.DataFileHandler;
import dannypx.foe.handler.io.DataModels;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.Alignment;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CustomHudDataHandler extends Handler {
    private static CustomHudDataHandler INSTANCE = new CustomHudDataHandler();

    public static CustomHudDataHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new CustomHudDataHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private CustomHudDataModel customHudData = new CustomHudDataModel();

    private boolean needsUpdate = false;
    public boolean needsRenderUpdate = false;

    public CustomHudDataModel getCustomHudData() {
        return customHudData;
    }

    public void setCustomHudData(CustomHudDataModel customHudData) {
        this.customHudData = customHudData;
        this.needsRenderUpdate = true;
        this.updateCustomHudData();
    }

    private void updateCustomHudData() {
        if(needsUpdate) {
            needsRenderUpdate = true;
            DataFileHandler.instance().saveToFile(DataModels.DataModelType.CUSTOM_HUD_DATA);
        }
        this.needsUpdate = false;
    }
    //endregion

    //region Methods
    public void tick() {
        if(customHudData.uuid == null && minecraft.player != null) {
            customHudData.uuid = minecraft.player.getUUID();
        } else if(customHudData.uuid != null && this.needsUpdate) {
            this.updateCustomHudData();
        } else if(!CustomHudDataModel.CUSTOM_HUD_DATA_MODEL_VERSION.equals(customHudData.version)) {
            customHudData.version = CustomHudDataModel.CUSTOM_HUD_DATA_MODEL_VERSION;
            this.updateDefault();
            needsUpdate = true;
        }
    }

    public void init() {
        if(minecraft.player != null) {
            this.setUUID(minecraft.player.getUUID());
            needsRenderUpdate = true;
        }
    }

    private void setUUID(UUID uuid) {
        this.customHudData.uuid = uuid;
    }

    public void createNewCustomHud(String id) {
        customHudData.customHudRawDataList.put(id, new CustomHud());
        needsUpdate = true;
    }

    public void createNewCustomHud(String id, CustomHud customHud) {
        customHudData.customHudRawDataList.put(id, customHud);
        needsUpdate = true;
    }

    public CustomHud deleteCustomHud(String id) {
        needsUpdate = true;
        return customHudData.customHudRawDataList.remove(id);
    }

    public void updateHud(String currentSelectedHud, String newName, float scale, boolean showBackground, boolean showElement, List<Triplet<String, Boolean, Boolean>> list) {
        CustomHud newHud = customHudData.customHudRawDataList.get(currentSelectedHud);

        if(!Objects.equals(currentSelectedHud, newName)) {
            newHud = deleteCustomHud(currentSelectedHud);
            currentSelectedHud = newName;
        }

        newHud.stringLines = list;
        newHud.scale = scale;
        newHud.showBackground = showBackground;
        newHud.showElement = showElement;

        customHudData.customHudRawDataList.put(currentSelectedHud, newHud);
        needsUpdate = true;
    }

    public void updateHud(String currentSelectedHud, int xPercent, int yPercent, Alignment alignment) {
        CustomHud newHud = customHudData.customHudRawDataList.get(currentSelectedHud);

        newHud.xPos = xPercent;
        newHud.yPos = yPercent;
        newHud.alignment = alignment;

        customHudData.customHudRawDataList.put(currentSelectedHud, newHud);
        needsUpdate = true;
    }

    public void updateDefault() {
        CustomHudDataModel.defaultHuds.forEach((key, timer) -> {
            customHudData.customHudRawDataList.putIfAbsent(key, timer);
        });
    }

    public void fixDefault() {
        customHudData.customHudRawDataList.putAll(CustomHudDataModel.defaultHuds);
        needsUpdate = true;
    }

    public void resetHuds() {
        customHudData.customHudRawDataList = new HashMap<>(CustomHudDataModel.defaultHuds);

        needsUpdate = true;
    }
    //endregion

    //region Model
    public static class CustomHudDataModel extends DataModels.DataModel {
        private static final String CUSTOM_HUD_DATA_MODEL_VERSION = "0.3";

        private static final Map<String, CustomHud> defaultHuds = Map.of(
                "Quest Hud",
                new CustomHud(new ArrayList<>(Arrays.asList(
                        Triplet.of("%is_not_blank.(<quest_data.data.0.goal>)%&7&l- &fQuests &7-", true, true),
                        Triplet.of("%is_not_blank.(<quest_data.data.0.goal>)%", false, false),
                        Triplet.of("%quest_data.data.0.goal% &e%quest_data.data.0.current%&7/&f%quest_data.data.0.max%", false, true),
                        Triplet.of("%quest_data.data.1.goal% &e%quest_data.data.1.current%&7/&f%quest_data.data.1.max%", false, true),
                        Triplet.of("%quest_data.data.2.goal% &e%quest_data.data.2.current%&7/&f%quest_data.data.2.max%", false, true),
                        Triplet.of("%quest_data.data.3.goal% &e%quest_data.data.3.current%&7/&f%quest_data.data.3.max%", false, true),
                        Triplet.of("%quest_data.data.4.goal% &e%quest_data.data.4.current%&7/&f%quest_data.data.4.max%", false, true),
                        Triplet.of("%quest_data.data.5.goal% &e%quest_data.data.5.current%&7/&f%quest_data.data.5.max%", false, true),
                        Triplet.of("%quest_data.data.6.goal% &e%quest_data.data.6.current%&7/&f%quest_data.data.6.max%", false, true),
                        Triplet.of("%quest_data.data.7.goal% &e%quest_data.data.7.current%&7/&f%quest_data.data.7.max%", false, true)
                )),
                        Alignment.TOP_RIGHT,
                        1,
                        15,
                        1.0f,
                        true,
                        true
                ),
                "Contest Hud",
                new CustomHud(new ArrayList<>(Arrays.asList(
                        Triplet.of("&7&l- &fContest &7-", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=false)%", false, false),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=false)%&fStarts in: &e%timer.Contest Timer.time.off.minute%&7:&e%timer.Contest Timer.time.off.second%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%", false, false),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)% %substring_back.(<chat.trigger.Contest Location>,<expression.(<index_of.(<chat.trigger.Contest Location>,:)>\\+2)>)%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)% %substring_back.(<chat.trigger.Contest Type>,<expression.(<index_of.(<chat.trigger.Contest Type>,:)>\\+2)>)%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%&fEnds in: &e%timer.Contest Timer.time.on.minute%&7:&e%timer.Contest Timer.time.on.second%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%condition.(<chat.trigger.Contest 1st>=lb)%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%chat.trigger.Contest 1st%", false, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%chat.trigger.Contest 2nd%", false, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%chat.trigger.Contest 3rd%", false, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%condition.(<chat.trigger.Contest Placement>=You)%", false, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%substring_front.(<chat.trigger.Contest Placement>,<expression.(<index_of.(<chat.trigger.Contest Placement>,out of)>\\-2)>)%", true, true),
                        Triplet.of("%condition.(<timer.Contest Timer.time.is_on>=true)%%not.(<condition.(<chat.trigger.Contest Placement>=Unranked)>)%%condition.(<chat.trigger.Contest Location>=<boss_bar.location>)%%substring_back.(<chat.trigger.Contest Placement>,<expression.(<index_of.(<chat.trigger.Contest Placement>,out of)>\\-1)>)%", true, true)
                )),
                        Alignment.RIGHT,
                        1,
                        50,
                        1.0f,
                        true,
                        true
                )
        );

        // ID of the HUD
        public Map<String, CustomHud> customHudRawDataList = new HashMap<>(defaultHuds);

        public CustomHudDataModel() {
            super(CUSTOM_HUD_DATA_MODEL_VERSION, null);
        }
    }
    //endregion

    //region Hud Object
    public static class CustomHud {
        // String, isCentre, isSmall
        private List<Triplet<String, Boolean, Boolean>> stringLines;
        private Alignment alignment;
        private int xPos;
        private int yPos;
        private float scale;
        private boolean showBackground;
        private boolean showElement;

        public List<Triplet<String, Boolean, Boolean>> getStringLines() {
            return stringLines != null ? stringLines : new ArrayList<>();
        }

        public Alignment getAlignment() {
            return alignment != null ? alignment : Alignment.TOP_LEFT;
        }

        public int getxPos() {
            return xPos;
        }

        public int getyPos() {
            return yPos;
        }

        public float getScale() {
            return scale;
        }

        public boolean isShowBackground() {
            return showBackground;
        }

        public boolean isShowElement() {
            return showElement;
        }

        public CustomHud(
                List<Triplet<String, Boolean, Boolean>> stringLines,
                Alignment alignment,
                int xPos,
                int yPos,
                float scale,
                boolean showBackground,
                boolean showElement
        ) {
            this.stringLines = stringLines;
            this.alignment = alignment;
            this.xPos = xPos;
            this.yPos = yPos;
            this.scale = scale;
            this.showBackground = showBackground;
            this.showElement = showElement;
        }

        public CustomHud() {
            this.stringLines = new ArrayList<>(List.of(
                    Triplet.of("Example text", false, false)
            ));
            this.alignment = Alignment.TOP_LEFT;
            this.xPos = 30;
            this.yPos = 30;
            this.scale = 1.0f;
            this.showBackground = true;
            this.showElement = true;
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "customHudData", Pair.of(Component.literal("[customHudData]"), ComponentHelper.literal(getCustomHudData()))
        );
    }
    //endregion
}
