package dannypx.foe.screens;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.logic.LoggerHandler;
import dannypx.foe.handler.store.CustomChatNotificationDataHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import dannypx.foe.screens.widget.ButtonListWidget;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.type.type_adapter.PatternAdapter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

public class CustomChatNotificationMakerScreen extends Screen implements ScreenConstants {
    //region Fields
    private final Screen parentScreen;

    private ButtonListWidget buttonList;
    private Map<String, ButtonListWidget.ButtonEntry> buttonEntryMap = new HashMap<>();
    private String selectedChatNotificationId;

    private Component header;
    private final int widgetHeight = 20;

    private EditBox nameEditBox;

    private final int sideWidth = 100;
    private EditBox stringEditBox;
    private String stringField;
    //endregion

    //region Methods
    public CustomChatNotificationMakerScreen(Screen parent) {
        super(Component.literal("Custom Chat Notification Maker Screen"));
        this.parentScreen = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.renderWidgets();
        this.resetFields();
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBox(guiGraphics, mouseX, mouseY, delta);

        super.render(guiGraphics, mouseX, mouseY, delta);

        this.renderComponent(guiGraphics, mouseX, mouseY, delta);
        this.renderTooltip(guiGraphics, mouseX, mouseY, delta);
        this.buttonList.render(guiGraphics, mouseX, mouseY, delta);
    }

