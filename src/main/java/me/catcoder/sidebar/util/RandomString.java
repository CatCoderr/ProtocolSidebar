package me.catcoder.sidebar.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RandomString {

    private final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * ALPHABET.length());
            sb.append(ALPHABET.charAt(randomIndex));
        }
        return sb.toString();
    }
}
