package dannypx.foe.handler.logic;

import dannypx.foe.handler.Handler;
import dannypx.foe.handler.fetch.*;
import dannypx.foe.handler.store.*;
import dannypx.foe.helper.FunctionParser;
import dannypx.foe.helper.MathHelper;
import dannypx.foe.helper.ComponentHelper;
import dannypx.foe.item.TagObject;
import dannypx.foe.item.ValidateItem;
import dannypx.foe.type.search.Operator;
import dannypx.foe.type.tuple.Pair;
import dannypx.foe.type.placeholder.PlaceholderValue;
import dannypx.foe.type.placeholder.StringValue;
import dannypx.foe.type.placeholder.ComponentValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

public class PlaceholderHandler extends Handler {
    private static PlaceholderHandler INSTANCE = new PlaceholderHandler();

    public static PlaceholderHandler instance() {
        if (INSTANCE == null) {
            INSTANCE = new PlaceholderHandler();
        }
        return INSTANCE;
    }

    //region Fields
    private static final Map<String, Function<String[], Pair<Boolean, PlaceholderValue>>> placeholders = Map.ofEntries(
            Map.entry("boss_bar", BossEventHandler.instance()::getBossBar),
            Map.entry("player", LocalPlayerHandler.instance()::getClientPlayer),
            Map.entry("network", NetworkHandler.instance()::getNetwork),
            Map.entry("scoreboard", ScoreboardHandler.instance()::getScoreboard),
            Map.entry("tab", TabOverlayHandler.instance()::getTab),
            Map.entry("title", TitleHandler.instance()::getTitle),
            Map.entry("connection", ConnectionHandler.instance()::getConnection),
            Map.entry("inventory", InventoryHandler.instance()::getInventory),
            Map.entry("key_bind", KeyBindHandler.instance()::getKeyBind),
            Map.entry("loading", LoadingHandler.instance()::getLoading),
            Map.entry("ray_cast", HitResultHandler.instance()::getRayCast),
            Map.entry("crew", CrewHandler.instance()::getCrew),
            Map.entry("chat", ChatHandler.instance()::getChat),
            Map.entry("timer", TimerHandler.instance()::getTimer),
            Map.entry("catch", CatchingHandler.instance()::getCatch),
            Map.entry("tracker_data", CustomTrackerDataHandler.instance()::getCustomTrackerData),
            Map.entry("constant_data", ConstantDataHandler.instance()::getConstantData),
            Map.entry("profile_data", ProfileDataHandler.instance()::getProfileData),
            Map.entry("quest_data", QuestDataHandler.instance()::getQuestData),
            Map.entry("stats_data", StatsDataHandler.instance()::getStatsData),
            Map.entry("crew_data", CrewDataHandler.instance()::getCrewData)
    );

    private static final Map<String, Function<FunctionParser.FunctionPlaceholder, Pair<Boolean, PlaceholderValue>>> functionPlaceholders = Map.ofEntries(
            // Boolean
            Map.entry("condition", PlaceholderHandler::parseConditionFromString),
            Map.entry("is_blank", param -> parseIsBlankFromString(param, true)),
            Map.entry("is_not_blank", param -> parseIsBlankFromString(param, false)),
            Map.entry("or", PlaceholderHandler::parseOrFromString),
            Map.entry("and", PlaceholderHandler::parseAndFromString),
            Map.entry("not", PlaceholderHandler::parseNotFromString),
            Map.entry("xot", PlaceholderHandler::parseXorFromString),
            // String
            Map.entry("substring_front", param -> parseSubStringFromString(param, true)),
            Map.entry("substring_back", param -> parseSubStringFromString(param, false)),
            Map.entry("index_of", PlaceholderHandler::parseIndexOfFromString),
            // Math
            Map.entry("expression", PlaceholderHandler::parseExpressionFromString),
            Map.entry("max", PlaceholderHandler::parseMaxFromString),
            Map.entry("min", PlaceholderHandler::parseMinFromString),
            Map.entry("abs", PlaceholderHandler::parseAbsoluteFromString),
            Map.entry("ceil", PlaceholderHandler::parseCeilingFromString),
            Map.entry("floor", PlaceholderHandler::parseFloorFromString),
            Map.entry("round", PlaceholderHandler::parseRoundingFromString)
    );
    //endregion

