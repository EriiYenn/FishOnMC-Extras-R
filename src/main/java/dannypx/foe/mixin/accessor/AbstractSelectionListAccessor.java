package dannypx.foe.mixin.accessor;

import net.minecraft.client.gui.components.AbstractSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor<E> {
    @Accessor(value = "children")
    List<E> getChildren();

    @Invoker("repositionEntries")
    void callRepositionEntries();
}
