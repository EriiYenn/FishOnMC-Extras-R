package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.ChatNotifierHandler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.handler.store.ConstantDataHandler;
import dannypx.foe.handler.store.CustomChatTriggerDataHandler;
import dannypx.foe.handler.store.CustomTrackerDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.ComponentValue;
import dannypx.foe.type.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatHandler extends Handler {
    private static ChatHandler INSTANCE = new ChatHandler();

    public static ChatHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ChatHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private Map<String, Component> storedChatTriggerComponent = new HashMap<>();

    final List<String> blacklistedMessageFilters = List.of(
            "REACTIONS »"
    );

    public Pair<Boolean, PlaceholderValue> getChat(String[] params) {
        if(params.length > 1
                && minecraft.player != null
        ) {
            Pattern fieldPattern = Pattern.compile("^(trigger)$");

            if(fieldPattern.matcher(params[0]).matches()
            ) {
                return switch(params[0]) {
                    case "trigger" -> PlaceholderHandler.getPlaceholderValue(new ComponentValue(storedChatTriggerComponent.getOrDefault(params[1], Component.empty())));
                    default -> PlaceholderHandler.noResult();
                };
            }
        }
        return PlaceholderHandler.noResult();
    }
    //endregion

    //region Methods
    public void init() {
        if(storedChatTriggerComponent.isEmpty()) {
            this.initChatTrigger();
        }
    }

    public void initChatTrigger() {
        storedChatTriggerComponent.clear();
        CustomChatTriggerDataHandler.instance().getCustomChatTriggerData().chatTriggerList.forEach((name, trigger) -> {
            storedChatTriggerComponent.put(name, Component.empty());
        });
    }

    public void onReceiveMessage(Component component) {
        if(this.inBlackList(component)) return;
        
        this.checkPet(component);
        this.checkChatTrigger(component);
    }

    private boolean inBlackList(Component component) {
        return blacklistedMessageFilters.stream().anyMatch(filter -> component.getString().startsWith(filter));
    }

    private void checkPet(Component component) {
        if(component.getString().startsWith("PETS » Equipped your")) {
            ProfileDataHandler.instance().updatePet(true);
        } else if (component.getString().startsWith("PETS » Pet unequipped!")) {
            ProfileDataHandler.instance().updatePet(false);
        } else if(component.getString().startsWith("CREWS » Crew Chat has been enabled")) {
            ProfileDataHandler.instance().updateCrewChat(true);
        } else if(component.getString().startsWith("CREWS » Crew Chat has been disabled")) {
            ProfileDataHandler.instance().updateCrewChat(false);
        } else if(component.getString().startsWith("TOURNAMENT You have ENABLED tournament contributions")) {
            ProfileDataHandler.instance().updateTournamentContribution(true);
        } else if(component.getString().startsWith("TOURNAMENT You have DISABLED tournament contributions")) {
            ProfileDataHandler.instance().updateTournamentContribution(false);
        }
    }

    private void checkChatTrigger(Component component) {
        CustomChatTriggerDataHandler.instance().getCustomChatTriggerData().chatTriggerList.forEach((name, trigger) -> {
            if(!trigger.getRegex().isBlank()
                    && trigger.getPattern().matcher(component.getString()).matches()
            ) {
                storedChatTriggerComponent.put(name, component);
                if(trigger.getNotificationToTrigger() != null
                        && !trigger.getNotificationToTrigger().isBlank()
                        && trigger.isUseChatTrigger()
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        NotifierHandler.instance().notifyOnTrigger(trigger.getNotificationToTrigger());
                    });
                }

                if(trigger.getChatNotificationToTrigger() != null
                        && !trigger.getChatNotificationToTrigger().isBlank()
                        && trigger.isUseChatTrigger()
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        ChatNotifierHandler.instance().notifyChatOnTrigger(trigger.getChatNotificationToTrigger());
                    });
                }

                if(trigger.getTrackerToTrigger() != null
                        && !trigger.getTrackerToTrigger().isBlank()
                        && trigger.isUseChatTrigger()
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        CustomTrackerDataHandler.instance().updateTracker(trigger.getTrackerToTrigger());
                    });
                }
            }
        });
    }

    public String onModifyChatMessage(String text) {
        AtomicReference<String> modified = new AtomicReference<>(text);
        ConstantDataHandler.instance().getConstantData().fishData.forEach((category, fieldMap) -> {
            fieldMap.forEach((stringField, textField) -> {
                if(modified.get().contains(textField.getString().trim())) {
                    modified.set(modified.get().replace(textField.getString().trim(), ComponentHelper.capitalize(stringField)));
                }
            });
        });

        modified.set(modified.get().replace("FoER » ", ""));

        return modified.get();
    }

    public Component onModifyGameMessage(Component component) {
        return component;
    }

    public void cleanChatTriggerStore(String[] chatTriggers) {
        for (String chatTrigger : chatTriggers) {
            if(storedChatTriggerComponent.containsKey(chatTrigger)) {
                CodeExecuterHandler.runLater(2, () -> {
                    storedChatTriggerComponent.put(chatTrigger, Component.empty());
                });
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
