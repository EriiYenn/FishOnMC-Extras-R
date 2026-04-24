package dannypx.foe.screens.element.hud;

import dannypx.foe.handler.fetch.TabOverlayHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.handler.logic.ProfitTrackerHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.config.Configs;
import dannypx.foe.screens.element.Element;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ProfitTrackerElement extends Element {
    private static final int PADDING = 6;
    private static final int LINE_GAP = 1;
    private static final int PANEL_ALPHA = 0x99000000;

    public ProfitTrackerElement() {
        super(120,
                48,
                Configs.hudConfig.profitTrackerElementXPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementYPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementAlignment.get(),
                Configs.hudConfig.profitTrackerElementGroup.translation("Profit Tracker"),
                false);
    }

    public ProfitTrackerElement(boolean isCopy) {
        super(120,
                48,
                Configs.hudConfig.profitTrackerElementXPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementYPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementAlignment.get(),
                Configs.hudConfig.profitTrackerElementGroup.translation("Profit Tracker"),
                isCopy);
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!Configs.hudConfig.showProfitTrackerElement.get()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        ProfitTrackerHandler h = ProfitTrackerHandler.instance();

        Component title = Component.literal("Profit").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        Component lineTotal = Component.literal("")
                .append(Component.literal("Session ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(h.sessionTotalFormatted()).withStyle(ChatFormatting.WHITE));
        Component lineRate = Component.literal("")
                .append(Component.literal("/hr ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(h.perHourFormatted()).withStyle(ChatFormatting.AQUA));
        Component lineLast = Component.literal("")
                .append(Component.literal("Last ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(h.lastCatchFormatted()).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal(h.lastCatchNameTruncated()).withStyle(ChatFormatting.GRAY));

        int innerW = Math.max(font.width(title), Math.max(font.width(lineTotal), Math.max(font.width(lineRate), font.width(lineLast))));
        int innerH = font.lineHeight * 4 + LINE_GAP * 3;
        int boxW = innerW + PADDING * 2;
        int boxH = innerH + PADDING * 2;

        int scaledWidth = (int) (Minecraft.getInstance().getWindow().getGuiScaledWidth() * (1 / Configs.hudConfig.profitTrackerElementScale.get()));
        int scaledHeight = (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() * (1 / Configs.hudConfig.profitTrackerElementScale.get()));

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(Configs.hudConfig.profitTrackerElementScale.get(), Configs.hudConfig.profitTrackerElementScale.get());

        if (LoadingHandler.instance().isLoadingDone() && TabOverlayHandler.instance().isInInstance()) {
            if (!isCopy) {
                xPos = Configs.hudConfig.profitTrackerElementXPosition.get() / 100f;
                yPos = Configs.hudConfig.profitTrackerElementYPosition.get() / 100f;
            }

            int x = switch (Configs.hudConfig.profitTrackerElementAlignment.get()) {
                case TOP_LEFT, BOTTOM_LEFT -> Math.round(scaledWidth * xPos);
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - Math.round(scaledWidth * xPos) - boxW;
                default -> Math.round(scaledWidth * xPos);
            };

            int y = switch (Configs.hudConfig.profitTrackerElementAlignment.get()) {
                case TOP_LEFT, TOP_RIGHT -> Math.round(scaledHeight * yPos);
                case BOTTOM_LEFT, BOTTOM_RIGHT -> scaledHeight - Math.round(scaledHeight * yPos) - boxH;
                default -> Math.round(scaledHeight * yPos);
            };

            guiGraphics.fill(x, y, x + boxW, y + boxH, PANEL_ALPHA);

            int tx = x + PADDING;
            int ty = y + PADDING;
            GuiGraphicsHelper.drawString(guiGraphics, font, title, tx, ty, true, false, false, false);
            ty += font.lineHeight + LINE_GAP;
            GuiGraphicsHelper.drawString(guiGraphics, font, lineTotal, tx, ty, true, false, false, false);
            ty += font.lineHeight + LINE_GAP;
            GuiGraphicsHelper.drawString(guiGraphics, font, lineRate, tx, ty, true, false, false, false);
            ty += font.lineHeight + LINE_GAP;
            GuiGraphicsHelper.drawString(guiGraphics, font, lineLast, tx, ty, true, false, false, false);
        }

        guiGraphics.pose().popMatrix();
    }
}
