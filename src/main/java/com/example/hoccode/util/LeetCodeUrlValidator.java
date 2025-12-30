package com.example.hoccode.util;

import java.util.regex.Pattern;

public class LeetCodeUrlValidator {

    private static final Pattern LEETCODE_URL_PATTERN = Pattern.compile(
        "^https?:\\/\\/(www\\.)?leetcode\\.com\\/problems\\/[a-z0-9-]+(\\/.*)?$"
    );

    public static boolean isLeetCodeUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return LEETCODE_URL_PATTERN.matcher(url).matches();
    }
}