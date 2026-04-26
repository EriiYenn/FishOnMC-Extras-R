package dannypx.foe.screens.widget;

import dannypx.foe.handler.logic.SearchHandler;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.NotNull;

public class SearchBarWidget extends EditBox {
    private final List<Component> hoverInfo;
    private Font font;

    public SearchBarWidget(Font font, int x, int y, int width, int height, Component component, List<Component> hoverInfo) {
        super(font, x, y, width, height, null, component);
        this.hoverInfo = hoverInfo;
        this.font = font;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        SearchHandler.instance().setFocused(this.isFocused());
    }


    public void render(GuiGraphics guiGraphics, float tickDelta) {
        if(this.isHovered() && this.isFocused()) {
            guiGraphics.pose().pushMatrix();
            try {
                float scale = .75f;
                guiGraphics.pose().translate(0f, getY() + getHeight());
                guiGraphics.pose().scale(scale, scale);

                int padding = 4;
                int lineHeight = font.lineHeight + 1;
                int length = hoverInfo.stream().map(font::width).max(Integer::compareTo).orElse(0);
                int lines = hoverInfo.size() * lineHeight;
                int x = (int) (((float) Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2) * (1 / scale));

                guiGraphics.fill(x - length / 2 - padding, 0, x + length / 2 + padding, padding * 2 + lines, CommonColors.BLACK);

                guiGraphics.hLine(x - length / 2 - padding, x + length / 2 + padding, 0, CommonColors.GRAY);
                guiGraphics.hLine(x - length / 2 - padding, x + length / 2 + padding, padding * 2 + lines, CommonColors.GRAY);
                guiGraphics.vLine(x - length / 2 - padding, 0, padding * 2 + lines, CommonColors.GRAY);
                guiGraphics.vLine(x + length / 2 + padding, 0, padding * 2 + lines, CommonColors.GRAY);

                AtomicInteger count = new AtomicInteger(0);
                hoverInfo.forEach(component -> guiGraphics.drawString(font, component, x - length / 2, padding + count.getAndIncrement() * lineHeight, CommonColors.WHITE, true));
            } finally {
                guiGraphics.pose().popMatrix();
            }
        }
    }
}
