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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChestScreenRenderHandler extends ScreenHandler {
    private static ChestScreenRenderHandler INSTANCE = new ChestScreenRenderHandler();

    public static ChestScreenRenderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChestScreenRenderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    SearchBarWidget searchBarWidget;
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
                    minecraft.getWindow().getGuiScaledHeight() / 2 - 133,
                    160,
                    20
            );

            widgets.add(searchBarWidget);

            widgets.forEach(Screens.getButtons(screen)::add);
        }
    }

    @Override
    public void render(Screen screen, GuiGraphics guiGraphics, int mouseX, int mouseY, float tickDelta) {
        super.render(screen, guiGraphics, mouseX, mouseY, tickDelta);

        searchBarWidget.render(guiGraphics, tickDelta);
    }

    public void onClose(Screen screen) {
        SearchHandler.instance().setOnScreen(false);
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
