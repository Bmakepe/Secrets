package com.makepe.blackout.GettingStarted.OtherClasses;

import android.app.Application;
import android.content.Context;

public class GetTimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Just Now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "A Min Ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return "about " + diff / MINUTE_MILLIS + " Mins Ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "An Hr Ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "about " + diff / HOUR_MILLIS + " Hrs Ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return diff / DAY_MILLIS + " Days Ago";
        }
    }

}
