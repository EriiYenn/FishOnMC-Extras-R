package dannypx.foe.item;

import com.mojang.serialization.DataResult;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.helper.ItemStackHelper;
import dannypx.foe.helper.UUIDHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TagObject {

    protected final Minecraft minecraft = Minecraft.getInstance();

    public static final String ID = "id";
    public static final String CATCHER = "catcher";
    public static final String UUID_KEY = "uuid";
    public static final String COUNTER = "counter";
    public static final String TYPE = "type";
    public static final String RARITY = "rarity";
    public static final String RENDER_INFO = "renderInfo";
    public static final String MONEY = "money";

    public static final int RARITY_LINE = 1;
    public static final int RARITY_SIBLING = 1;

    //From the bottom
    public static final int SHOP_PRICE_LINE = 5;

    public static final int BORDER_LINE = 2;

    protected final CompoundTag compoundTag;
    protected final ItemStack itemStack;

    protected TagObject() {
        this.compoundTag = new CompoundTag();
        this.itemStack = ItemStack.EMPTY;
    }

    protected TagObject(@NotNull CompoundTag compoundTag, ItemStack itemStack) {
        this.compoundTag = compoundTag;
        this.itemStack = itemStack.copy();
    }

    //region Generics
    public boolean contains(String key) {
        return this.compoundTag.contains(key);
    }

    public int getInt(String key) {
        return this.compoundTag.getInt(key).orElse(0);
    }

    public float getFloat(String key) {
        return this.compoundTag.getFloat(key).orElse(0.0f);
    }

    public String getString(String key) {
        return this.compoundTag.getString(key).orElse("");
    }

    public boolean getBoolean(String key) {
        return this.compoundTag.getBoolean(key).orElse(false);
    }

    public UUID getUuid(String key) {
        return UUIDHelper.getUUID(this.compoundTag.getIntArray(key).orElse(new int[]{0}));
    }

    public ListTag getList(String key) {
        return this.compoundTag.getList(key).orElse(new ListTag());
    }

    public Tag get(String key) {
        return this.compoundTag.get(key);
    }

    public byte getType(String key) {
        return this.compoundTag.get(key).getId();
    }
    //endregion

    public UUID getID() {
        return this.getUuid(ID);
    }

    public UUID getPlayerUUID() {
        if(this.contains(CATCHER)) {
            return this.getUuid(CATCHER);
        } else if (this.contains(UUID_KEY)) {
            return this.getUuid(UUID_KEY);
        }
        return null;
    }

    //region Generic
    public Component getName() {
        Component name = this.itemStack.getCustomName();
        return name != null ? name : this.itemStack.getHoverName();
    }

    public int getCount() {
        if(this.contains(COUNTER)) {
            return this.getInt(COUNTER);
        }
        return this.itemStack.getCount();
    }

    public boolean isOwn() {
        if(minecraft.player != null && getPlayerUUID() != null) {
            return minecraft.player.getUUID().equals(getPlayerUUID());
        }
        return false;
    }

    public @NotNull String getType() {
        if(this.contains(TYPE)) {
            return this.getString(TYPE);
        } else if (this.contains(FishTagObject.FISH)) {
            return "fish";
        }
        return "";
    }

    public @NotNull String getRarity() {
        if(this.contains(RARITY)) {
            return this.getString(RARITY);
        }
        return "";
    }

    public Component getRarityComponent() {
        if(this.itemStack.get(DataComponents.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            try {
                List<Component> componentList = this.getLore();
                Component rating = componentList.get(RARITY_LINE).getSiblings().get(RARITY_SIBLING);
                return ComponentHelper.trim(rating);
            } catch (ArrayIndexOutOfBoundsException e) {
                return Component.empty();
            }
        }
        return Component.empty();
    }

    public Component getBorderComponent() {
        if(this.itemStack.get(DataComponents.LORE) != null
                && !this.getLore().isEmpty()
        ) {
            List<Component> componentList = this.getLore();
            if(!componentList.isEmpty()) {
                Component borderLine = componentList.get(BORDER_LINE);
                if (!borderLine.getSiblings().isEmpty()) return borderLine.getSiblings().getFirst();
            }
        }
        return Component.empty();
    }

    public static Component getBorderComponent(ItemStack itemStack) {
        if(itemStack.get(DataComponents.LORE) != null) {
            List<Component> componentList = itemStack.get(DataComponents.LORE).lines();
            if(!componentList.isEmpty()) {
                Component borderLine = componentList.get(BORDER_LINE);
                if (!borderLine.getSiblings().isEmpty()) return borderLine.getSiblings().getFirst();
            }
        }
        return Component.empty();
    }

    public List<Component> getLore() {
        if(this.itemStack.get(DataComponents.LORE) != null) {
            return Objects.requireNonNull(this.itemStack.get(DataComponents.LORE)).lines();
        }
        return List.of();
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    protected List<TagObject> getItemStackList(String key) {
        if(this.contains(key)) {
            DataResult<List<ItemStack>> result =
                    ItemStack.CODEC.listOf().parse(NbtOps.INSTANCE, this.get(key));
            List<ItemStack> itemStackList;

            try {
                itemStackList = result.getPartialOrThrow();
            } catch (Exception e) {
                itemStackList = List.of();
            }

            return itemStackList.stream().map(item -> {
                Pair<Boolean, TagObject> validatedItem = ValidateItem.isType(item);
                return validatedItem.value2();
            }).toList();
        }
        return List.of();
    }

    public ListTag getRenderInfo() {
        if(this.contains(RENDER_INFO)) {
            return (ListTag) this.get(RENDER_INFO);
        }
        return new ListTag();
    }

    public float getMoney() {
        ListTag renderInfo = this.getRenderInfo();
        if(!renderInfo.isEmpty()) {
            if(((CompoundTag) renderInfo.getFirst()).contains(MONEY)) {
                return ((CompoundTag) renderInfo.getFirst()).getFloat(MONEY).orElse(0.0f);
            }
        }
        return 0f;
    }

    protected boolean isAuctionItem() {
        ListTag listTag = this.getRenderInfo();

        if(!listTag.isEmpty()) {
            return ((CompoundTag) listTag.getFirst()).contains(MONEY);
        }
        return false;
    }
    //endregion

    public static TagObject of(@NotNull CompoundTag compoundTag, @NotNull ItemStack itemStack) {
        return new TagObject(compoundTag, itemStack);
    }

    public static TagObject empty() {
        return new TagObject(ItemStackHelper.getTag(ItemStack.EMPTY), ItemStack.EMPTY);
    }
}