    private void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        if(stringEditBox.isMouseOver(mouseX, mouseY)) {
            guiGraphics.setComponentTooltipForNextFrame(font, List.of(
                    Component.literal("You can also use placeholders. See wiki")
            ), mouseX, mouseY);
        }
    }

    private void renderComponent(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredString(font,
                this.header,
                (BUTTON_WIDTH + PADDING * 2) + (this.minecraft.getWindow().getGuiScaledWidth() - (BUTTON_WIDTH + PADDING * 2)) / 2,
                PADDING + widgetHeight / 2 - font.lineHeight / 2,
                CommonColors.WHITE
        );

        guiGraphics.drawString(font,
                Component.literal("Name"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - font.lineHeight / 2 + (widgetHeight + PADDING),
                CommonColors.WHITE,
                true
        );

        guiGraphics.drawString(font,
                Component.literal("Notif. Text"),
                (BUTTON_WIDTH + PADDING * 2) + PADDING,
                PADDING + widgetHeight / 2 - font.lineHeight / 2 + (widgetHeight + PADDING) * 2,
                CommonColors.WHITE,
                true
        );
    }

    private void renderBox(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)
    {
        guiGraphics.fill(
                (BUTTON_WIDTH + PADDING * 2), 0,
                this.minecraft.getWindow().getGuiScaledWidth(),
                this.minecraft.getWindow().getGuiScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3,
                0x99000000);
        guiGraphics.hLine((BUTTON_WIDTH + PADDING * 2), this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3, CommonColors.DARK_GRAY);
        guiGraphics.vLine((BUTTON_WIDTH + PADDING * 2), 0, this.minecraft.getWindow().getGuiScaledHeight() - (BUTTON_HEIGHT + PADDING_HALF) - 3, CommonColors.DARK_GRAY);
    }

    private void renderWidgets() {
        List<AbstractWidget> widgets = new ArrayList<>();

        widgets.add(this.saveBackButton());
        widgets.add(this.backButton());

        widgets.add(getButtonList());

        widgets.add(getNewButtonElementButton());
        widgets.add(getDeleteButtonElementButton());
        widgets.add(getImportButton());
        widgets.add(getExportButton());

        widgets.add(getNameEditBox());
        widgets.add(getStringEditBox());

        widgets.forEach(this::addRenderableWidget);
    }

    private AbstractWidget getNameEditBox() {
        nameEditBox = new EditBox(
                font,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + widgetHeight + PADDING,
                this.minecraft.getWindow().getGuiScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2  - sideWidth,
                widgetHeight,
                Component.empty()
        );
        nameEditBox.setMaxLength(Integer.MAX_VALUE);

        nameEditBox.setResponder(s -> {
            if(selectedChatNotificationId != null) {
                nameEditBox.setHint(Component.literal(s));
            }
        });

        return nameEditBox;
    }

    private AbstractWidget getStringEditBox() {
        stringEditBox = new EditBox(
                font,
                (BUTTON_WIDTH + PADDING * 2) + PADDING + sideWidth,
                PADDING + (widgetHeight + PADDING) * 2,
                this.minecraft.getWindow().getGuiScaledWidth() - (BUTTON_WIDTH + PADDING * 2) - PADDING * 2 - sideWidth,
                widgetHeight,
                Component.empty()
        );
        stringEditBox.setMaxLength(Integer.MAX_VALUE);

        stringEditBox.setResponder(s -> {
            if(selectedChatNotificationId != null) {
                stringField = s;
                stringEditBox.setHint(Component.literal(s));
            }
        });

        return stringEditBox;
    }

    private AbstractWidget getNewButtonElementButton() {
        return Button.builder(
                        Component.literal("Create Chat Notification"),
                        (button) -> {
                            String id = "Custom Chat Notification #" + UUID.randomUUID();

                            CustomChatNotificationDataHandler.instance().createNewChatCustomNotification(id);

                            ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(id);

                            buttonList.addEntry(buttonEntry);
                            buttonEntryMap.put(id, buttonEntry);
                        })
                .size(BUTTON_WIDTH / 2 - PADDING, BUTTON_HEIGHT)
                .pos(PADDING_HALF, this.minecraft.getWindow().getGuiScaledHeight() - PADDING_HALF - BUTTON_HEIGHT)
                .build();
    }

    private AbstractWidget getDeleteButtonElementButton() {
        return Button.builder(
                        Component.literal("Delete Selected"),
                        (button) -> {
                            if(selectedChatNotificationId != null) {
                                CustomChatNotificationDataHandler.instance().deleteCustomChatNotification(selectedChatNotificationId);

                                ButtonListWidget.ButtonEntry entry = buttonEntryMap.get(selectedChatNotificationId);

                                buttonList.removeEntry(entry);
                                buttonEntryMap.remove(selectedChatNotificationId);

                                selectedChatNotificationId = null;
                                resetFields();
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .pos(PADDING + (BUTTON_WIDTH / 2 - PADDING_HALF), this.minecraft.getWindow().getGuiScaledHeight() - PADDING_HALF - BUTTON_HEIGHT)
                .build();
    }

    private AbstractWidget getImportButton() {
        return Button.builder(
                        Component.literal("Import"),
                        (button) -> {
                            String rawData = this.minecraft.keyboardHandler.getClipboard();
                            try {
                                String json = ComponentHelper.decompress(Base64.getDecoder().decode(rawData));

                                Gson gson = new GsonBuilder().create();
                                Triplet<String, String, Integer> data = gson.fromJson(json, TypeToken.getParameterized(Triplet.class, String.class, Integer.class).getType());

                                if(data.value3() > FishOnMCExtras.CHAT_NOTIFICATION_VERSION) {
                                    SystemToast.add(this.minecraft.getToastManager(),
                                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                            Component.literal("Fish On Extras Rebirth"),
                                            Component.literal("Could not Import. Imported Chat Notification is made on a newer version"));
                                    return;
                                }

                                if(CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.containsKey(data.value1())) {
                                    String notification = data.value1() + " (Duplicate)";

                                    data = Triplet.of(data.value1() + " (Duplicate)", notification, data.value3());
                                }

                                String id = data.value1();

                                CustomChatNotificationDataHandler.instance().createNewChatCustomNotification(data.value1(), data.value2());

                                ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(id);

                                buttonList.addEntry(buttonEntry);
                                buttonEntryMap.put(id, buttonEntry);

                                SystemToast.add(this.minecraft.getToastManager(),
                                        SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                        Component.literal("Fish On Extras Rebirth"),
                                        Component.literal("Imported Chat Notification"));
                            } catch (Exception e) {
                                LoggerHandler.error(e);

                                SystemToast.add(this.minecraft.getToastManager(),
                                        SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                        Component.literal("Fish On Extras Rebirth"),
                                        Component.literal("Could not Import. Data invalid"));
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING, BUTTON_HEIGHT)
                .pos(PADDING_HALF, this.minecraft.getWindow().getGuiScaledHeight() - PADDING_HALF - BUTTON_HEIGHT * 2 - PADDING_HALF)
                .tooltip(Tooltip.create(Component.literal("Imports from the code on your clipboard")))
                .build();
    }

    private AbstractWidget getExportButton() {
        return Button.builder(
                        Component.literal("Export Selected"),
                        (button) -> {
                            if(selectedChatNotificationId != null) {
                                try {
                                    Triplet<String, String, Integer> dataButton = Triplet.of(
                                            selectedChatNotificationId,
                                            selectedChatNotificationId,
                                            FishOnMCExtras.CHAT_NOTIFICATION_VERSION
                                    );

                                    String rawData = Base64.getEncoder().encodeToString(
                                            ComponentHelper.compress(new GsonBuilder().registerTypeAdapter(Pattern.class, new PatternAdapter()).create().toJson(dataButton))
                                    );

                                    String dataToCopy = "**Custom Chat Notification: **" + selectedChatNotificationId + "\n" +
                                            "```\n" +
                                            rawData + "\n" +
                                            "```\n" +
                                            "-# Using Chat Trigger version: " + "`v" + FishOnMCExtras.CHAT_NOTIFICATION_VERSION + "`";

                                    this.minecraft.keyboardHandler.setClipboard(dataToCopy);

                                    SystemToast.add(this.minecraft.getToastManager(),
                                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                            Component.literal("Fish On Extras Rebirth"),
                                            Component.literal("Exported Chat Notification on your clipboard"));
                                } catch (Exception e) {
                                    LoggerHandler.error(e);

                                    SystemToast.add(this.minecraft.getToastManager(),
                                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                                            Component.literal("Fish On Extras Rebirth"),
                                            Component.literal("An error has occurred"));
                                }
                            }
                        })
                .size(BUTTON_WIDTH / 2 - PADDING_HALF, BUTTON_HEIGHT)
                .pos(PADDING + (BUTTON_WIDTH / 2 - PADDING_HALF), this.minecraft.getWindow().getGuiScaledHeight() - PADDING_HALF - BUTTON_HEIGHT * 2 - PADDING_HALF)
                .tooltip(Tooltip.create(Component.literal("Save first before exporting")))
                .build();
    }

    private AbstractWidget getButtonList() {
        buttonList = new ButtonListWidget(
                this.minecraft,
                (BUTTON_WIDTH + PADDING * 2),
                height - ScreenConstants.BUTTON_HEIGHT * 3 - PADDING * 2,
                0,
                BUTTON_HEIGHT + PADDING_HALF,
                BUTTON_HEIGHT,
                "Custom Chat Triggers"
        );

        CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.forEach((name, text) -> {
            ButtonListWidget.ButtonEntry buttonEntry = createChatNotificationEntry(name);

            buttonList.addEntry(buttonEntry);
            buttonEntryMap.put(name, buttonEntry);
        });

        return buttonList;
    }

    private Button saveBackButton() {
        return Button.builder(Component.literal("Save and Return"), button -> {
            if(selectedChatNotificationId != null) {
                if(nameEditBox.getValue().isBlank()) {
                    SystemToast.add(this.minecraft.getToastManager(),
                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            Component.literal("Fish On Extras Rebirth"),
                            Component.literal("Chat Notification name is empty"));

                    return;
                }

                if(!Objects.equals(selectedChatNotificationId, nameEditBox.getValue())
                        && CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.containsKey(nameEditBox.getValue())
                ) {
                    SystemToast.add(this.minecraft.getToastManager(),
                            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
                            Component.literal("Fish On Extras Rebirth"),
                            Component.literal("Chat Notification name already exist"));

                    return;
                }

                CustomChatNotificationDataHandler.instance().updateChatNotification(selectedChatNotificationId, nameEditBox.getValue(), stringEditBox.getValue());

            }
                    this.onClose();
                })
                .pos(width - PADDING_HALF - BUTTON_WIDTH / 2, height - PADDING_HALF - BUTTON_HEIGHT)
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .build();
    }

    private Button backButton() {
        return Button.builder(Component.literal("Return"), button ->
                    this.onClose())
                .pos(width - (PADDING_HALF + BUTTON_WIDTH / 2) * 2, height - PADDING_HALF - BUTTON_HEIGHT)
                .size(BUTTON_WIDTH / 2, BUTTON_HEIGHT)
                .build();
    }

    private ButtonListWidget.ButtonEntry createChatNotificationEntry(String id) {
        return new ButtonListWidget.ButtonEntry(
                Button.builder(
                        Component.literal(id),
                        button -> {
                            selectedChatNotificationId = id;
                            stringField = CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.get(id);
                            this.setFields();
                        }
                ).width(BUTTON_WIDTH).build()
        );
    }

    private void setFields() {
        this.header = Component.literal(selectedChatNotificationId);
        nameEditBox.setValue(selectedChatNotificationId);
        nameEditBox.setHint(Component.literal(selectedChatNotificationId));

        if(selectedChatNotificationId != null) {
            stringEditBox.setValue(stringField);
            stringEditBox.setHint(Component.literal(stringField));
        }
    }

    private void resetFields() {
        this.header = Component.literal("No Chat Trigger Selected");

        nameEditBox.setValue("");
        nameEditBox.setHint(Component.literal(""));


        stringEditBox.setValue("");
        stringEditBox.setHint(Component.literal(""));


        stringField = "";
        selectedChatNotificationId = null;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parentScreen);
    }
    //endregion
}
