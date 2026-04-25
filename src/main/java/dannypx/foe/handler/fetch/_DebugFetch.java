package dannypx.foe.handler.fetch;

import dannypx.foe.type.tuple.Pair;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.MutableComponent;

public class _DebugFetch {
    public static List<String> _getHandlers() {
        return List.of(
                TabOverlayHandler.class.getName(),
                LocalPlayerHandler.class.getName(),
                ScoreboardHandler.class.getName(),
                BossEventHandler.class.getName(),
                TitleHandler.class.getName(),
                StatsScreenHandler.class.getName(),
                GenericContainerScreenHandler.class.getName()
        );
    }

    /// Handler, Map<Field, Pair<Value, Tooltip>>
    public static Map<String, Map<String, Pair<MutableComponent, MutableComponent>>> _getFields() {
        return Map.of(
                TabOverlayHandler.class.getName(), TabOverlayHandler.instance()._getFields(),
                LocalPlayerHandler.class.getName(), LocalPlayerHandler.instance()._getFields(),
                ScoreboardHandler.class.getName(), ScoreboardHandler.instance()._getFields(),
                BossEventHandler.class.getName(), BossEventHandler.instance()._getFields(),
                TitleHandler.class.getName(), TitleHandler.instance()._getFields(),
                StatsScreenHandler.class.getName(), StatsScreenHandler.instance()._getFields(),
                GenericContainerScreenHandler.class.getName(), GenericContainerScreenHandler.instance()._getFields(),
                HitResultHandler.class.getName(), HitResultHandler.instance()._getFields()
        );
    }
}
