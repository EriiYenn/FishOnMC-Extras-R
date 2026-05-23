package dannypx.foe.config;

import dannypx.foe.FishOnMCExtras;
import me.fzzyhmstrs.fzzy_config.annotations.Version;
import me.fzzyhmstrs.fzzy_config.api.FileType;
import me.fzzyhmstrs.fzzy_config.config.Config;
import me.fzzyhmstrs.fzzy_config.config.ConfigGroup;
import me.fzzyhmstrs.fzzy_config.util.Translatable;
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedBoolean;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@Version(version = 7)
@Translatable.Name("§7§oDev§8§o: §f§oMixin Toggles")
@Translatable.Desc("§4WARNING §7These are the toggles to mixins. Do not touch these unless you know what you are doing")
public class MixinConfig extends Config {
    public MixinConfig() {
        super(Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "mixin_config"));
    }

    @Name("HumanoidArmorLayerMixin Mixin")
    public ConfigGroup humanoidArmorLayerMixinGroup = new ConfigGroup("humanoid_armor_layer_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean humanoidArmorLayerMixinRenderArmorPiece = new ValidatedBoolean(true);

    @Name("BossHealthOverlay Mixin")
    public ConfigGroup bossHealthOverlayMixinGroup = new ConfigGroup("boss_health_overlay_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean bossHealthOverlayMixinRender = new ValidatedBoolean(true);

    @Name("ClientPlayNetworkHandler Mixin")
    public ConfigGroup clientPlayNetworkHandlerMixinGroup = new ConfigGroup("client_play_network_handler_mixin_group");

    public ValidatedBoolean clientPacketListenerMixinHandlePlayerInfoUpdate = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean clientPacketListenerMixinHandlePlayerInfoRemove = new ValidatedBoolean(true);

    @Name("GuiGraphics Mixin")
    public ConfigGroup guiGraphicsMixinGroup = new ConfigGroup("gui_graphics_mixin_group");

    public ValidatedBoolean guiGraphicsMixinRenderItemCount = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean guiGraphicsMixinRenderItemDecorations = new ValidatedBoolean(true);

    @Name("FishingHookEntityRenderer Mixin")
    public ConfigGroup fishingHookEntityRendererMixinGroup = new ConfigGroup("fishing_fishing_hook_entity_renderer_mixin_group");

    public ValidatedBoolean fishingHookRendererMixinVertex = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean fishingHookRendererMixinRender = new ValidatedBoolean(true);

    @Name("AbstractContainerScreen Mixin")
    public ConfigGroup abstractContainerScreenMixinGroup = new ConfigGroup("abstract_container_screen_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean abstractContainerScreenMixinKeyPressed = new ValidatedBoolean(true);

    @Name("Gui Mixin")
    public ConfigGroup guiMixinGroup = new ConfigGroup("gui_hud");

    public ValidatedBoolean guiMixinSetTitle = new ValidatedBoolean(true);

    public ValidatedBoolean guiMixinSetSubtitle = new ValidatedBoolean(true);

    public ValidatedBoolean guiMixinRenderSelectedItemName = new ValidatedBoolean(true);

    public ValidatedBoolean guiMixinRenderScoreBoardSidebar = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean guiMixinRenderItemHotbar = new ValidatedBoolean(true);

    @Name("PlayerTabOverlayMixin Mixin")
    public ConfigGroup playerTabOverlayMixinGroup = new ConfigGroup("player_tab_overlay_mixin_group");

    public ValidatedBoolean playerTabOverlayMixinRedirectRender = new ValidatedBoolean(true);

    public ValidatedBoolean playerTabOverlayMixinInjectRender = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean playerTabOverlayMixinCollectPlayerEntries = new ValidatedBoolean(true);

    @Name("AbstractRecipeBookScreen Mixin")
    public ConfigGroup abstractRecipeBookScreenMixinGroup = new ConfigGroup("abstract_recipe_book_screen");

    @ConfigGroup.Pop
    public ValidatedBoolean abstractRecipeBookScreenMixinAddRecipeBook = new ValidatedBoolean(true);

    @Name("ContextualBarRenderer Mixin")
    public ConfigGroup contextualBarRendererMixinGroup = new ConfigGroup("contextual_bar_renderer_mixin");

    @ConfigGroup.Pop
    public ValidatedBoolean contextualBarRendererMixinRenderExperienceLevel = new ValidatedBoolean(true);

    @Name("ExperienceBarRenderer Mixin")
    public ConfigGroup experienceBarRendererMixinGroup = new ConfigGroup("experience_bar_renderer_mixin");

    public ValidatedBoolean experienceBarRendererMixinRender = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean experienceBarRendererMixinRenderBackground = new ValidatedBoolean(true);

    @Name("JumpableVehicleBar Mixin")
    public ConfigGroup jumpableVehicleBarMixinGroup = new ConfigGroup("jumpable_vehicle_bar_mixin");

    public ValidatedBoolean jumpableVehicleBarMixinRender = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean jumpableVehicleBarMixinRenderBackground = new ValidatedBoolean(true);

    @Name("LocatorBarMixin Mixin")
    public ConfigGroup locatorBarMixinGroup = new ConfigGroup("locator_bar_mixin");

    public ValidatedBoolean locatorBarMixinRender = new ValidatedBoolean(true);

    @ConfigGroup.Pop
    public ValidatedBoolean locatorBarMixinRenderBackground = new ValidatedBoolean(true);

    @Name("InventoryScreen Mixin")
    public ConfigGroup inventoryScreenMixinGroup = new ConfigGroup("inventory_screen_mixin");

    @ConfigGroup.Pop
    public ValidatedBoolean inventoryScreenMixinRenderBg = new ValidatedBoolean(true);

    @Name("LevelRendererBrightnessGetter Mixin")
    public ConfigGroup levelRendererBrightnessGetterMixinGroup = new ConfigGroup("level_renderer_brightness_getter_mixin");

    @ConfigGroup.Pop
    public ValidatedBoolean levelRendererBrightnessGetterMixinMethod_68890 = new ValidatedBoolean(true);

    @Name("RecipeBookWidget Mixin")
    public ConfigGroup recipeBookWidgetMixinGroup = new ConfigGroup("recipe_book_widget");

    @ConfigGroup.Pop
    public ValidatedBoolean recipeBookWidgetIsOpen = new ValidatedBoolean(true);

    @Name("BlockLightSectionStorageMixin Mixin")
    public ConfigGroup blockLightSectionStorageMixinGroup = new ConfigGroup("block_light_section_stroage_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean blockLightSectionStorageMixinGetLightValue = new ValidatedBoolean(true);

    @Name("Entity Mixin")
    public ConfigGroup entityMixinGroup = new ConfigGroup("entity_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean entityMixinIsCustomNameVisible = new ValidatedBoolean(true);

    @Name("EntityRenderDispatcher Mixin")
    public ConfigGroup entityRenderDispatcherMixinGroup = new ConfigGroup("entity_renderer_dispatcher_mixin_group");

    @ConfigGroup.Pop
    public ValidatedBoolean entityRenderDispatcherMixinShouldRender = new ValidatedBoolean(true);

    @Override
    public @NotNull FileType fileType() {
        return FileType.JSON;
    }
}
