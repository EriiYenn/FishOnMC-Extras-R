package dannypx.foe.handler.fetch;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.KeyBindHandler;
import dannypx.foe.helper.KeyBindHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.ArmorTagObject;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.tuple.Pair;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class TooltipHandler extends Handler {
    private static TooltipHandler INSTANCE = new TooltipHandler();

    public static TooltipHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TooltipHandler();
        }
        return INSTANCE;
    }

    //region Fields
    //endregion

    //region Methods
    public void fetchTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> components) {
        Pair<Boolean, TagObject> validatedItem = ValidateItem.isServerItem(itemStack);
        if(validatedItem.value1()) {
            Pair<Boolean, ArmorTagObject> validatedArmor = ValidateItem.isArmor(validatedItem.value2());
            if(validatedArmor.value1()) this.setArmorRolls(validatedArmor.value2(), components);

            if(ValidateItem.isAuctionItem(validatedItem.value2())) {
                this.setPricesPerItem(validatedItem.value2(), components);
            } else if(this.isTackleShopItem(components)) {
                this.setPricesPerItemRaw(validatedItem.value2(), components);
            }
        } else {
            if(itemStack.getItem() == Items.ENDER_EYE
                    && itemStack.get(DataComponents.LORE) != null
                    && itemStack.get(DataComponents.LORE).lines().getFirst().getString().contains("Bonus Slot")
            ) {
                this.setArmorRoll(itemStack, components);
            }
        }
    }

    private void setArmorRoll(ItemStack itemStack, List<Component> components) {
        for (int i = 0; i < ArmorRollScreenHandler.instance().getRollList().size(); i++) {
            ItemStack listItem = ArmorRollScreenHandler.instance().getRollList().get(i);

            if(ItemStack.isSameItemSameComponents(listItem, itemStack)) {
                Component border = TagObject.getBorderComponent(itemStack);
                Component borderComponent = Component.literal(border.getString().trim())
                        .setStyle(border.getStyle())
                        .append("   ");

                int sizeOfLine = -1;
                for (int j = 0; j < components.size(); j++) {
                    if(components.get(j).getString().contains("(+")
                            && components.get(j).getString().contains("%)")
                    ) sizeOfLine = j;
                }

                if(sizeOfLine != -1) {
                    ArmorTagObject armor = ArmorRollScreenHandler.instance().getArmor();

                    int rolls = armor.getArmorRollRolls(i);
                    int tier = i + 1;
                    int money = ArmorTagObject.calculateMoneyRolls(rolls, tier);

                    Component moneyRoll = ComponentHelper.concat(
                            borderComponent,
                            Component.literal(ComponentHelper.smallCaps("rolls: ")).withStyle(ChatFormatting.GRAY),
                            Component.literal(String.valueOf(rolls - 1)).withStyle(ChatFormatting.YELLOW),
                            Component.literal("x ").withStyle(ChatFormatting.WHITE),
                            Component.literal(ComponentHelper.smallCaps("| spent: ")).withStyle(ChatFormatting.GRAY),
                            Component.literal("$" + ComponentHelper.shortenNumber(money, 2)).withStyle(ChatFormatting.GREEN)
                    );

                    components.add(sizeOfLine + 2, moneyRoll);
                }
            }
        }
    }

    private void setArmorRolls(ArmorTagObject armorNbtObject, List<Component> components) {
        Component border = Component.literal(armorNbtObject.getBorderComponent().getString().trim())
                .setStyle(armorNbtObject.getBorderComponent().getStyle())
                .append("   ");

        int tierLine = -1;
        int rightClickLine = -1;
        int qualityLine = -1;
        Component seperatorComponent = Component.empty();

        for (int i = 0; i < components.size(); i++) {
            if(components.get(i).getString().contains("Tier: ")) tierLine = i;
            if(components.get(i).getString().contains("ʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴛᴏ ʀᴏʟʟ ʙᴏɴᴜsᴇs")) rightClickLine = i;
            if(components.get(i).getString().contains("ꞯᴜᴀʟɪᴛʏ: ")
                    || components.get(i).getString().contains("ᴜᴀʟɪᴛʏ ")
            ) qualityLine = i;
            if(Objects.equals(seperatorComponent, Component.empty())
                    && components.get(i).getString().contains("                                               ")
                    && components.get(i).getSiblings().get(1).getStyle().isStrikethrough()
            ) seperatorComponent = components.get(i);
        }

        if(rightClickLine != -1
                && armorNbtObject.isIdentified()
        ) {
            Component inspectComponent = ComponentHelper.concat(
                    Component.literal(ComponentHelper.smallCaps("Hold ")),
                    Component.literal(ComponentHelper.smallCaps(KeyBindHelper.getKeyString(Configs.keyBindConfig.inspectKeybind))),
                    Component.literal(ComponentHelper.smallCaps(" to see more info"))
            ).withStyle(ChatFormatting.DARK_GRAY);
            Component rollHintComponent = ComponentHelper.concat(
                    border,
                    inspectComponent
            );

            components.add(rightClickLine + 1, rollHintComponent);
        }

        if(KeyBindHandler.instance().isPressingInspect()) {
            if(tierLine != -1) {
                for (int i = 4; i >= 0; i--) {
                    if(armorNbtObject.isArmorRollUnlocked(i)
                            && armorNbtObject.isArmorRollRolled(i)
                    ) {
                        int rolls = armorNbtObject.getArmorRollRolls(i);
                        int tier = i + 1;
                        int money = ArmorTagObject.calculateMoneyRolls(rolls, tier);

                        Component moneyRoll = ComponentHelper.concat(
                                border,
                                Component.literal(ComponentHelper.smallCaps("  └ rolls: ")).withStyle(ChatFormatting.GRAY),
                                Component.literal(String.valueOf(rolls - 1)).withStyle(ChatFormatting.YELLOW),
                                Component.literal("x ").withStyle(ChatFormatting.WHITE),
                                Component.literal(ComponentHelper.smallCaps("| spent: ")).withStyle(ChatFormatting.GRAY),
                                Component.literal("$" + ComponentHelper.shortenNumber(money, 2)).withStyle(ChatFormatting.GREEN)
                        );

                        components.add(tierLine + tier + 1, moneyRoll);
                    }
                }
            }

            if(qualityLine != -1
                    && armorNbtObject.isIdentified()
            ) {
                String username = null;

                if(armorNbtObject.getPlayerUUID() != null) {
                    username = ProfileHandler.instance().getUsernameFromId(armorNbtObject.getPlayerUUID());
                }

                Component identifierComponent = ComponentHelper.concat(
                        border,
                        Component.literal("Identifier:").withStyle(ChatFormatting.GRAY)
                );

                Component usernameComponent = ComponentHelper.concat(
                        border,
                        Component.literal(ComponentHelper.smallCaps("  Player: ")).withStyle(ChatFormatting.GRAY),
                        username != null ? Component.literal(username).withStyle(ChatFormatting.YELLOW)
                                : Component.literal("Loading").withStyle(ChatFormatting.DARK_GRAY)
                );

                components.add(qualityLine + 1, usernameComponent);
                components.add(qualityLine + 1, identifierComponent);
                components.add(qualityLine + 1, seperatorComponent);
            }
        }
    }

    private boolean isTackleShopItem(List<Component> components) {
        if(components.size() < TagObject.SHOP_PRICE_LINE + 2) {
            return false;
        }

        int priceLine = Minecraft.getInstance().options.advancedItemTooltips ? TagObject.SHOP_PRICE_LINE + 2 : TagObject.SHOP_PRICE_LINE;

        Component priceComponent = components.get(components.size() - priceLine);
        return priceComponent.getString().contains("Price: $");
    }

    private void setPricesPerItem(TagObject tagObject, List<Component> components) {
        if(components.size() < TagObject.SHOP_PRICE_LINE + 2) {
            return;
        }

        if(tagObject.getCount() > 1) {
            float price = tagObject.getMoney();

            this.setPrice(tagObject, components, price);
        }
    }


    private void setPricesPerItemRaw(TagObject tagObject, List<Component> componentList) {
        if(componentList.size() < TagObject.SHOP_PRICE_LINE + 2) {
            return;
        }
        int priceLine = Minecraft.getInstance().options.advancedItemTooltips ? TagObject.SHOP_PRICE_LINE + 2 : TagObject.SHOP_PRICE_LINE;

        Component priceComponent = componentList.get(componentList.size() - priceLine).copy();
        float price = ComponentHelper.toIntFromString(priceComponent.getString().substring(priceComponent.getString().indexOf("$") + 1));

        this.setPrice(tagObject, componentList, price);
    }

    private void setPrice(TagObject tagObject, List<Component> componentList, float price) {
        int priceLine = Minecraft.getInstance().options.advancedItemTooltips ? TagObject.SHOP_PRICE_LINE + 2 : TagObject.SHOP_PRICE_LINE;

        if(price != 0f) {
            float pricePerItem = price / tagObject.getCount();
            String pricePerItemString = ComponentHelper.shortenNumber(pricePerItem, 2);

            Component pricePerItemComponent = ComponentHelper.concat(
                    Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY),
                    Component.literal("$").withStyle(ChatFormatting.DARK_GREEN),
                    Component.literal(pricePerItemString).withStyle(ChatFormatting.DARK_GREEN),
                    Component.literal(ComponentHelper.smallCaps(" per item")).withStyle(ChatFormatting.GRAY),
                    Component.literal(")").withStyle(ChatFormatting.DARK_GRAY)
            );


            componentList.set(componentList.size() - priceLine, ComponentHelper.concat(
                    componentList.get(componentList.size() - priceLine),
                    pricePerItemComponent
            ));
        }
    }

    private Component getPercentComponent(String percent) {
        return ComponentHelper.concat(
                Component.literal(" ("),
                Component.literal(percent),
                Component.literal("%)")

        );
    }
    //endregion

    //region Dev
    /// Field, Pair<Value, Tooltip>
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