    //region Methods
    // Boolean = hasFullData
    public static Pair<Boolean, MutableComponent> parsePlaceholderFromString(String input) {
        boolean hasFullData = true;

        MutableComponent result = Component.empty();
        int lastEnd = 0;
        Style activeStyle = Style.EMPTY;

        while (hasFullData) {
            int startPlaceholderPos = -1;
            int endPlaceHolderPos = -1;

            for (int i = lastEnd; i < input.length(); i++) {
                if (input.charAt(i) == '%' && (i == 0 || input.charAt(i - 1) != '\\')) {
                    if (startPlaceholderPos == -1) {
                        startPlaceholderPos = i;
                    } else {
                        endPlaceHolderPos = i;
                        break;
                    }
                }
            }

            if (startPlaceholderPos == -1 || endPlaceHolderPos == -1) {
                break;
            }

            if (startPlaceholderPos > lastEnd) {
                String before = input.substring(lastEnd, startPlaceholderPos);
                Pair<MutableComponent, Style> parsed =
                        ComponentHelper.parseLegacyWithStyle(before, activeStyle);

                result.append(parsed.value1());
                activeStyle = parsed.value2();
            }

            String full = input.substring(startPlaceholderPos + 1, endPlaceHolderPos);

            String[] parts = full.split("\\.");
            String identifier = parts[0];
            String[] parameters = Arrays.copyOfRange(parts, 1, parts.length);

            Pair<Boolean, PlaceholderValue> functionResult = null;

            if(placeholders.containsKey(identifier)) {
                Function<String[], Pair<Boolean, PlaceholderValue>> function = placeholders.get(identifier);

                if (function != null) {
                    functionResult = function.apply(parameters);
                } else {
                    result.append(Component.literal(input.substring(startPlaceholderPos, endPlaceHolderPos + 1)).setStyle(activeStyle));
                }
            } else if (functionPlaceholders.containsKey(identifier)) {
                functionResult = parseFunctionPlaceHolderFromString("%" + full + "%");
            }

            if (functionResult != null && functionResult.value1()) {
                Pair<MutableComponent, Style> parsed;

                switch (functionResult.value2()) {
                    case StringValue stringValue -> parsed = ComponentHelper.parseLegacyWithStyle(stringValue.value(), activeStyle);
                    case ComponentValue componentValue -> parsed = Pair.of(componentValue.value().copy(), componentValue.value().getStyle());
                }

                result.append(parsed.value1());
                activeStyle = parsed.value2();
            } else {
                result.append(Component.literal(input.substring(startPlaceholderPos, endPlaceHolderPos + 1)).setStyle(activeStyle));
                hasFullData = false;
            }

            lastEnd = endPlaceHolderPos + 1;
        }

        if (lastEnd < input.length()) {
            String remaining = input.substring(lastEnd);
            Pair<MutableComponent, Style> parsed = ComponentHelper.parseLegacyWithStyle(remaining, activeStyle);
            result.append(parsed.value1());
        }

        return Pair.of(hasFullData, result);
    }

    private static Pair<Boolean, PlaceholderValue> parseFunctionPlaceHolderFromString(String placeholder) {
        FunctionParser.FunctionPlaceholder functionPlaceholder = FunctionParser.parse(placeholder);

        Function<FunctionParser.FunctionPlaceholder, Pair<Boolean, PlaceholderValue>> function = functionPlaceholders.get(functionPlaceholder.function);

        if(function != null) {
            return function.apply(functionPlaceholder);
        } else {
            return noResult();
        }
    }

