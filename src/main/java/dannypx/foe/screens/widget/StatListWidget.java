package dannypx.foe.screens.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.screens.interfaces.ScreenConstants;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StatListWidget extends AbstractSelectionList<StatListWidget.@NotNull StatEntry> implements ScreenConstants {

    public StatListWidget(Minecraft client, int width, int height, int x, int y, int itemHeight) {
        super(client, width, height, y, itemHeight);
        this.setPosition(x - ((width / 4) / 3), y);
    }

    @Override
    protected void renderListItems(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.disableScissorForRenderTypeDraws();
        super.renderListItems(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public int getRowLeft() {
        return this.getX();
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + width - 14;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {}

    @Override
    public int addEntry(StatEntry entry) {
        return super.addEntry(entry);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void renderListBackground(@NotNull GuiGraphics guiGraphics) {}

    @Override
    protected void renderListSeparators(@NotNull GuiGraphics guiGraphics) {}

    public static class StatEntry extends ContainerObjectSelectionList.Entry<@NotNull StatEntry>{
        boolean isHeader;
        Component category;
        Component field1;
        Component field2;
        Component field3;
        List<ItemStack> itemStacks;
        int width = 160;

        public StatEntry(Component category, Component field1, Component field2, Component field3, List<ItemStack> itemStacks, boolean isHeader) {
            this.isHeader = isHeader;
            this.category = category;
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
            this.setHeight(16);
            this.itemStacks = itemStacks;
        }

        public StatEntry(Component category, boolean isHeader) {
            this.isHeader = isHeader;
            this.category = category;
            this.field1 = Component.empty();
            this.field2 = Component.empty();
            this.field3 = Component.empty();
            this.itemStacks = List.of();
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of();
        }

//        public void setWidth(int width) {
//            this.width = width;
//        }

        @Override
        public void renderContent(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
            if(!LoadingHandler.instance().isLoadingDone()) {
                return;
            }

            Font font = Minecraft.getInstance().font;
            int height = font.lineHeight;
            int posY = getY() - height / 2 + 4;


            if(isHeader) {
                int headerX = getX() + width / 2;
                int headerWidth = font.width(
                        Component.literal(ComponentHelper.smallCaps(category.getString())).setStyle(category.getStyle())
                );

                GuiGraphicsHelper.drawString(guiGraphics, font, category,
                        headerX - headerWidth / 2, posY,
                        true, true, true, true);
            } else {
                Component fieldComponent;
                if(!itemStacks.isEmpty()) {
                    fieldComponent = field2;
                } else if(!Objects.equals(field2, Component.empty())) {
                    fieldComponent = ComponentHelper.concat(field1, Component.literal(" "), field2);
                } else {
                    fieldComponent = field1;
                }
                int fieldComponentX = itemStacks.isEmpty() ? getX() + 17 + PADDING_QUART : getX() + 17 + PADDING_QUART + 16 + PADDING_QUART ;

                int field3X = getX() + (width/4) * 3;
                int field3Width = font.width(ComponentHelper.smallCaps(field3.getString()));

                GuiGraphicsHelper.drawString(guiGraphics, font, fieldComponent,
                        fieldComponentX, posY,
                        true, true, true, true);

                GuiGraphicsHelper.drawString(guiGraphics, font, field3,
                        field3X - field3Width / 2, posY,
                        true, true, true, true);

                if(!itemStacks.isEmpty()) {
                    long seconds = System.currentTimeMillis() / 1000;
                    int itemIndex = (int) (seconds % itemStacks.size());
                    guiGraphics.renderItem(itemStacks.get(itemIndex), getX() + 17 + PADDING_QUART, getY() - 16 / 2 + 4);
                }
            }
        }
    }
}
