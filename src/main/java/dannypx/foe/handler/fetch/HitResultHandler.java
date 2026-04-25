package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.ComponentValue;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.regex.Pattern;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class HitResultHandler extends Handler {
    private static HitResultHandler INSTANCE = new HitResultHandler();

    public static HitResultHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new HitResultHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private HitResult hitResult = null;
    private @Nullable BlockHitResult blockHitResult = null;
    private @Nullable EntityHitResult entityHitResult = null;

    private @Nullable ItemStack itemFrameItem = ItemStack.EMPTY;

    public @Nullable BlockHitResult getBlockHitResult() {
        return blockHitResult;
    }

    public @Nullable EntityHitResult getEntityHitResult() {
        return entityHitResult;
    }

    public @Nullable ItemStack getItemFrameItem() {
        return itemFrameItem;
    }

    public Pair<Boolean, PlaceholderValue> getRayCast(String[] params) {
        if(params.length > 0) {
            Pattern fieldPattern = Pattern.compile("^(block_hit_result|entity_hit_result|item_frame_item)$");

            if(fieldPattern.matcher(params[0]).matches()
                    && params.length == 1
            ) {
                return switch(params[0]) {
                    case "block_hit_result" -> {
                        if(getBlockHitResult() != null && !getBlockFromHitResult().getString().contains("Air")) {
                            yield PlaceholderHandler.getPlaceholderValue(new ComponentValue(getBlockFromHitResult()));
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "entity_hit_result" -> {
                        if(getEntityHitResult() != null && !getEntityHitResult().getEntity().getName().getString().isBlank()) {
                            yield PlaceholderHandler.getPlaceholderValue(new ComponentValue(getEntityHitResult().getEntity().getName()));
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    case "item_frame_item" -> {
                        if(getItemFrameItem() != ItemStack.EMPTY) {
                            yield PlaceholderHandler.getPlaceholderValue(new ComponentValue(getItemFrameItem().getHoverName()));
                        }
                        yield PlaceholderHandler.noResult();
                    }
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void tick() {
        hitResult = minecraft.hitResult;

        if(hitResult instanceof BlockHitResult blockHit) this.blockHitResult = blockHit;
        else this.blockHitResult = null;

        if(hitResult instanceof EntityHitResult entityHit) this.entityHitResult = entityHit;
        else this.entityHitResult = null;
        
        this.checkEntity();
    }

    private void checkEntity() {
        if(this.entityHitResult != null && entityHitResult.getEntity() instanceof ItemFrame itemFrameEntity) {
            itemFrameItem = itemFrameEntity.getItem();
        } else {
            itemFrameItem = ItemStack.EMPTY;
        }
    }

    private MutableComponent getBlockFromHitResult() {
        if(getBlockHitResult() != null && minecraft.level != null) {
            BlockPos blockPos = getBlockHitResult().getBlockPos();
            Block block = minecraft.level.getBlockState(blockPos).getBlock();
            return block.getName();
        }
        return null;
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "entityHitResult", Pair.of(getEntityHitResult() != null ? getEntityHitResult().getEntity().getName().copy() : Component.empty(), Component.empty()),
                "blockHitResult" , Pair.of(getBlockHitResult() != null ? getBlockFromHitResult() : Component.empty(), Component.empty())
        );
    }
    //endregion
}
