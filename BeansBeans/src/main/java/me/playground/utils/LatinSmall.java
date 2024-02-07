package me.playground.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;

import java.util.Formatter;
import java.util.concurrent.ExecutionException;

public class LatinSmall {

    private static final LoadingCache<String, String> conversionCache = CacheBuilder.from("expireAfterAccess=3m").build(
            new CacheLoader<>() {
                @Override
                public @NotNull String load(@NotNull String s) {
                    return convert(s);
                }
            });

    public static String translate(String s) {
        try {
            return conversionCache.get(s.toUpperCase());
        } catch (ExecutionException ignored) {
            return s;
        }
    }

    private static String convert(String s) {
        int len = s.length();
        Formatter smallCaps = new Formatter(new StringBuilder(len));
        for (int i = -1; ++i < len;) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z' && c != 'X') {
                smallCaps.format("%c", Character.codePointOf("LATIN LETTER SMALL CAPITAL " + c));
            } else if (c == 'X') {
                smallCaps.format("%c", 'x');
            } else {
                smallCaps.format("%c", c);
            }
        }
        return smallCaps.toString();
    }

}
