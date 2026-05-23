package dannypx.foe.handler.renderer;

import dannypx.foe.handler.ScreenHandler;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.type.tuple.Pair;
import java.util.Map;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

public class PresetsScreenRenderHandler extends ScreenHandler {
    private static PresetsScreenRenderHandler INSTANCE = new PresetsScreenRenderHandler();

    public static PresetsScreenRenderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new PresetsScreenRenderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    //endregion

    //region Methods
    public boolean checkMouseScroll(Screen screen, double mouseX, double mouseY, double horizontalAmount, double verticalAmount, boolean consumed) {
        if(screen instanceof ContainerScreen containerScreen) {
            int syncId = containerScreen.getMenu().containerId;
            int moveSlot = -1;

            if(verticalAmount > 0) moveSlot = 46;
            else if (verticalAmount < 0) moveSlot = 52;

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
