package com.mzhang.cleantimer;

public class Common {
    public static String formatTime(int totalMillis) {
        int mins = totalMillis / 60000;
        double secs = (double)(totalMillis % 60000) / 1000;
        return String.format("%02d", mins) + ":" + String.format("%06.3f", secs);
    }
}
