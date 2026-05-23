package dannypx.foe.mixin.inject;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.logic.ConnectionHandler;
import dannypx.foe.handler.logic.LoadingHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    public <E extends Entity> void injectShouldRender(E entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if(ConnectionHandler.instance().isOnServer()
                && LoadingHandler.instance().isLoadingDone()
                && Configs.mixinConfig.entityRenderDispatcherMixinShouldRender.get()
        ) {
            if(Minecraft.getInstance().options.hideGui
                    && entity instanceof Display.TextDisplay textDisplay
                    && textDisplay.getBillboardConstraints() != Display.BillboardConstraints.FIXED
            ) {
                cir.setReturnValue(false);
            } else if (!Configs.rendererConfig.showPlayerNamePlate.get()
                    && entity instanceof Display.TextDisplay textDisplay
                    && textDisplay.getText().getString().contains("\uF064")
            ) {
                cir.setReturnValue(false);
            }
        }
    }
}
