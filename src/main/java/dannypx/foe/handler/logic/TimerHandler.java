package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.ChatHandler;
import dannypx.foe.handler.store.CustomTrackerDataHandler;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.handler.store.CustomTimerDataHandler;
import dannypx.foe.type.tuple.Triplet;
import java.util.*;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TimerHandler extends Handler {
    private static TimerHandler INSTANCE = new TimerHandler();

    public static TimerHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new TimerHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private List<CustomTimerDataHandler.CustomTimer> timers = new ArrayList<>();

    private Map<CustomTimerDataHandler.CustomTimer, Runnable> callbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Runnable> endOfOnCallbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Runnable> endOfOffCallbacks = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Long> lastTrigger = new HashMap<>();
    private Map<CustomTimerDataHandler.CustomTimer, Long> lastPos = new HashMap<>();

    public Pair<Boolean, PlaceholderValue> getTimer(String[] params) {
        if(params.length > 1) {
            Pattern fieldPattern = Pattern.compile("^(timer|offset|notification_to_trigger|clean_up_chat_trigger|use_timer|is_period|off_timer|notification_to_trigger_end|time)$");

            CustomTimerDataHandler.CustomTimer timer = timers.stream().filter(t -> Objects.equals(t.getName(), params[0])).findFirst().orElse(null);

            if(timer != null) {
                if(fieldPattern.matcher(params[1]).matches()) {
                    return switch (params[1]) {
                        case "timer" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timer.getTimer())));
                        case "offset" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timer.getOffset())));
                        case "notification_to_trigger" -> PlaceholderHandler.getPlaceholderValue(new StringValue(timer.getNotificationToTrigger()));
                        case "clean_up_chat_trigger" -> PlaceholderHandler.getPlaceholderValue(new StringValue(timer.getCleanUpChatTrigger()));
                        case "use_timer" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timer.isUseTimer())));
                        case "is_period" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timer.isPeriod())));
                        case "off_timer" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                                yield PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timerPeriod.getOffTimer())));
                            }
                            yield PlaceholderHandler.noResult();
                        }
                        case "notification_to_trigger_end" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                                yield PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(timerPeriod.getNotificationToTriggerEnd())));
                            }
                            yield PlaceholderHandler.noResult();
                        }
                        case "time" -> {
                            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod
                                && params.length >= 3
                            ) {
                                long cycle = timerPeriod.getTimer() + timerPeriod.getOffTimer();
                                long adjusted = System.currentTimeMillis() / 1000 + timerPeriod.getOffset();
                                long pos = adjusted % cycle;

                                long secondsUntilNextOn;
                                long secondsUntilNextOff;
                                Triplet<Long, Long, Long> remainingTimeOn;
                                Triplet<Long, Long, Long> remainingTimeOff;
                                boolean isOn;

                                if(pos < timerPeriod.getTimer()) {
                                    secondsUntilNextOn = timerPeriod.getTimer() - pos;
                                    secondsUntilNextOff = cycle - pos;
                                    remainingTimeOn = getTime(secondsUntilNextOn);
                                    remainingTimeOff = getTime(secondsUntilNextOff);
                                    isOn = true;
                                } else {
                                    secondsUntilNextOn = (cycle - pos) + timerPeriod.getTimer();
                                    secondsUntilNextOff = cycle - pos;
                                    remainingTimeOn = getTime(secondsUntilNextOn);
                                    remainingTimeOff = getTime(secondsUntilNextOff);
                                    isOn = false;
                                }

                                yield switch (params[2]) {
                                    case "on" -> switch (params[3]) {
                                        case "second" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOn.value1())));
                                        case "minute" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOn.value2())));
                                        case "hour" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(remainingTimeOn.value3())));
                                        default -> PlaceholderHandler.noResult();
                                    };
                                    case "off" -> switch (params[3]) {
                                        case "second" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOff.value1())));
                                        case "minute" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTimeOff.value2())));
                                        case "hour" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(remainingTimeOff.value3())));
                                        default -> PlaceholderHandler.noResult();
                                    };
                                    case "is_on" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(isOn)));
                                    case "is_off" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(!isOn)));
                                    default -> PlaceholderHandler.noResult();
                                };
                            } else if (params.length == 3) {
                                long timeSeconds = System.currentTimeMillis() / 1000;
                                long adjusted = timeSeconds + timer.getOffset();
                                long pos = adjusted % timer.getTimer();
                                long remaining = timer.getTimer() - pos;

                                Triplet<Long, Long, Long> remainingTime = getTime(remaining);

                                yield switch (params[2]) {
                                    case "second" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTime.value1())));
                                    case "minute" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.format(Locale.US, "%02d", remainingTime.value2())));
                                    case "hour" -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(remainingTime.value3())));
                                    default -> PlaceholderHandler.noResult();
                                };
                            } else {
                                yield PlaceholderHandler.noResult();
                            }
                        }
                        default -> PlaceholderHandler.noResult();
                    };
                }
            }
        }
        return PlaceholderHandler.noResult();
    }

    private Triplet<Long, Long, Long> getTime(long seconds) {
        long hour = seconds / 3600;
        long minute = (seconds % 3600) / 60;
        long second = seconds % 60;

        return Triplet.of(second, minute, hour);
    }
    //endregion

    //region Methods
    public void tick() {
        long timeMillis = System.currentTimeMillis();

        timers.forEach(timer -> {
            long adjustedWithOffset = timeMillis + timer.getOffset() * 1000L;

            if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                long cycle = timerPeriod.getTimer() * 1000L + timerPeriod.getOffTimer() * 1000L;
                if(cycle > 0) {
                    long pos = adjustedWithOffset % cycle;
                    long prevPos = lastPos.getOrDefault(timerPeriod, pos);

                    if (crossed(prevPos, pos, 0)) {
                        this.triggerTimer(timerPeriod, timeMillis, endOfOffCallbacks.get(timerPeriod));
                    }

                    if (crossed(prevPos, pos, timerPeriod.getTimer() * 1000L)) {
                        this.triggerTimer(timerPeriod, timeMillis, endOfOnCallbacks.get(timerPeriod));

                        // Clean Chat Trigger
                        String[] chatTriggers = timer.getCleanUpChatTrigger().replaceAll("\\s", "").split("\\s*,\\s*");
                        ChatHandler.instance().cleanChatTriggerStore(chatTriggers);
                    }

                    lastPos.put(timerPeriod, pos);
                }
            } else {
                if(timer.getTimer() > 0) {
                    long interval = timer.getTimer() * 1000L;
                    long pos = adjustedWithOffset % interval;
                    long prevPos = lastPos.getOrDefault(timer, pos);

                    if (crossed(prevPos, pos, 0)) {
                        this.triggerTimer(timer, timeMillis, callbacks.get(timer));

                        // Clean Chat Trigger
                        String[] chatTriggers = timer.getCleanUpChatTrigger().replaceAll("\\s", "").split("\\s*,\\s*");
                        ChatHandler.instance().cleanChatTriggerStore(chatTriggers);
                    }

                    lastPos.put(timer, pos);
                }
            }
        });
    }

    public void init() {
        this.initTimers();
    }

    public void initTimers() {
        timers.clear();
        callbacks.clear();
        endOfOnCallbacks.clear();
        endOfOffCallbacks.clear();
        lastTrigger.clear();
        lastPos.clear();

        List<CustomTimerDataHandler.CustomTimer> tempTimers = new ArrayList<>();
        CustomTimerDataHandler.instance().getCustomTimerData().timerList.forEach((name, timer) -> {
            if(timer.isUseTimer()) {
                if(timer instanceof CustomTimerDataHandler.CustomTimerPeriod timerPeriod) {
                    tempTimers.add(timerPeriod);

                    this.register(timerPeriod, () -> {
                        CodeExecuterHandler.runLater(1, () -> {
                            NotifierHandler.instance().notifyOnTrigger(timerPeriod.getNotificationToTrigger());
                            ChatNotifierHandler.instance().notifyChatOnTrigger(timerPeriod.getChatNotificationToTrigger());
                            CustomTrackerDataHandler.instance().updateTracker(timerPeriod.getTrackerToTrigger());
                        });
                    }, () -> {
                        CodeExecuterHandler.runLater(1, () -> {
                            NotifierHandler.instance().notifyOnTrigger(timerPeriod.getNotificationToTriggerEnd());
                            ChatNotifierHandler.instance().notifyChatOnTrigger(timerPeriod.getChatNotificationToTriggerEnd());
                            CustomTrackerDataHandler.instance().updateTracker(timerPeriod.getTrackerToTriggerEnd());
                        });
                    });
                } else {
                    tempTimers.add(timer);

                    this.register(timer, () -> {
                        CodeExecuterHandler.runLater(1, () -> {
                            NotifierHandler.instance().notifyOnTrigger(timer.getNotificationToTrigger());
                            ChatNotifierHandler.instance().notifyChatOnTrigger(timer.getChatNotificationToTrigger());
                            CustomTrackerDataHandler.instance().updateTracker(timer.getTrackerToTrigger());
                        });
                    });
                }
            }
        });

        timers = new ArrayList<>(tempTimers);
    }

    public void register(CustomTimerDataHandler.CustomTimer timer, Runnable callback) {
        callbacks.put(timer, callback);
    }

    public void register(CustomTimerDataHandler.CustomTimerPeriod timer, Runnable onCallback, Runnable offCallback) {
        endOfOnCallbacks.put(timer, offCallback);
        endOfOffCallbacks.put(timer, onCallback);
    }

    private void triggerTimer(CustomTimerDataHandler.CustomTimer timer, long time, Runnable callback) {
        if(callback != null) {
            long last = lastTrigger.getOrDefault(timer, -1L);

            if(last != time) {
                lastTrigger.put(timer, time);
                callback.run();
            }
        }
    }

    private boolean crossed(long prev, long curr, long target) {
        if (curr == prev) return false;
        if(prev <= curr) {
            return prev < target && curr >= target;
        } else {
            return prev < target || curr >= target;
        }
    }
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
