package io.onemfive.core.util;

import java.util.Random;

/**
 * TODO: Add Description
 *
 * @author objectorange
 */
public final class Numbers {

    public static int randomNumber(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

}
