package com.loohp.multichatdiscordsrvaddon.utils;

import com.loohp.multichatdiscordsrvaddon.objectholders.ICPlaceholder;
import com.loohp.multichatdiscordsrvaddon.objectholders.ValuePairs;

import java.util.List;
import java.util.regex.Matcher;

public class PatternUtils {

    public static ValuePairs<Boolean, Matcher> matches(String message, List<ICPlaceholder> placeholders) {
        for (ICPlaceholder placeholder : placeholders) {
            Matcher matcher = placeholder.getKeyword().matcher(message);
            if (matcher.matches()) return new ValuePairs<>(true, matcher);
        }

        return new ValuePairs<>(false, null);
    }
}
