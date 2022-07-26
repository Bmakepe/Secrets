package com.makepe.blackout.GettingStarted.OtherClasses;

import android.app.Application;
import android.content.Context;

public class GetTimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;
    private static final int MONTH_MILLIS = 4 * WEEK_MILLIS;
    private static final int YEAR_MILLIS = 12 * MONTH_MILLIS;

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
            return diff / MINUTE_MILLIS + " Mins Ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "An Hr Ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " Hrs Ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday";
        } else {
            return diff / DAY_MILLIS + " Days Ago";
        }

        // TODO: localize
        /*(final long diff = now - time;
        if (diff < MINUTE_MILLIS) {//within the minute - less than 60 seconds
            return "Just Now";
        } else if (diff < 2 * MINUTE_MILLIS) {//between a minute and 2 minutes
            return "A Min Ago";
        } else if (diff < 50 * MINUTE_MILLIS) {//within the hour
            return diff / MINUTE_MILLIS + " Mins Ago";
        } else if (diff < 90 * MINUTE_MILLIS) {//at the hour mark
            return "An Hr Ago";
        } else if (diff < 24 * HOUR_MILLIS) {//within 24 hours
            return diff / HOUR_MILLIS + " Hrs Ago";
        } else if (diff < 48 * HOUR_MILLIS) {//within 48 hours
            return "Yesterday";
        } else if (diff > 48 * HOUR_MILLIS && diff < 168 * HOUR_MILLIS){//within the week
            return diff / DAY_MILLIS + " Days Ago";
        } else if (diff > ){//weeks within the month

        } else if(){//at the month mark

        } else if(){//within the months before the year

        }else if(){//at the year mark

        }else{//after the year mark has hit

        }*/
    }

}
