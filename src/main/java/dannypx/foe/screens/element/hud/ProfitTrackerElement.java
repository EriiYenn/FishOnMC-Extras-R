package dannypx.foe.screens.element.hud;

import dannypx.foe.handler.fetch.TabOverlayHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.handler.logic.ProfitTrackerHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.config.Configs;
import dannypx.foe.screens.element.Element;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

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
        refreshMeasuredSize();
    }

    public ProfitTrackerElement(boolean isCopy) {
        super(120,
                48,
                Configs.hudConfig.profitTrackerElementXPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementYPosition.get() / 100f,
                Configs.hudConfig.profitTrackerElementAlignment.get(),
                Configs.hudConfig.profitTrackerElementGroup.translation("Profit Tracker"),
                isCopy);
        refreshMeasuredSize();
    }

    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!Configs.hudConfig.showProfitTrackerElement.get()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        ProfitTrackerHandler h = ProfitTrackerHandler.instance();
        Layout layout = buildLayout(font, h);
        this.width = layout.boxWidth;
        this.height = layout.boxHeight;

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
                case TOP_RIGHT, BOTTOM_RIGHT -> scaledWidth - Math.round(scaledWidth * xPos) - layout.boxWidth;
                default -> Math.round(scaledWidth * xPos);
            };

            int y = switch (Configs.hudConfig.profitTrackerElementAlignment.get()) {
                case TOP_LEFT, TOP_RIGHT -> Math.round(scaledHeight * yPos);
                case BOTTOM_LEFT, BOTTOM_RIGHT -> scaledHeight - Math.round(scaledHeight * yPos) - layout.boxHeight;
                default -> Math.round(scaledHeight * yPos);
            };

            guiGraphics.fill(x, y, x + layout.boxWidth, y + layout.boxHeight, PANEL_ALPHA);

            int tx = x + PADDING;
            int ty = y + PADDING;
            for (int i = 0; i < layout.lines.size(); i++) {
                GuiGraphicsHelper.drawString(guiGraphics, font, layout.lines.get(i), tx, ty, true, false, false, false);
                if (i < layout.lines.size() - 1) {
                    ty += font.lineHeight + LINE_GAP;
                }
            }
        }

        guiGraphics.pose().popMatrix();
    }

    private void refreshMeasuredSize() {
        Font font = Minecraft.getInstance().font;
        if (font != null) {
            Layout layout = buildLayout(font, ProfitTrackerHandler.instance());
            this.width = layout.boxWidth;
            this.height = layout.boxHeight;
        }
    }

    private static Layout buildLayout(Font font, ProfitTrackerHandler handler) {
        List<Component> lines = new ArrayList<>();
        boolean peaceful = !Configs.hudConfig.profitTrackingExperimental.get();
        lines.add(Component.literal(peaceful ? "Tracker" : "Profit").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (!peaceful) {
            lines.add(Component.literal("")
                    .append(Component.literal("Session ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(handler.sessionTotalFormatted()).withStyle(ChatFormatting.WHITE)));
            lines.add(Component.literal("")
                    .append(Component.literal("/hr ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(handler.perHourFormatted()).withStyle(ChatFormatting.AQUA)));
            lines.add(Component.literal("")
                    .append(Component.literal("Last ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(handler.lastCatchFormatted()).withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(" · ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(handler.lastCatchNameTruncated()).withStyle(ChatFormatting.GRAY)));
        } else if (!"—".equals(handler.lastCatchNameTruncated())) {
            lines.add(Component.literal("")
                    .append(Component.literal("Last ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal(handler.lastCatchNameTruncated()).withStyle(ChatFormatting.GRAY)));
        }

        if (Configs.hudConfig.showProfitTrackerBreakdown.get() && handler.hasBreakdown()) {
            if (!peaceful) {
                lines.add(Component.literal("Sources").withStyle(ChatFormatting.YELLOW));
            }

            int maxCategories = Configs.hudConfig.profitTrackerBreakdownMaxCategories.get();
            int maxSubcategories = Configs.hudConfig.profitTrackerBreakdownMaxSubcategories.get();
            boolean showSubs = Configs.hudConfig.showProfitTrackerBreakdownSubcategories.get();

            List<ProfitTrackerHandler.ProfitBreakdownCategoryRow> rows = handler.getBreakdownRows();
            for (int i = 0; i < Math.min(maxCategories, rows.size()); i++) {
                ProfitTrackerHandler.ProfitBreakdownCategoryRow row = rows.get(i);
                MutableComponent categoryLine = Component.literal("")
                        .append(Component.literal(row.label).withStyle(ChatFormatting.GRAY));
                if (!peaceful) {
                    categoryLine
                            .append(Component.literal(" ").withStyle(ChatFormatting.GRAY))
                            .append(Component.literal(ProfitTrackerHandler.formatValue(row.total)).withStyle(ChatFormatting.WHITE))
                            .append(Component.literal(" (" + formatPercent(row.percentOfSession) + ")").withStyle(ChatFormatting.DARK_GRAY));
                }
                appendItemCount(categoryLine, row.itemCount, ChatFormatting.DARK_GRAY);
                lines.add(categoryLine);

                if (showSubs) {
                    for (int j = 0; j < Math.min(maxSubcategories, row.subRows.size()); j++) {
                        ProfitTrackerHandler.ProfitBreakdownSubRow subRow = row.subRows.get(j);
                        MutableComponent subLine = Component.literal("")
                                .append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY))
                                .append(Component.literal(subRow.label).withStyle(ChatFormatting.DARK_GRAY));
                        if (!peaceful) {
                            subLine
                                    .append(Component.literal(" ").withStyle(ChatFormatting.DARK_GRAY))
                                    .append(Component.literal(ProfitTrackerHandler.formatValue(subRow.total)).withStyle(ChatFormatting.GRAY));
                        }
                        appendItemCount(subLine, subRow.itemCount, ChatFormatting.DARK_GRAY);
                        lines.add(subLine);
                    }
                }
            }
        }

        int innerWidth = 0;
        for (Component line : lines) {
            innerWidth = Math.max(innerWidth, font.width(line));
        }
        int innerHeight = font.lineHeight * lines.size() + LINE_GAP * Math.max(0, lines.size() - 1);
        return new Layout(lines, innerWidth + PADDING * 2, innerHeight + PADDING * 2);
    }

    private static String formatPercent(double percent) {
        return String.format(Locale.US, "%.0f%%", percent);
    }

    private static void appendItemCount(MutableComponent line, long itemCount, ChatFormatting style) {
        if (itemCount > 0L) {
            line.append(Component.literal(" \u00B7 " + itemCount).withStyle(style));
        }
    }

    private static class Layout {
        private final List<Component> lines;
        private final int boxWidth;
        private final int boxHeight;

        private Layout(List<Component> lines, int boxWidth, int boxHeight) {
            this.lines = lines;
            this.boxWidth = boxWidth;
            this.boxHeight = boxHeight;
        }
    }
}
