package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.CustomChatNotificationDataHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.tuple.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;

public class ChatNotifierHandler extends Handler {
    private static ChatNotifierHandler INSTANCE = new ChatNotifierHandler();

    public static ChatNotifierHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatNotifierHandler();
        }
        return INSTANCE;
    }

    //region Fields
    public void notifyChatOnTrigger(String notificationId) {
        if(notificationId != null) {
            String notification = CustomChatNotificationDataHandler.instance().getCustomChatNotificationData().notificationList.getOrDefault(notificationId, "");

            if(!notification.isBlank()) {
                Pair<Boolean, MutableComponent> message = PlaceholderHandler.parsePlaceholderFromString(notification.replace("&", "§"));

                if(message.value1()) {
                    this.sendChatMessage(message.value2());
                }
            }
        }
    }

    public void sendChatMessage(Component message) {
        minecraft.gui.getChat().addMessage(
                ComponentHelper.concat(
                        Component.literal("FoER ").withStyle(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD),
                        Component.literal("» ").withStyle(ChatFormatting.DARK_GRAY),
                        message
                )
        );
    }
    //endregion

    //region Methods
    //endregion

    //region Dev

    /// Field, Pair<Value, Tooltip>
    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "key", Pair.of(Component.literal("value"), Component.empty())
        );
    }
    //endregion
}
