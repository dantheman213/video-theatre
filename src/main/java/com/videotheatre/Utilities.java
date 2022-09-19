package com.videotheatre;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

    public static String decodeMediaSourceUrlToFilePath(String url) throws MalformedURLException, URISyntaxException {
        //return URLDecoder.decode(url, StandardCharsets.UTF_8).replaceAll("file://", "").replaceAll("//", File.separator);
        return new File(new URL(url).toURI()).getPath();
    }
}
