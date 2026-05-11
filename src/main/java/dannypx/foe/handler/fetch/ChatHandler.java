package dannypx.foe.handler.fetch;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.logic.CodeExecuterHandler;
import dannypx.foe.handler.logic.NotifierHandler;
import dannypx.foe.handler.logic.PlaceholderHandler;
import dannypx.foe.handler.logic.ProfitRewardAttributionHandler;
import dannypx.foe.handler.store.CustomChatTriggerDataHandler;
import dannypx.foe.handler.store.ProfileDataHandler;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.ComponentValue;
import dannypx.foe.type.tuple.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
        ProfitRewardAttributionHandler.instance().onChatMessage(component);
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
            if(!trigger.regex.isBlank()
                    && trigger.pattern.matcher(component.getString()).matches()
            ) {
                storedChatTriggerComponent.put(name, component);
                if(!trigger.notificationToTrigger.isBlank()
                        && trigger.useChatTrigger
                ) {
                    CodeExecuterHandler.runLater(1, () -> {
                        NotifierHandler.instance().notifyChatTrigger(trigger.notificationToTrigger);
                    });
                }
            }
        });
    }

    public Component onModifyMessage(Component component) {
        component = this.modifyPetMessageWithPercentage(component);
        return component;
    }

    private Component modifyPetMessageWithPercentage(Component component) {

        String json = ComponentHelper.componentToJson(component);
        if (json.contains("ᴘᴇᴛ ʀᴀᴛɪɴɢ")) {
            String petStr = json.substring(json.indexOf(" Pet\\n"), json.indexOf("ʀɪɢʜᴛ ᴄʟɪᴄᴋ ᴛᴏ ᴏᴘᴇɴ ᴘᴇᴛ ᴍᴇɴᴜ"));
            Pattern statNumber = Pattern.compile("(?<=\\+)(.*?)(?=\")");
            Matcher statNumberMatcher = statNumber.matcher(petStr);

            if(statNumberMatcher.find()) {
                List<String> matches = statNumberMatcher.results().map(MatchResult::group).toList();

                String petClimateLuck = matches.get(matches.size() - 7);
                String petClimateScale = matches.get(matches.size() - 5);
                String petLocationLuck = matches.get(matches.size() - 3);
                String petLocationScale = matches.getLast();

                float multiplier = findMultiplier(petStr);
                float total = Stream.of(petClimateLuck, petClimateScale, petLocationLuck, petLocationScale).mapToInt(Integer::parseInt).sum();

                StringBuilder builder = new StringBuilder(petStr);
                String petStrNew = petStr;

                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 9), " (" + ComponentHelper.floatToString((Float.parseFloat(petClimateLuck) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 10), " (" + ComponentHelper.floatToString((Float.parseFloat(petClimateScale) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 13), " (" + ComponentHelper.floatToString((Float.parseFloat(petLocationLuck) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 14), " (" + ComponentHelper.floatToString((Float.parseFloat(petLocationScale) * 4 / multiplier), 0) + "%)").toString();
                petStrNew = builder.insert(StringUtils.ordinalIndexOf(petStrNew, "\\n", 16), " (" + ComponentHelper.floatToString((total / multiplier), 0) + "%)").toString();

                return ComponentHelper.jsonToComponent(json.replace(petStr, petStrNew));
            }
        }
        return component;
    }

    private static float findMultiplier(String petStr) {
        if (petStr.indexOf('\uf033') != -1) return 1f;
        else if (petStr.indexOf('\uf034') != -1) return 2f;
        else if (petStr.indexOf('\uf035') != -1) return 3f;
        else if (petStr.indexOf('\uf036') != -1) return 5f;
        else if (petStr.indexOf('\uf037') != -1) return 7.5f;
        return 1;
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
