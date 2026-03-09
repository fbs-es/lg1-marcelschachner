package fbs.lg1;

import java.util.Random;

public class NumberGenerator {

    private static final Random random = new Random();

    public static int generateNumber(int uG, int oG) {
        return uG + random.nextInt(oG - uG + 1);
    }

    public static int binarySearchGuess(int uG, int oG) {
        return uG + (oG - uG) / 2;
    }
}
