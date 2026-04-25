package dannypx.foe.handler.logic;

import dannypx.foe.handler.fetch.HitResultHandler;
import dannypx.foe.type.tuple.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.MutableComponent;

public class _DebugLogic {
    public static List<String> _getHandlers() {
        return List.of(
                ConnectionHandler.class.getName(),
                LoadingHandler.class.getName(),
                KeyBindHandler.class.getName(),
                InventoryHandler.class.getName(),
                NotifierHandler.class.getName(),
                HitResultHandler.class.getName(),
                SearchHandler.class.getName()
        );
    }

    /// Handler, Map<Field, Pair<Value, Tooltip>>
    public static Map<String, Map<String, Pair<MutableComponent, MutableComponent>>> _getFields() {
        return Map.of(
                ConnectionHandler.class.getName(), ConnectionHandler.instance()._getFields(),
                LoadingHandler.class.getName(), LoadingHandler.instance()._getFields(),
                KeyBindHandler.class.getName(), KeyBindHandler.instance()._getFields(),
                InventoryHandler.class.getName(), InventoryHandler.instance()._getFields(),
                NotifierHandler.class.getName(), NotifierHandler.instance()._getFields(),
                SearchHandler.class.getName(), SearchHandler.instance()._getFields()
        );
    }
}