    private static Pair<Boolean, PlaceholderValue> parseConditionFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator != null && placeholder.left != null && placeholder.right != null) {
            String leftField;
            String rightField;

            if(placeholder.leftBracketed)
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%").value2().getString();
            else {
                leftField = placeholder.left;
            }

            if(placeholder.rightBracketed)
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%").value2().getString();
            else {
                rightField = placeholder.right;
            }

            try {
                float leftFloat = Float.parseFloat(leftField);
                float rightFloat = Float.parseFloat(rightField);

                return Pair.of(MathHelper.checkOperation(placeholder.operator, leftFloat, rightFloat), new StringValue(""));
            } catch (NumberFormatException e) {
                return switch (placeholder.operator) {
                    case SHORT_EQUAL -> Pair.of(leftField.contains(rightField), new StringValue(""));
                    case EQUAL -> Pair.of(leftField.equals(rightField), new StringValue(""));
                    case NOT_EQUAL -> Pair.of(!leftField.equals(rightField), new StringValue(""));
                    default -> noResult();
                };
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseIsBlankFromString(FunctionParser.FunctionPlaceholder placeholder, boolean isBlank) {
        if(placeholder.operator == null && placeholder.left != null && placeholder.right == null) {
            String leftField;

            if(placeholder.leftBracketed) {
                Pair<Boolean, MutableComponent> parsedString = parsePlaceholderFromString("%" + placeholder.left + "%");
                if(parsedString.value1()) {
                    leftField = parsePlaceholderFromString("%" + placeholder.left + "%").value2().getString();
                } else {
                    leftField = "";
                }
            } else {
                leftField = placeholder.left;
            }

            return Pair.of(leftField.isBlank() == isBlank, new StringValue(""));
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseSubStringFromString(FunctionParser.FunctionPlaceholder placeholder, boolean isFront) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            int rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            try {
                if(leftField.value1()) {
                    if(placeholder.rightBracketed) {
                        rightField = (int) Float.parseFloat(parsePlaceholderFromString("%" + placeholder.right + "%").value2().getString());
                    } else {
                        rightField = (int) Float.parseFloat(placeholder.right);
                    }

                    if(isFront) {
                        return Pair.of(true, new ComponentValue(ComponentHelper.substring(leftField.value2(), 0, rightField)));
                    } else {
                        return Pair.of(true, new ComponentValue(ComponentHelper.substring(leftField.value2(), rightField, leftField.value2().getString().length())));
                    }
                } else {
                    return noResult();
                }
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseIndexOfFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(true, Component.literal(placeholder.right));
            }

            if(leftField.value1() && rightField.value1()) {
                int index = leftField.value2().getString().indexOf(rightField.value2().getString());

                if(index == -1) {
                    return noResult();
                } else {
                    return Pair.of(true, new StringValue(String.valueOf(index)));
                }
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseOrFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(Boolean.parseBoolean(placeholder.left), Component.empty());
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(Boolean.parseBoolean(placeholder.right), Component.empty());
            }

            if(leftField.value1() || rightField.value1()) {
                return Pair.of(true, new StringValue(""));
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseXorFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(Boolean.parseBoolean(placeholder.left), Component.empty());
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(Boolean.parseBoolean(placeholder.right), Component.empty());
            }

            if((leftField.value1() || rightField.value1()) && (leftField.value1() != rightField.value1())) {
                return Pair.of(true, new StringValue(""));
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseAndFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(Boolean.parseBoolean(placeholder.left), Component.empty());
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(Boolean.parseBoolean(placeholder.right), Component.empty());
            }

            if(leftField.value1() && rightField.value1()) {
                return Pair.of(true, new StringValue(""));
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseNotFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == null && placeholder.left != null && placeholder.right == null) {
            Pair<Boolean, MutableComponent> leftField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(Boolean.parseBoolean(placeholder.left), Component.empty());
            }

            if(leftField.value1()) {
                return Pair.of(false, new StringValue(""));
            } else {
                return Pair.of(true, new StringValue(""));
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseExpressionFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator != null && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(true, Component.literal(placeholder.right));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());
                float rightNumber = Float.parseFloat(rightField.value2().getString());

                float result = MathHelper.checkExpression(placeholder.operator, leftNumber, rightNumber);

                if(result != Float.MIN_VALUE) {
                    return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
                }
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseMaxFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(true, Component.literal(placeholder.right));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());
                float rightNumber = Float.parseFloat(rightField.value2().getString());

                float result = Math.max(leftNumber, rightNumber);

                return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseMinFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(true, Component.literal(placeholder.right));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());
                float rightNumber = Float.parseFloat(rightField.value2().getString());

                float result = Math.min(leftNumber, rightNumber);

                return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseAbsoluteFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == null && placeholder.left != null && placeholder.right == null) {
            Pair<Boolean, MutableComponent> leftField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());

                float result = Math.abs(leftNumber);

                return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseCeilingFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == null && placeholder.left != null && placeholder.right == null) {
            Pair<Boolean, MutableComponent> leftField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());

                float result = (float) Math.ceil(leftNumber);

                return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseRoundingFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == Operator.SEPARATOR && placeholder.left != null && placeholder.right != null) {
            Pair<Boolean, MutableComponent> leftField;
            Pair<Boolean, MutableComponent> rightField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            if(placeholder.rightBracketed) {
                rightField = parsePlaceholderFromString("%" + placeholder.right + "%");
            } else {
                rightField = Pair.of(true, Component.literal(placeholder.right));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());
                int rightNumber = Integer.parseInt(rightField.value2().getString());

                if (rightNumber < 0) return noResult();

                BigDecimal bd = new BigDecimal(Double.toString(leftNumber));
                bd = bd.setScale(rightNumber, RoundingMode.HALF_UP);
                float result = (float) bd.doubleValue();

                return Pair.of(true, new StringValue(ComponentHelper.floatToString(result, rightNumber)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> parseFloorFromString(FunctionParser.FunctionPlaceholder placeholder) {
        if(placeholder.operator == null && placeholder.left != null && placeholder.right == null) {
            Pair<Boolean, MutableComponent> leftField;

            if(placeholder.leftBracketed) {
                leftField = parsePlaceholderFromString("%" + placeholder.left + "%");
            } else {
                leftField = Pair.of(true, Component.literal(placeholder.left));
            }

            try {
                float leftNumber = Float.parseFloat(leftField.value2().getString());

                float result = (float) Math.floor(leftNumber);

                return Pair.of(true, new StringValue(String.format(Locale.US, "%f", result)));
            } catch (NumberFormatException e) {
                return noResult();
            }
        }
        return noResult();
    }

    public static Pair<Boolean, PlaceholderValue> getPlaceholderValue(PlaceholderValue placeholderValue) {
        return getPlaceholderValue(placeholderValue, false);
    }

    public static Pair<Boolean, PlaceholderValue> getPlaceholderValue(PlaceholderValue placeholderValue, Boolean noHide) {
        switch (placeholderValue) {
            case StringValue stringValue -> {
                if(!stringValue.value().isBlank()) return Pair.of(stringValue);
                return noHide ? Pair.of(stringValue) : Pair.ofFalse(stringValue);
            }
            case ComponentValue componentValue -> {
                if(!componentValue.value().getString().isBlank()) return Pair.of(componentValue);
                return noHide ? Pair.of(componentValue) : Pair.ofFalse(componentValue);
            }
        }
    }

    public static Pair<Boolean, PlaceholderValue> noResult() {
        return Pair.ofFalse(new StringValue(""));
    }

    public static Pair<Boolean, PlaceholderValue> getNbtValue(TagObject object, String field) {
        if(object.contains(field)) {
            Tag data = object.get(field);
            return switch (data.getId()) {
                case 1 -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(object.getBoolean(field))));
                case 3 -> PlaceholderHandler.getPlaceholderValue(new StringValue(String.valueOf(object.getInt(field))));
                case 5 -> PlaceholderHandler.getPlaceholderValue(new StringValue(ComponentHelper.floatToString(object.getFloat(field), 2)));
                case 8 -> PlaceholderHandler.getPlaceholderValue(new StringValue(object.getString(field)));
                default -> PlaceholderHandler.noResult();
            };
        }
        return PlaceholderHandler.noResult();
    }

    public static Pair<Boolean, PlaceholderValue> getNbtValue(ItemStack itemStack, String field) {
        Pair<Boolean, TagObject> item = ValidateItem.isServerItem(itemStack);
        return getNbtValue(item.value2(), field);
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
