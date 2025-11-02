package com.example.hoccode.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveWordUtil {
    private final Pattern pattern;
    public SensitiveWordUtil(List<String> sensitiveWords) {
        String regex = "\\b(" + String.join("|",
                sensitiveWords.stream()
                        .map(Pattern::quote)
                        .toList()
        ) + ")\\b";
        this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
    public boolean containsSensitive(String text) {
        return pattern.matcher(text).find();
    }

    public String findFirstSensitiveWord(String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}