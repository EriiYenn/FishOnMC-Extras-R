package dannypx.foe.item;

import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.helper.ItemStackHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class FishTagObject extends TagObject {
    public static final String FISH = "fish";
    public static final String FISH_SIZE = "size";
    public static final String VARIANT = "variant";
    public static final String LENGTH = "length";
    public static final String WEIGHT = "weight";

    public static final int FISH_SIZE_LINE = 7;
    public static final int FISH_SIZE_SIBLING = 2;

    public static final int VARIANT_LINE = 1;
    public static final int VARIANT_SIBLING = 2;

    public FishTagObject(CompoundTag compoundTag, ItemStack itemStack) {
        super(compoundTag, itemStack);
    }

    public String getFish() {
        if(this.contains(FISH)) {
            return this.getString(FISH);
        }
        return "";
    }

    public float getLength() {
        if(this.contains(LENGTH)) {
            return this.getFloat(LENGTH);
        }
        return 0f;
    }
    public float getWeight() {
        if(this.contains(WEIGHT)) {
            return this.getFloat(WEIGHT);
        }
        return 0f;
    }

    public String getVariant() {
        if(this.contains(VARIANT)) {
            return this.getString(VARIANT);
        }
        return "";
    }

    public Component getVariantComponent() {
        if(this.itemStack.get(DataComponents.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Component> componentList = this.getLore();
                Component variant = componentList.get(VARIANT_LINE).getSiblings().get(VARIANT_SIBLING);
                return ComponentHelper.trim(variant);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Component.empty();
            }
        }
        return Component.empty();
    }

    public String getFishSize() {
        return this.getString(FISH_SIZE);
    }

    public Component getFishSizeComponent() {
        if(this.itemStack.get(DataComponents.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Component> componentList = this.getLore();
                Component fishSize = componentList.get(FISH_SIZE_LINE).getSiblings().get(FISH_SIZE_SIBLING);;
                return ComponentHelper.trim(fishSize);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Component.empty();
            }
        }
        return Component.empty();
    }

    public static FishTagObject of(@NotNull CompoundTag compoundTag, @NotNull ItemStack itemStack) {
        return new FishTagObject(compoundTag, itemStack);
    }

    public static FishTagObject empty() {
        return new FishTagObject(ItemStackHelper.getTag(ItemStack.EMPTY), ItemStack.EMPTY);
    }
}
