package net.flectone.chat.util;

import java.util.Random;

public class RandomUtil {

    private static final Random RANDOM = new Random();

    public static int nextInt(int start, int end) {
        if (start > end) return 0;
        return start == end ? start : start + RANDOM.nextInt(end - start);
    }

    public static int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }

}
