package dannypx.foe.config;

import dannypx.foe.FishOnMCExtras;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import me.fzzyhmstrs.fzzy_config.util.Translatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedString;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedInt;
import me.fzzyhmstrs.fzzy_config.validation.number.ValidatedNumber;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Version(version = 7)
@Translatable.Name("Renderer Configuration")
@Translatable.Desc("§7Configure renderer of various things")
public class RendererConfig extends Config {
    public RendererConfig() {
        super(Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "renderer_config"));
    }

    @Name("Item Marker")
    @Desc("§7This is the small markers on items")
    public ConfigGroup itemMarkerGroup = new ConfigGroup("item_marker_group");

    @Name("Show Equipped Pet Item")
    public ValidatedBoolean showPetEquippedMarker = new ValidatedBoolean(true);

    @Name("Show Rarity marker")
    public ValidatedBoolean showRarityMarker = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    @Name("Blacklist items from rendering rarity marker")
    @Desc("§7Blacklist item types here.\nYou can put item types here separated by commas.\n\nIf you don't know what item type certain items are, check in the Debug Screen.\n\n1. Turn on Debug mode in Debug Configurations\n2. Hold an item\n3. Go to the Debug Screen\n4. Scroll to InventoryHandler\n5. You can read the item type in Current Held Item")
    public ValidatedString blackListItems = new ValidatedString.Builder("")
            .withCorrector().build();

    @Name("Entity Model Renderer")
    @Desc("§7This is the model renderer for entities")
    public ConfigGroup entityModelGroup = new ConfigGroup("entity_model_renderer_group");

    @Name("Show pet names")
    public ValidatedBoolean showPetName = new ValidatedBoolean(true);

    @Name("Show nameplate on players")
    public ValidatedBoolean showPlayerNamePlate = new ValidatedBoolean(true);

    @Name("Use the 3D fishing hook texture")
    public ValidatedBoolean showNewFishingHook = new ValidatedBoolean(true);

    @Name("Fishing Hook Light")
    @Desc("§7This is the light that emits from the fishing hook")
    public ConfigGroup fishingHookLightGroup = new ConfigGroup("fishing_hook_light_renderer_group");

    @Name("Emit light from fishing hook")
    public ValidatedBoolean showLightFishingHook = new ValidatedBoolean(true);

    @Name("Light Radius")
    public ValidatedInt lightRadiusFishingHook = new ValidatedInt(5, 15, 0, ValidatedNumber.WidgetType.SLIDER);

    @ConfigGroup.Pop
    @Name("Max Light Level")
    public ValidatedInt maxLightLevelFishingHook = new ValidatedInt(10, 15, 0, ValidatedNumber.WidgetType.SLIDER);

    @ConfigGroup.Pop
    @Name("Show bait on fishing hook")
    public ValidatedBoolean showBaitOnFishingHook = new ValidatedBoolean(true);

    @Name("Hide armor on players")
    public ValidatedBoolean hideArmor = new ValidatedBoolean(false);

    @Name("Small Stack Count Number")
    public ConfigGroup smallStackCountGroup = new ConfigGroup("small_stack_count_group");

    @Name("Use small stack count number")
    public ValidatedBoolean useSmallStackCountNumber = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    @Name("Show stack count on bait")
    public ValidatedBoolean showStackCountOnBait = new ValidatedBoolean(true);

    @Name("Tab Renderer")
    @Desc("§7This is the renderer for tab extras")
    public ConfigGroup tabRendererGroup = new ConfigGroup("tab_renderer_group");

    @ConfigGroup.Pop
    @Name("Show online crew members")
    public ValidatedBoolean showOnlineCrewMembers = new ValidatedBoolean(true);


    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON;
    }
}
