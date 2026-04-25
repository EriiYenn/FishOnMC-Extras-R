package dannypx.foe.screens;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.ProfitCategoryDataHandler;
import dannypx.foe.handler.store.ProfitPricingDataHandler;
import dannypx.foe.handler.store.ProfitRewardTriggerDataHandler;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NonNull;

public class MainScreen extends DefaultModScreen {
    //region Fields
    private static final Identifier ICON_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "elements/icon");
    //endregion

    //region Methods
    public MainScreen(Screen parent) {
        super(parent, Component.literal("Main Screen"));
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
    }

    @Override
    public void render(@NonNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        int screenWidth = this.minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = this.minecraft.getWindow().getGuiScaledHeight();

        int size = 200;

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                ICON_TEXTURE,
                screenWidth / 2 - size / 2, screenHeight / 2 - size + 32,
                size, size
        );

        Component hudComponent = Component.literal("Creator Settings");
        guiGraphics.drawString(font, hudComponent, width / 2 - font.width(hudComponent) / 2, height / 2 - PADDING_QUART - font.lineHeight, CommonColors.WHITE, true);

        Component configComponent = Component.literal("Configuration");
        guiGraphics.drawString(font, configComponent, width / 2 - font.width(configComponent) / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2 + BUTTON_HEIGHT + PADDING, CommonColors.WHITE, true);

        //Versions
        guiGraphics.drawString(font, Component.literal("Mod Version: v" + FishOnMCExtras.VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - font.lineHeight - PADDING_QUART, CommonColors.WHITE, true);
        guiGraphics.drawString(font, Component.literal("HUD Version: v" + FishOnMCExtras.HUD_VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - (font.lineHeight + PADDING_QUART) * 2, CommonColors.WHITE, true);
        guiGraphics.drawString(font, Component.literal("Chat Trigger Version: v" + FishOnMCExtras.CHAT_TRIGGER_VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - (font.lineHeight + PADDING_QUART) * 3, CommonColors.WHITE, true);
        guiGraphics.drawString(font, Component.literal("Timer Version: v" + FishOnMCExtras.TIMER_VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - (font.lineHeight + PADDING_QUART) * 4, CommonColors.WHITE, true);
        guiGraphics.drawString(font, Component.literal("Notification Version: v" + FishOnMCExtras.NOTIFICATION_VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - (font.lineHeight + PADDING_QUART) * 5, CommonColors.WHITE, true);
        guiGraphics.drawString(font, Component.literal("Button Version: v" + FishOnMCExtras.BUTTON_VERSION).withStyle(ChatFormatting.DARK_GRAY), PADDING_QUART, height - (font.lineHeight + PADDING_QUART) * 6, CommonColors.WHITE, true);
    }

    private void renderWidgets() {
        List<AbstractWidget> widgets = new ArrayList<>();

        widgets.add(customHudButton());
        widgets.add(moveHudButton());
        widgets.add(customChatTriggerButton());
        widgets.add(customTimerButton());
        widgets.add(customNotificationButton());
        widgets.add(configButton());
        widgets.add(controlsButton());
        widgets.add(reloadProfitDataButton());
        widgets.add(openProfitDataFolderButton());

        widgets.forEach(this::addRenderableWidget);
    }

    private Button customHudButton() {
        return Button.builder(Component.literal("Create HUDs"), button ->
                        this.minecraft.setScreen(new CustomHudMakerScreen(this.minecraft.screen)))
                .pos(width / 2 - BUTTON_WIDTH / 2, height / 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Custom HUD Creator Screen")))
                .build();
    }

    private Button moveHudButton() {
        return Button.builder(Component.literal("Move HUDs"), button ->
                        this.minecraft.setScreen(new MoveElementScreen(this.minecraft.screen)))
                .pos(width / 2 + PADDING_HALF, height / 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Move HUD Elements Screen")))
                .build();
    }

    private Button customChatTriggerButton() {
        return Button.builder(Component.literal("Create Chat Triggers"), button ->
                        this.minecraft.setScreen(new CustomChatTriggerMakerScreen(this.minecraft.screen)))
                .pos(width / 2 - BUTTON_WIDTH / 2, height / 2 + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Custom Notification Creator Screen")))
                .build();
    }

    private Button customTimerButton() {
        return Button.builder(Component.literal("Create Timers"), button ->
                        this.minecraft.setScreen(new CustomTimerMakerScreen(this.minecraft.screen)))
                .pos(width / 2 + PADDING_HALF, height / 2 + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Custom Notification Creator Screen")))
                .build();
    }

    private Button customNotificationButton() {
        return Button.builder(Component.literal("Create Notifications"), button ->
                        this.minecraft.setScreen(new CustomNotificationMakerScreen(this.minecraft.screen)))
                .pos(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Custom Notification Creator Screen")))
                .build();
    }

    private Button configButton() {
        return Button.builder(Component.literal("Config Screen"), button ->
                        ConfigApiJava.INSTANCE.openScreen(FishOnMCExtras.MOD_ID))
                .pos(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2 + BUTTON_HEIGHT + PADDING + font.lineHeight + PADDING_QUART)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Config Screen")))
                .build();
    }

    private Button controlsButton() {
        return Button.builder(Component.literal("Controls"), button ->
                        ConfigApiJava.INSTANCE.openScreen(Configs.keyBindConfig.translationKey()))
                .pos(width / 2 + PADDING_HALF, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2 + BUTTON_HEIGHT + PADDING + font.lineHeight + PADDING_QUART)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open Controls Config")))
                .build();
    }

    private Button reloadProfitDataButton() {
        return Button.builder(Component.literal("Reload Profit Data"), button -> {
                    boolean pricingReloaded = ProfitPricingDataHandler.instance().reloadFromFile();
                    boolean categoriesReloaded = ProfitCategoryDataHandler.instance().reloadFromFile();
                    boolean rewardTriggersReloaded = ProfitRewardTriggerDataHandler.instance().reloadFromFile();
                    if (pricingReloaded && categoriesReloaded && rewardTriggersReloaded) {
                        LoggerHandler.info("Reloaded profit tracker data files");
                    } else {
                        LoggerHandler.error("Could not reload one or more profit tracker data files");
                    }
                })
                .pos(width / 2 - BUTTON_WIDTH / 2, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2 + BUTTON_HEIGHT + PADDING + font.lineHeight + PADDING_QUART + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Reload profit_pricing.json, profit_categories.json, and profit_reward_triggers.json")))
                .build();
    }

    private Button openProfitDataFolderButton() {
        return Button.builder(Component.literal("Open Folder"), button ->
                        Util.getPlatform().openUri(ProfitPricingDataHandler.instance().getPricingDirectory().toUri().toString()))
                .pos(width / 2 + PADDING_HALF, height / 2 + (BUTTON_HEIGHT + PADDING_HALF) * 2 + BUTTON_HEIGHT + PADDING + font.lineHeight + PADDING_QUART + BUTTON_HEIGHT + PADDING_HALF)
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .tooltip(Tooltip.create(Component.literal("Open the folder that contains profit tracker JSON files")))
                .build();
    }
    //endregion
}
