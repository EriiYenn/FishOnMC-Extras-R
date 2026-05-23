package dannypx.foe.mixin.inject;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {

    @Shadow
    @Final
    private List<?> children;

    @ModifyReturnValue(method = "children", at = @At("RETURN"))
    private List<?> modifyChildren(List<?> original) {
        return this.children;
    }
}
