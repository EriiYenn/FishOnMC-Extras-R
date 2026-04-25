package dannypx.foe.config;

import dannypx.foe.FishOnMCExtras;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import me.fzzyhmstrs.fzzy_config.util.Translatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedLong;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Version(version = 8)
@Translatable.Name("§7§oDev§8§o: §f§oHandlers")
@Translatable.Desc("§4WARNING §7These are the back-end handlers. Disabling these might stop some " +
        "functions from working. Do not touch these unless you know what you are doing")
public class HandlerConfig extends Config {

    public HandlerConfig() {
        super(Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "handler_config"));
    }

    //region Fetch Handler Group
    @Name("Fetch Handlers")
    @Desc("§7Handles fetching data from various Minecraft elements")
    public ConfigGroup fetchHandlerGroup = new ConfigGroup("fetch_handler_group");

    @Desc("§7Data from Player List (Tab)")
    public ValidatedBoolean tabOverlayHandler = new ValidatedBoolean(true);

    @Desc("§7Data from Scoreboard")
    public ValidatedBoolean scoreboardHandler = new ValidatedBoolean(true);

    @Desc("§7Data from Client Player")
    public ValidatedBoolean localPlayerHandler = new ValidatedBoolean(true);

    @Desc("§7Data from Player Inventory")
    public ValidatedBoolean inventoryHandler = new ValidatedBoolean(true);

    @Desc("§7Data from Boss Bar")
    public ValidatedBoolean bossEventHandler = new ValidatedBoolean(true);

    @Desc("§7Data from Network Handler")
    public ValidatedBoolean networkHandler = new ValidatedBoolean(true);

    @Name("Screen Handlers")
    @Desc("§7Handles fetching of screen data")
    public ConfigGroup screenFetchGroup = new ConfigGroup("screen_fetch_group");

    @Desc("§7Data from Screens")
    public ValidatedBoolean genericContainerScreenHandler = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    @ConfigGroup.Pop
    @Desc("§7The delay in ticks before any data is fetched")
    public ValidatedInt screenDelayFetch = new ValidatedInt(2, 100, 0, ValidatedNumber.WidgetType.SLIDER);
    //endregion

    //region IO Handler Group
    @Name("IO Handlers")
    @Desc("§7Handles storing data to disk")
    public ConfigGroup ioHandlerGroup = new ConfigGroup("io_handler_group");

    @ConfigGroup.Pop
    @Desc("§7File storing handler")
    public ValidatedBoolean dataFileHandler = new ValidatedBoolean(true);
    //endregion

    //region Logic Handler Group
    @Name("Logic Handlers")
    @Desc("§7Handles logic of data")
    public ConfigGroup logicGroup = new ConfigGroup("logic_group");


    @Desc("§7Handles loading mod when on server")
    public ValidatedBoolean loadingHandler = new ValidatedBoolean(true);

    @Desc("§7Handles key binds")
    public ValidatedBoolean keyBindHandler = new ValidatedBoolean(true);

    @Desc("§7Handles the notifier")
    public ValidatedBoolean notifierHandler = new ValidatedBoolean(true);

    @Desc("§7Handles the crew info")
    public ValidatedBoolean crewHandler = new ValidatedBoolean(true);

    @Desc("§7Handles the ray cast hit results")
    public ValidatedBoolean rayCastHandler = new ValidatedBoolean(true);

    @Desc("§7Handles the light rendering")
    public ValidatedBoolean lightHandler = new ValidatedBoolean(true);

    @Desc("§7Handles the timers")
    public ValidatedBoolean timerHandler = new ValidatedBoolean(true);

    @Name("Event Handler")
    @Desc("§7Handles logic of client events")
    public ConfigGroup eventLogicGroup = new ConfigGroup("event_logic_group");

    @ConfigGroup.Pop
    @Desc("§7Open /events on join")
    public ValidatedBoolean openEventsOnJoin = new ValidatedBoolean(true);

    @Name("Catching Handler")
    @Desc("§7Handles logic of fishing")
    public ConfigGroup catchingLogicGroup = new ConfigGroup("catching_logic_group");

    @Desc("§7Handles fishing")
    public ValidatedBoolean catchingHandler = new ValidatedBoolean(true);

    @Desc("§7The cooldown in seconds before wasFishing() is turned to false")
    public ValidatedInt catchingStatusCooldown = new ValidatedInt(5, 60, 0, ValidatedNumber.WidgetType.SLIDER);

    @Desc("§7The item check window before caught fish.\nMake sure this is minimum 50 + (50 * catchingItemsDelayCheck)")
    public ValidatedLong catchingItemsCheckWindow = new ValidatedLong(100L, 1000L, 0L, ValidatedNumber.WidgetType.SLIDER);

    @ConfigGroup.Pop
    @ConfigGroup.Pop
    @Desc("§7The delay in ticks before checking items")
    public ValidatedInt catchingItemsDelayCheck = new ValidatedInt(1, 20, 0, ValidatedNumber.WidgetType.SLIDER);

    @Name("Reward Profit Handler")
    @Desc("§7Handles quest and competition reward attribution for the profit tracker")
    public ConfigGroup rewardProfitLogicGroup = new ConfigGroup("reward_profit_logic_group");

    @Desc("§7Track quest rewards in the profit tracker")
    public ValidatedBoolean trackQuestProfit = new ValidatedBoolean(true);

    @Desc("§7Track competition rewards in the profit tracker")
    public ValidatedBoolean trackCompetitionProfit = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    @Desc("§7How long in milliseconds reward contexts stay active for inventory gains")
    public ValidatedLong rewardProfitWindowMs = new ValidatedLong(4000L, 10000L, 250L, ValidatedNumber.WidgetType.SLIDER);


    @Name("Debug message dismissal time")
    @Desc("§7How long in seconds, before the notification dismisses")
    public ValidatedInt debugDismissalTime = new ValidatedInt(15, 60, 0, ValidatedNumber.WidgetType.SLIDER);
    //endregion

    //region Renderer Handler Group
    @Name("Renderer Handlers")
    @Desc("§7Handles rendering")
    public ConfigGroup rendererGroup = new ConfigGroup("renderer_group");

    @ConfigGroup.Pop
    @Desc("§7Handles HUD rendering")
    public ValidatedBoolean hudRenderHandler = new ValidatedBoolean(true);
    //endregion

    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON;
    }
}
