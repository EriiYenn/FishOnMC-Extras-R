package dannypx.foe.handler.logic;

import dannypx.foe.handler.store.ProfitPricingDataHandler;
import dannypx.foe.item.TagObject;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Resolves a per-unit coin value from server item NBT ({@link TagObject#getMoney()}) or static fallbacks.
 */
public final class ItemValueResolver {
    private static final Map<Identifier, Float> STATIC_BY_ITEM = Map.of();

    private ItemValueResolver() {}

    public static float unitValueFor(TagObject tag) {
        float fromNbt = profitMoneyFromNbt(tag);
        if (fromNbt > 0f) {
            return fromNbt;
        }
        ItemStack stack = tag.getItemStack();
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        Float fixed = STATIC_BY_ITEM.get(id);
        if (fixed != null && fixed > 0f) {
            return fixed;
        }

        float configured = ProfitPricingDataHandler.instance().getValue(stack);
        if (configured > 0f) {
            return configured;
        }

        float fromLore = valueFromLoreDollars(tag);
        if (fromLore > 0f) {
            return fromLore;
        }
        return 0f;
    }

    /**
     * Fish on MC can store {@link TagObject#MONEY} on the root tag or in later {@code renderInfo} compounds.
     * Keeps {@link TagObject#getMoney()} aligned with upstream (first renderInfo entry only); profit uses the max here.
     */
    private static float profitMoneyFromNbt(TagObject tag) {
        float best = 0f;
        if (tag.contains(TagObject.MONEY)) {
            best = Math.max(best, tag.getFloat(TagObject.MONEY));
        }
        ListTag renderInfo = tag.getRenderInfo();
        for (int i = 0; i < renderInfo.size(); i++) {
            Tag t = renderInfo.get(i);
            if (t instanceof CompoundTag ct && ct.contains(TagObject.MONEY)) {
                Tag moneyTag = ct.get(TagObject.MONEY);
                if (moneyTag instanceof NumericTag num) {
                    best = Math.max(best, (float) num.doubleValue());
                } else {
                    best = Math.max(best, ct.getFloat(TagObject.MONEY).orElse(0f));
                }
            }
        }
        return best;
    }

    /**
     * When {@code money} is not present in NBT, some server items only expose a dollar amount in the lore
     * (e.g. "Value: $1.2K"). Picks the largest parsed amount to avoid small incidental {@code $} in text.
     */
    private static float valueFromLoreDollars(TagObject tag) {
        List<Component> lore = tag.getLore();
        if (lore.isEmpty()) {
            return 0f;
        }
        float best = 0f;
        for (Component line : lore) {
            float parsed = MoneyParseUtil.parseLargestAmount(line.getString());
            if (parsed > best) {
                best = parsed;
            }
        }
        return best;
    }
}
