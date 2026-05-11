package dannypx.foe.handler.logic;

import dannypx.foe.config.Configs;
import dannypx.foe.handler.Handler;
import dannypx.foe.handler.store.ProfitRewardTriggerDataHandler;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.tuple.Pair;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ProfitRewardAttributionHandler extends Handler {
    private static ProfitRewardAttributionHandler INSTANCE = new ProfitRewardAttributionHandler();

    public static ProfitRewardAttributionHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new ProfitRewardAttributionHandler();
        }
        return INSTANCE;
    }

    private RewardContext activeContext = RewardContext.none();

    @Override
    public void init() {
        this.reset();
    }

    @Override
    public void tick() {
        if (!activeContext.isActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now > activeContext.activeUntilMs()) {
            this.reset();
            return;
        }

        InventoryHandler.instance().getSnapshottedItems().stream()
                .filter(item -> !item.isProfitCounted())
                .filter(item -> item.getTime() >= activeContext.openedAtMs())
                .filter(item -> item.getTime() <= activeContext.activeUntilMs())
                .toList()
                .forEach(item -> this.processInventoryGain(item, activeContext.kind()));
    }

    public void reset() {
        this.activeContext = RewardContext.none();
    }

    public void openQuestContext() {
        if (Configs.handlerConfig.trackQuestProfit.get()) {
            this.mergeOrOpenForKind(RewardContextKind.QUEST);
        }
    }

    public void openCompetitionContext() {
        if (Configs.handlerConfig.trackCompetitionProfit.get()) {
            this.mergeOrOpenForKind(RewardContextKind.COMPETITION);
        }
    }

    public void onChatMessage(Component component) {
        String message = component.getString();
        if (message.isBlank()) {
            return;
        }

        if (Configs.handlerConfig.trackCompetitionProfit.get()) {
            this.checkCompetitionOpenPatterns(message);
            this.checkMoneyPatterns(message,
                    ProfitRewardTriggerDataHandler.instance().getProfitRewardTriggerData().competitionMoneyPatterns,
                    RewardContextKind.COMPETITION);
        }

        if (Configs.handlerConfig.trackQuestProfit.get()) {
            this.checkQuestOpenPatterns(message);
            this.checkMoneyPatterns(message,
                    ProfitRewardTriggerDataHandler.instance().getProfitRewardTriggerData().questMoneyPatterns,
                    RewardContextKind.QUEST);
        }
    }

    private void processInventoryGain(InventoryHandler.InventoryGainEvent itemEvent, RewardContextKind kind) {
        Pair<Boolean, TagObject> validatedItem = ValidateItem.isType(itemEvent.getItemStack());
        if (!validatedItem.value1()) {
            return;
        }

        TagObject tag = validatedItem.value2();
        if (ValidateItem.isFish(tag.getItemStack()).value1()) {
            return;
        }

        if (ItemValueResolver.unitValueFor(tag) <= 0f) {
            return;
        }

        ProfitTrackerHandler.instance().recordItemReward(tag, itemEvent.getCount(), sourceForItem(kind, tag));
        itemEvent.markProfitCounted();
        LoggerHandler._debug("Profit tracker attributed " + itemEvent.getCount() + "x " + tag.getName().getString() + " to " + kind.categoryLabel, tag.getItemStack());
    }

    private void checkCompetitionOpenPatterns(String message) {
        for (Pattern pattern : ProfitRewardTriggerDataHandler.instance().getProfitRewardTriggerData().competitionOpenPatterns) {
            if (pattern != null && pattern.matcher(message).find()) {
                this.openCompetitionContext();
                return;
            }
        }
    }

    private void checkQuestOpenPatterns(String message) {
        for (Pattern pattern : ProfitRewardTriggerDataHandler.instance().getProfitRewardTriggerData().questOpenPatterns) {
            if (pattern != null && pattern.matcher(message).find()) {
                this.openQuestContext();
                return;
            }
        }
    }

    private void checkMoneyPatterns(String message, List<Pattern> patterns, RewardContextKind kind) {
        for (Pattern pattern : patterns) {
            if (pattern == null) {
                continue;
            }

            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                float amount = extractAmount(matcher);
                if (amount <= 0f) {
                    continue;
                }

                if (!hasActiveContextFor(kind)) {
                    return;
                }

                this.mergeOrOpenForKind(kind);
                ProfitTrackerHandler.instance().recordMoneyReward(amount, kind.moneySource());
                LoggerHandler._debug("Profit tracker attributed $" + amount + " to " + kind.categoryLabel);
                return;
            }
        }
    }

    private boolean hasActiveContextFor(RewardContextKind kind) {
        return activeContext.isActive() && activeContext.kind() == kind;
    }

    private static float extractAmount(Matcher matcher) {
        for (int i = 1; i <= matcher.groupCount(); i++) {
            float fromGroup = MoneyParseUtil.parseFirstAmount(matcher.group(i));
            if (fromGroup > 0f) {
                return fromGroup;
            }
        }
        return MoneyParseUtil.parseFirstAmount(matcher.group());
    }

    private void openContext(RewardContextKind kind) {
        long now = System.currentTimeMillis();
        activeContext = new RewardContext(kind, now, now + Configs.handlerConfig.rewardProfitWindowMs.get());
    }

    /**
     * If the same source is already open, extend the end time but keep the original {@code openedAtMs} so
     * multi-line quest rewards (QUEST Complete, then + $..., then item grants) do not miss inventory gains
     * that happened before a later line resets the window.
     */
    private void mergeOrOpenForKind(RewardContextKind kind) {
        long now = System.currentTimeMillis();
        long window = Configs.handlerConfig.rewardProfitWindowMs.get();
        if (activeContext.isActive() && activeContext.kind() == kind) {
            long opened = activeContext.openedAtMs();
            activeContext = new RewardContext(kind, opened, now + window);
        } else {
            this.openContext(kind);
        }
    }

    private static ProfitSourceClassifier.ProfitSource sourceForItem(RewardContextKind kind, TagObject tag) {
        ProfitSourceClassifier.ProfitSource classified = ProfitSourceClassifier.classify(tag);
        return new ProfitSourceClassifier.ProfitSource(kind.categoryId, kind.categoryLabel, classified.subId, classified.subLabel);
    }

    @Override
    protected Map<String, Pair<MutableComponent, MutableComponent>> _getFields() {
        return Map.of(
                "activeRewardContext", Pair.of(Component.literal(activeContext.kind().categoryLabel), Component.empty())
        );
    }

    private record RewardContext(RewardContextKind kind, long openedAtMs, long activeUntilMs) {
        private static RewardContext none() {
            return new RewardContext(RewardContextKind.NONE, 0L, 0L);
        }

        private boolean isActive() {
            return kind != RewardContextKind.NONE;
        }
    }

    private enum RewardContextKind {
        NONE("", ""),
        QUEST("quests", "Quests"),
        COMPETITION("competitions", "Competitions");

        private final String categoryId;
        private final String categoryLabel;

        RewardContextKind(String categoryId, String categoryLabel) {
            this.categoryId = categoryId;
            this.categoryLabel = categoryLabel;
        }

        private ProfitSourceClassifier.ProfitSource moneySource() {
            return new ProfitSourceClassifier.ProfitSource(categoryId, categoryLabel, "money", "Money");
        }
    }
}
