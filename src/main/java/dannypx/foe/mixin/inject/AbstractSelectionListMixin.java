package dannypx.foe.mixin.inject;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.mixin.accessor.AbstractSelectionListEntryAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class AbstractSelectionListMixin {

    @Shadow
    @Final
    private List<?> children;

    @Shadow
    @Final
    protected int defaultEntryHeight;

    @Shadow
    public abstract int getNextY();

    @Shadow
    public abstract int getRowLeft();

    @Shadow
    public abstract int getRowWidth();

    @Shadow
    protected abstract void repositionEntries();

    @ModifyReturnValue(method = "children", at = @At("RETURN"))
    private List<?> modifyChildren(List<?> original) {
        return this.children;
    }

    @Inject(
            method = "addEntry(Lnet/minecraft/client/gui/components/AbstractSelectionList$Entry;)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectAddEntry(
            @Coerce Object entry,
            CallbackInfoReturnable<Integer> cir
    ) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.abstractSelectionListMixinAddEntry.get()
        ) {
            List<Object> list = (List<Object>) this.children;

            ((AbstractSelectionListEntryAccessor) entry).callSetX(this.getRowLeft());
            ((AbstractSelectionListEntryAccessor) entry).callSetWidth(this.getRowWidth());
            ((AbstractSelectionListEntryAccessor) entry).callSetY(this.getNextY());
            ((AbstractSelectionListEntryAccessor) entry).callSetHeight(this.defaultEntryHeight);

            list.add(entry);
            this.repositionEntries();
            cir.setReturnValue(list.size() - 1);
            cir.cancel();
        }
    }

    // Rewritten old functionality
    @Inject(
            method = "addEntry(Lnet/minecraft/client/gui/components/AbstractSelectionList$Entry;I)I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectAddEntryWithHeight(
            @Coerce Object entry,
            int i,
            CallbackInfoReturnable<Integer> cir
    ) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.mainConfig.enableMod.get()
                && Configs.mixinConfig.abstractSelectionListMixinAddEntry.get()
        ) {
            List<Object> list = (List<Object>) this.children;

            ((AbstractSelectionListEntryAccessor) entry).callSetX(this.getRowLeft());
            ((AbstractSelectionListEntryAccessor) entry).callSetWidth(this.getRowWidth());
            ((AbstractSelectionListEntryAccessor) entry).callSetY(this.getNextY());
            ((AbstractSelectionListEntryAccessor) entry).callSetHeight(this.defaultEntryHeight);

            list.add(i, entry);
            this.repositionEntries();
            cir.setReturnValue(list.size() - 1);
            cir.cancel();
        }
    }
}
