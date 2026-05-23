package dannypx.foe.mixin.inject;

import dannypx.foe.entity.FishingHookEntityModel;
import dannypx.foe.handler.logic.ConnectionHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dannypx.foe.config.Configs;
import dannypx.foe.interfaces.IFishingHookEntity;
import dannypx.foe.interfaces.IFishingHookRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingHookRenderer.class)
public abstract class FishingHookRendererMixin {
    @Unique
    private Model<FishingHookRenderState> fishingHookModel;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectInit(EntityRendererProvider.Context context, CallbackInfo ci) {
        this.fishingHookModel = new FishingHookEntityModel<>(context.bakeLayer(FishingHookEntityModel.MODEL_LAYER));
    }

    @Inject(method = "vertex", at = @At("HEAD"), cancellable = true)
    private static void injectVertex(CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.rendererConfig.showNewFishingHook.get()
                && Configs.mixinConfig.fishingHookRendererMixinVertex.get()
        ) {
            ci.cancel();
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At("RETURN"))
    private void injectRender(FishingHookRenderState fishingHookRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if(ConnectionHandler.instance().isOnServer()
                && Configs.rendererConfig.showNewFishingHook.get()
                && Configs.mixinConfig.fishingHookRendererMixinRender.get()
        ) {
            poseStack.pushPose();
            poseStack.translate(0f, -0.0075f, 0f);
            submitNodeCollector.submitModel(this.fishingHookModel, fishingHookRenderState, poseStack, FishingHookEntityModel.RENDER_LAYER, fishingHookRenderState.lightCoords, OverlayTexture.NO_OVERLAY, fishingHookRenderState.outlineColor, null);
            poseStack.popPose();
        }

        ItemStack bait = ((IFishingHookRenderState) fishingHookRenderState).foer$getBaitStack();
        if(ConnectionHandler.instance().isOnServer()
                && Configs.rendererConfig.showBaitOnFishingHook.get()
                && bait != null
                && !bait.isEmpty()
                && !((IFishingHookRenderState) fishingHookRenderState).foer$isDisabledBait()
        ) {
            Minecraft minecraft = Minecraft.getInstance();
            if(minecraft.getEntityRenderDispatcher().camera != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.4, 0);

                poseStack.mulPose(Axis.YP.rotationDegrees(-minecraft.getEntityRenderDispatcher().camera.yRot()));

                ItemModelResolver itemModelManager = minecraft.getItemModelResolver();
                TrackingItemStackRenderState itemRenderState = new TrackingItemStackRenderState();

                itemModelManager.updateForTopItem(itemRenderState, bait, ItemDisplayContext.GROUND, minecraft.level, null, 0);
                itemRenderState.submit(poseStack, submitNodeCollector, fishingHookRenderState.lightCoords, OverlayTexture.NO_OVERLAY, 0);

                poseStack.popPose();
            }
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/projectile/FishingHook;Lnet/minecraft/client/renderer/entity/state/FishingHookRenderState;F)V", at = @At("TAIL"))
    private void injectUpdateRenderState(FishingHook fishingHookEntity, FishingHookRenderState fishingHookEntityState, float f, CallbackInfo ci) {
        ((IFishingHookRenderState) fishingHookEntityState).foer$setBaitStack(((IFishingHookEntity) fishingHookEntity).foer$getBaitStack());
        ((IFishingHookRenderState) fishingHookEntityState).foer$setDisabledBait(((IFishingHookEntity) fishingHookEntity).foer$isDisabledBait());
    }
}
