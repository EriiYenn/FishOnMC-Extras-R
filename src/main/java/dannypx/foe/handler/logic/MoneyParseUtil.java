package dannypx.foe.handler.logic;

import dannypx.foe.helper.ComponentHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MoneyParseUtil {
    private static final Pattern DOLLAR_MONEY = Pattern.compile("\\$\\s*([0-9][0-9,]*(?:\\.[0-9]+)?\\s*[KkMm]|[0-9][0-9,]*(?:\\.[0-9]+)?)");

    private MoneyParseUtil() {}

    public static float parseFirstAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0f;
        }

        Matcher matcher = DOLLAR_MONEY.matcher(value);
        while (matcher.find()) {
            float parsed = parseAmountToken(matcher.group(1));
            if (parsed > 0f) {
                return parsed;
            }
        }
        return 0f;
    }

    public static float parseLargestAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0f;
        }

        float best = 0f;
        Matcher matcher = DOLLAR_MONEY.matcher(value);
        while (matcher.find()) {
            float parsed = parseAmountToken(matcher.group(1));
            if (parsed > best) {
                best = parsed;
            }
        }
        return best;
    }

    private static float parseAmountToken(String raw) {
        if (raw == null) {
            return 0f;
        }

        String cleaned = raw.replace(",", "").trim();
        if (cleaned.isEmpty()) {
            return 0f;
        }

        try {
            if (cleaned.matches(".*[KkMm].*")) {
                return ComponentHelper.toIntFromString(cleaned.toUpperCase());
            }
            return Float.parseFloat(cleaned);
        } catch (NumberFormatException ignored) {
            return 0f;
        }
    }
}
