package dannypx.foe.handler.logic;

import dannypx.foe.item.TagObject;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves a per-unit coin value from server item NBT ({@link TagObject#getMoney()}) or static fallbacks.
 */
public final class ItemValueResolver {
    private static final Map<Identifier, Float> STATIC_BY_ITEM = Map.of();

    private ItemValueResolver() {}

    public static float unitValueFor(TagObject tag) {
        float fromNbt = tag.getMoney();
        if (fromNbt > 0f) {
            return fromNbt;
        }
        ItemStack stack = tag.getItemStack();
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Float fixed = STATIC_BY_ITEM.get(id);
        if (fixed != null && fixed > 0f) {
            return fixed;
        }
        String label = stack.getHoverName().getString();
        if (label.contains("Lightning") && label.contains("Bottle")) {
            return 50_000f; // prototype static price; replace when server exposes value
        }
        return 0f;
    }
}
