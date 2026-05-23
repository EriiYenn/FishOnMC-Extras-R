package dannypx.foe.handler.renderer;

import dannypx.foe.FishOnMCExtras;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.SearchHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.helper.GuiGraphicsHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.*;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.config.Configs;
import java.util.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

public class ItemRendererHandler extends Handler {
    private static ItemRendererHandler INSTANCE = new ItemRendererHandler();

    public static ItemRendererHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemRendererHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private final Identifier petItemMarker = Identifier.fromNamespaceAndPath(FishOnMCExtras.MOD_ID, "icons/pet_item");
    //endregion

    //region Methods
    public void drawRarityMarker(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showRarityMarker.get()) {
            return;
        }

        Pair<Boolean, TagObject> validateItem = ValidateItem.isServerItem(stack);

        if(!this.checkIfBlacklisted(validateItem.value2())
                && !validateItem.value2().getRarity().isBlank()
        ) {
            Component rarityComponent = Component.literal(ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishTagObject.RARITY, new HashMap<>())
                    .getOrDefault(validateItem.value2().getRarity().toLowerCase(Locale.US), Component.empty()).getString().trim());
            if(rarityComponent.getString().isBlank()) rarityComponent = Component.literal(validateItem.value2().getRarityComponent().getString());

            if(!Objects.equals(rarityComponent, Component.empty())) {
                int markerX = x;
                int markerY = y - 1;

                guiGraphics.pose().pushMatrix();

                //TOP
                int bgX = markerX;
                int bgY = markerY - 1;
                guiGraphics.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                guiGraphics.drawString(font, rarityComponent, bgX, bgY, CommonColors.LIGHT_GRAY, false);
                guiGraphics.disableScissor();

                //BOTTOM
                bgY = markerY + 1;
                guiGraphics.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                guiGraphics.drawString(font, rarityComponent, bgX, bgY, CommonColors.LIGHT_GRAY, false);
                guiGraphics.disableScissor();

                //LEFT
                bgX = markerX - 1;
                bgY = markerY;
                guiGraphics.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                guiGraphics.drawString(font, rarityComponent, bgX, bgY, CommonColors.LIGHT_GRAY, false);
                guiGraphics.disableScissor();

                //RIGHT
                bgX = markerX + 1;
                guiGraphics.enableScissor(bgX, bgY + 2, bgX + 2, bgY + 4);
                guiGraphics.drawString(font, rarityComponent, bgX, bgY, CommonColors.LIGHT_GRAY, false);
                guiGraphics.disableScissor();

                guiGraphics.enableScissor(markerX, bgY + 2, markerX + 2, bgY + 4);
                guiGraphics.drawString(font, rarityComponent, markerX, markerY, CommonColors.WHITE, false);
                guiGraphics.disableScissor();

                guiGraphics.pose().popMatrix();
            }
        }
    }

    private boolean checkIfBlacklisted(TagObject tagObject) {
        if(!Configs.rendererConfig.blackListItems.get().isBlank()) {
            List<String> blacklistedItems = Arrays.stream(Configs.rendererConfig.blackListItems.get().split(",")).map(String::trim).toList();
            return blacklistedItems.contains(tagObject.getType());
        }
        return false;
    }

    public void drawStackCount(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        this.drawStackCount(guiGraphics, font, stack, x, y, true);
    }

    public void drawStackCount(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y, boolean isSmall) {
        Pair<Boolean, TagObject> validatedItem = ValidateItem.isServerItem(stack);

        int count = Configs.rendererConfig.showStackCountOnBait.get()
                ? validatedItem.value2().getCount()
                : stack.getCount();
        Component countComponent = isSmall
                ? ComponentHelper.literal(ComponentHelper.smallCaps(ComponentHelper.shortenNumber(count, 0)))
                : ComponentHelper.literal(ComponentHelper.shortenNumber(count, 0));
        int countWidth = font.width(countComponent);

        if(count > 1) GuiGraphicsHelper.drawString(guiGraphics, font, countComponent,
                x + 19 - 2 - countWidth, isSmall ? y + 6 + 4 : y + 6 + 3,
                true,
                isSmall,
                false,
                isSmall
        );
    }

    public void drawSearchItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if(SearchHandler.instance().isOnScreen()
                && SearchHandler.instance().filterItem(stack)) {
            guiGraphics.hLine(x, x + 16, y, CommonColors.RED);
            guiGraphics.hLine(x, x + 16, y + 16, CommonColors.RED);
            guiGraphics.vLine(x, y, y + 16, CommonColors.RED);
            guiGraphics.vLine(x + 16, y, y + 16, CommonColors.RED);
        }
    }


    public void drawPetItemEquipped(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        if(!Configs.rendererConfig.showPetEquippedMarker.get()) {
            return;
        }

        Pair<Boolean, PetTagObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1() && (validatedPet.value2().contains(PetTagObject.ITEM) || validatedPet.value2().contains(PetTagObject.SKIN))) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, petItemMarker, x, y, 16, 16, CommonColors.WHITE);
        }
    }

    public void drawFishSize(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, FishTagObject> validatedFish = ValidateItem.isFish(stack);

        if(validatedFish.value1()
                && !validatedFish.value2().getFishSize().isBlank()
        ) {
            Component sizeComponent = ConstantDataHandler.instance().getConstantData().fishData
                    .getOrDefault(FishTagObject.FISH_SIZE, new HashMap<>())
                    .getOrDefault(validatedFish.value2().getFishSize().toLowerCase(Locale.US), Component.empty());
            if(sizeComponent.getString().isBlank()) sizeComponent = validatedFish.value2().getFishSizeComponent();

            if(!sizeComponent.getString().isEmpty()) {
                sizeComponent = ComponentHelper.substring(sizeComponent, 0, 1);

                guiGraphics.drawString(font, sizeComponent, x + 17 - font.width(sizeComponent), y + 18 - font.lineHeight, CommonColors.WHITE, true);
            }
        }
    }

    public void drawPetRating(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, PetTagObject> validatedPet = ValidateItem.isPet(stack);

        if(validatedPet.value1()
                && !validatedPet.value2().getRatingComponent().getString().isBlank()
        ) {
            Component ratingComponent = validatedPet.value2().getRatingComponent();

            if(!ratingComponent.getString().isEmpty()) {
                ratingComponent = ComponentHelper.substring(ratingComponent, 0, 1);

                guiGraphics.drawString(font, ratingComponent, x + 17 - font.width(ratingComponent), y + 18 - font.lineHeight, CommonColors.WHITE, true);
            }
        }
    }

    public void drawArmorQuality(GuiGraphics guiGraphics, Font font, ItemStack stack, int x, int y) {
        Pair<Boolean, ArmorTagObject> validatedArmor = ValidateItem.isArmor(stack);

        if(validatedArmor.value1()
                && !validatedArmor.value2().getQualityComponent().getString().isBlank()
        ) {
            Component qualityArmor = validatedArmor.value2().getQualityComponent();
            Component qualityRaw = ComponentHelper.substring(qualityArmor, 0, qualityArmor.getString().length() - 1);
            Component qualityComponent = Component.literal(ComponentHelper.smallCaps(qualityRaw.getString())).setStyle(qualityArmor.getStyle());

            if(!qualityComponent.getString().isEmpty()) {
                guiGraphics.drawString(font, qualityComponent, x + 17 - font.width(qualityComponent), y + 17 - font.lineHeight, CommonColors.WHITE, true);
            }
        }
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
        );
    }
    //endregion
}
