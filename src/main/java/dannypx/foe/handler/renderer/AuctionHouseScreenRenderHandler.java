package dannypx.foe.handler.renderer;

import dannypx.foe.handler.ScreenHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.screens.widget.SearchBarWidget;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuctionHouseScreenRenderHandler extends ScreenHandler {
    private static AuctionHouseScreenRenderHandler INSTANCE = new AuctionHouseScreenRenderHandler();

    public static AuctionHouseScreenRenderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new AuctionHouseScreenRenderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public SearchBarWidget searchBarWidget;
    //endregion

    //region Methods
    public void init(Screen screen) {
        SearchHandler.instance().setFocused(false);
        SearchHandler.instance().setOnScreen(true);
        this.initWidgets(screen);
    }

    private void initWidgets(Screen screen) {
        if(LoadingHandler.instance().isLoadingDone()
                && Configs.mainConfig.enableMod.get()
        ) {
            List<AbstractWidget> widgets = new ArrayList<>();

            searchBarWidget = SearchHandler.getSearchBar(
                    minecraft.getWindow().getGuiScaledWidth() / 2 - 80,
                    minecraft.getWindow().getGuiScaledHeight() / 2 - 155,
                    160,
                    20
            );

            widgets.add(searchBarWidget);

            widgets.forEach(Screens.getButtons(screen)::add);
        }
    }

    public boolean checkMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, boolean consumed) {
        if(screen instanceof ContainerScreen containerScreen) {
            int syncId = containerScreen.getMenu().containerId;
            int moveSlot = -1;

            if(verticalAmount > 0) moveSlot = 46;
            else if (verticalAmount < 0) moveSlot = 51;

            if(moveSlot != -1
                    && !containerScreen.getMenu().getSlot(moveSlot).getItem().isEmpty()
                    && containerScreen.getMenu().getSlot(moveSlot).getItem().getItem() == Items.GUNPOWDER
                    && minecraft.gameMode != null
            ) {
                minecraft.gameMode.handleInventoryMouseClick(
                        syncId,
                        moveSlot,
                        0,
                        ClickType.PICKUP,
                        minecraft.player
                );
            }
        }

        return false;
    }

    public void onClose(Screen screen) {
        SearchHandler.instance().setOnScreen(false);
    }

    @Override
    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.render(screen, guiGraphics, mouseX, mouseY, tickDelta);

        searchBarWidget.render(guiGraphics, tickDelta);
    }

    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
