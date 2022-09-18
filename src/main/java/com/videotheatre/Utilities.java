package com.videotheatre;

import java.util.Random;

public class Utilities {

    private static Random random;

    public Utilities() {
        if(random == null) {
            random = new Random();
        }
    }

    public static int generateRandomNumber(int min, int max) {
        return (random.nextInt(max + 1 - min) + min);
    }
}
