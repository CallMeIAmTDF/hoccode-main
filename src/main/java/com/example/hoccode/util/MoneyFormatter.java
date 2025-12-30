package com.example.hoccode.util;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormatter {

    private static final Locale VIETNAM = new Locale("vi", "VN");

    private MoneyFormatter() {}

    public static String format(Long amount) {
        if (amount == null) {
            return "0";
        }
        return NumberFormat.getInstance(VIETNAM).format(amount);
    }
}