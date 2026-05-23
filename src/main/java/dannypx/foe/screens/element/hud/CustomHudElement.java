package dannypx.foe.screens.element.hud;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.fetch.TabOverlayHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.handler.store.CustomHudDataHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.Alignment;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.tuple.Triplet;
import dannypx.foe.screens.element.Element;
import dannypx.foe.screens.interfaces.ScreenConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public class CustomHudElement extends Element implements ScreenConstants {
    //region Fields
    // isCentre, isSmall, Line
    private List<Triplet<Boolean, Boolean, Component>> componentLines = new ArrayList<>();
    private Pair<Integer, Integer> contentDimensions = Pair.of(0, 0);

    private int boxWidth = 0;
    private int boxHeight = 0;

    private CustomHudDataHandler.CustomHud customHud;

    private static final Identifier BOX_TEXTURE = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "textures/gui/sprites/elements/sidebar_atlas.png");
    private static final int TEXTURE_WIDTH = 17;
    private static final int TEXTURE_HEIGHT = 11;
    private static final int BOX_PADDING = 5;
    private static final int MIN_WIDTH = 75;
    private static final int LINE_HEIGHT = Minecraft.getInstance().font.lineHeight + 1;
    //endregion

    public CustomHudElement(CustomHudDataHandler.CustomHud customHud, Component message) {
        super(75,
                50,
                customHud.getxPos() / 100f,
                customHud.getyPos() / 100f,
                customHud.getAlignment(),
                message,
                false);
        this.customHud = customHud;
    }

    //region Methods
    @Override
    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if(!customHud.isShowElement()) { return; }

        int scaledWidth = (int) (Minecraft.getInstance().getWindow().getGuiScaledWidth() * (1 / customHud.getScale()));
        int scaledHeight = (int) (Minecraft.getInstance().getWindow().getGuiScaledHeight() * (1 / customHud.getScale()));

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(customHud.getScale(), customHud.getScale());
        if(LoadingHandler.instance().isLoadingDone()
                && TabOverlayHandler.instance().isInInstance()
        ) {
            contentDimensions = this.assembleHud();
            boxWidth = contentDimensions.value1() + BOX_PADDING * 2 + PADDING * 2;
            boxHeight = contentDimensions.value2() + BOX_PADDING * 2 + PADDING_QUART * 2;

            int x = switch (customHud.getAlignment()) {
                case TOP_LEFT, BOTTOM_LEFT, LEFT -> Math.round(scaledWidth * xPos);
                case TOP, BOTTOM -> Math.round(scaledWidth * xPos) - boxWidth / 2;
                case TOP_RIGHT, BOTTOM_RIGHT, RIGHT -> scaledWidth
                        - Math.round(scaledWidth * xPos);
                default -> 0;
            };

            int y = switch (customHud.getAlignment()) {
                case TOP_LEFT, TOP_RIGHT, TOP -> Math.round(scaledHeight * yPos);
                case LEFT, RIGHT -> Math.round(scaledHeight * yPos) - boxHeight / 2;
                case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM -> scaledHeight
                        - Math.round(scaledHeight * yPos);
                default -> 0;
            };

            if(!componentLines.isEmpty()) {
                x = switch (customHud.getAlignment()) {
                    case TOP_LEFT, BOTTOM_LEFT, LEFT -> x;
                    case TOP, BOTTOM -> x - boxWidth / 2;
                    case TOP_RIGHT, BOTTOM_RIGHT, RIGHT -> x - boxWidth;
                    default -> 0;
                };

                y = switch (customHud.getAlignment()) {
                    case TOP_LEFT, TOP_RIGHT, TOP -> y;
                    case BOTTOM_LEFT, BOTTOM_RIGHT, BOTTOM -> y - boxHeight;
                    case LEFT, RIGHT -> y - boxHeight / 2;
                    default -> 0;
                };

                if(customHud.isShowBackground()) this.renderBox(guiGraphics, deltaTracker, x, y);
                this.renderComponent(guiGraphics, deltaTracker, x, y);
            }
        }
        guiGraphics.pose().popMatrix();
    }

    private void renderComponent(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int x, int y) {
        int componentX;
        int componentY;

        if(customHud.getAlignment() == Alignment.TOP || customHud.getAlignment() == Alignment.BOTTOM) {
            componentX = x + PADDING + BOX_PADDING + boxWidth / 2;
        } else {
            componentX = x + PADDING + BOX_PADDING;
        }

        if(customHud.getAlignment() == Alignment.LEFT || customHud.getAlignment() == Alignment.RIGHT) {
            componentY = y + PADDING_QUART + BOX_PADDING + boxHeight / 2;
        } else {
            componentY = y + PADDING_QUART + BOX_PADDING;
        }

        AtomicInteger line = new AtomicInteger(0);
        componentLines.forEach(componentParts -> {
            if(componentParts.value1()) {
                GuiGraphicsHelper.drawString(guiGraphics, Minecraft.getInstance().font, componentParts.value3(),
                        componentX - (PADDING + BOX_PADDING) + boxWidth / 2 - ComponentHelper.getWidth(Minecraft.getInstance().font, componentParts.value3(), componentParts.value2()) / 2,
                        componentY + line.getAndIncrement() * LINE_HEIGHT,
                        true, componentParts.value2(), true, componentParts.value2()
                        );
            } else {
                GuiGraphicsHelper.drawString(guiGraphics, Minecraft.getInstance().font, componentParts.value3(),
                        componentX,
                        componentY + line.getAndIncrement() * LINE_HEIGHT,
                        true, componentParts.value2(), true, componentParts.value2()
                );
            }
        });
    }

    private void renderBox(GuiGraphics guiGraphics, DeltaTracker deltaTracker, int x, int y) {
        int boxX = x;
        int boxY = y;

        if(customHud.getAlignment() == Alignment.TOP || customHud.getAlignment() == Alignment.BOTTOM) {
            boxX = boxX + boxWidth / 2;
        }

        if(customHud.getAlignment() == Alignment.LEFT || customHud.getAlignment() == Alignment.RIGHT) {
            boxY = boxY + boxHeight / 2;
        }

        int ATLAS_CORNER = 8;
        int ATLAS_BAR_WIDTH = 1;
        int ATLAS_BAR_HEIGHT = 5;
        int NIB_HEIGHT = 3;

        // Alpha Box
        guiGraphics.fill(
                boxX + BOX_PADDING, boxY + BOX_PADDING,
                boxX + this.boxWidth - BOX_PADDING, boxY + this.boxHeight - BOX_PADDING,
                0x7f000000
        );

        // Top Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX, boxY,
                0, NIB_HEIGHT,
                ATLAS_CORNER, ATLAS_CORNER,
                ATLAS_CORNER, ATLAS_CORNER,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Top
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX + ATLAS_CORNER, boxY,
                ATLAS_CORNER, NIB_HEIGHT,
                this.boxWidth - ATLAS_CORNER * 2, ATLAS_BAR_HEIGHT,
                ATLAS_BAR_WIDTH, ATLAS_BAR_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Top Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX + this.boxWidth - ATLAS_CORNER, boxY,
                ATLAS_CORNER + ATLAS_BAR_WIDTH, NIB_HEIGHT,
                ATLAS_CORNER, ATLAS_CORNER,
                ATLAS_CORNER, ATLAS_CORNER,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Bottom Left
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX, boxY + this.boxHeight - ATLAS_CORNER,
                0, 0,
                ATLAS_CORNER, ATLAS_CORNER,
                ATLAS_CORNER, ATLAS_CORNER,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Bottom
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX + ATLAS_CORNER, boxY + this.boxHeight - ATLAS_CORNER + NIB_HEIGHT,
                ATLAS_CORNER, NIB_HEIGHT,
                this.boxWidth - ATLAS_CORNER * 2, ATLAS_BAR_HEIGHT,
                ATLAS_BAR_WIDTH, ATLAS_BAR_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );

        // Bottom Right
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                BOX_TEXTURE,
                boxX + this.boxWidth - ATLAS_CORNER, boxY + this.boxHeight - ATLAS_CORNER,
                ATLAS_CORNER + ATLAS_BAR_WIDTH, 0,
                ATLAS_CORNER, ATLAS_CORNER,
                ATLAS_CORNER, ATLAS_CORNER,
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }

    private Pair<Integer, Integer> assembleHud() {
        componentLines.clear();

        AtomicBoolean hasData = new AtomicBoolean(false);

        customHud.getStringLines().forEach(componentParts -> {
            String componentString = componentParts.value1().replace("&", "§");
            Pair<Boolean, MutableComponent> componentLine = PlaceholderHandler.parsePlaceholderFromString(componentString);
            if(componentLine.value1()) {
                componentLines.add(Triplet.of(componentParts.value2(), componentParts.value3(), componentLine.value2()));
            }
            if(componentLine.value1() && !componentLine.value2().getString().isBlank()) {
                hasData.set(true);
            }
        });

        if(!hasData.get()) {
            componentLines.clear();
        }

        return Pair.of(
                Math.max(MIN_WIDTH, componentLines.stream()
                        .mapToInt(
                                line -> ComponentHelper.getWidth(Minecraft.getInstance().font, line.value3(), line.value2())
                        ).max().orElse(0)),
                LINE_HEIGHT * componentLines.size()
        );
    }
    //endregion
}
