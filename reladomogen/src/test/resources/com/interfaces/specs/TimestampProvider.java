package com.interfaces.specs;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;

public class TimestampProvider
{
    private static final Timestamp INFINITY_DATE = TimestampProvider.create(9999, 11, 1, Calendar.PM, 23, 59, 0, 0);

    private TimestampProvider()
    {
        throw new UnsupportedOperationException("utility methods only -- not instantiable");
    }

    private static Timestamp create(int year, int month, int dayOfMonth, int amPm, int hourOfDay, int minute, int second, int millisecond)
    {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.AM_PM, amPm);
        cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, millisecond);
        return new Timestamp(cal.getTimeInMillis());
    }

    /**
     * Infinity reference date.
     *
     * @return A timestamp representing date "9999-12-01 23:59:00.0"
     */
    public static Timestamp getInfinityDate()
    {
        return INFINITY_DATE;
    }

    public static Timestamp createBusinessDate(Date date)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        setBusinessDateTime(cal);
        return new Timestamp(cal.getTimeInMillis());
    }

    private static void setBusinessDateTime(Calendar cal)
    {
        cal.set(Calendar.AM_PM, Calendar.PM);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Converts the date passed to a Timestamp that is at 18:30 of the same day as the argument passed.
     *
     * @return a timestamp at 18:30 at the same day as the argument
     */
    public static Timestamp ensure1830(Date date)
    {
        return createBusinessDate(date);
    }

    public static Timestamp getNextDay(Timestamp businessDay)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(businessDay);
        cal.add(Calendar.DATE, 1);
        setBusinessDateTime(cal);
        return new Timestamp(cal.getTimeInMillis());
    }
}
