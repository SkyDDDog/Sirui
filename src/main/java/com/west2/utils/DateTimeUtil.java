package com.west2.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @desc 日期工具类
 * @date 2022/11/26
 */
public class DateTimeUtil {

    private static String[] parsePatterns = new String[]{"yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    public DateTimeUtil() {
    }

    public static long secondsFromNow(long stopStamp) {
        return stopStamp-DateTimeUtil.nowTimeStamp();
    }

    public static long nowTimeStamp() {
        return (System.currentTimeMillis() / 1000);
    }

    /**
     * @desc 时间转时间戳
     * @param s 时间字符串(yyyy-MM-dd HH:mm:ss)
     * @return 时间戳字符串
     */
    public static String dateToStamp(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String res = "";
        if (!"".equals(s)) {
            try {
                res = String.valueOf(sdf.parse(s).getTime() / 1000);
            } catch (Exception e) {
                System.out.println("传入了null值");
            }
        } else {
            long time = System.currentTimeMillis();
            res = String.valueOf(time / 1000);
        }

        return res;
    }

    public static String stampToFormatDate(String s, String pattern) {
        int seconds = Integer.parseInt(s);
        Date date = new Date(seconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }


    public static LocalDateTime parseDate(Object str, String pattern) {
        if (str == null) {
            return null;
        } else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(str.toString(), dateTimeFormatter);
        }
    }

    public static LocalDateTime parseDate(Object str, int pattern) {
        if (str == null) {
            return null;
        } else {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(parsePatterns[pattern]);
            return LocalDateTime.parse(str.toString(), dateTimeFormatter);
        }
    }

    public static String stamp2ISO8601(String timestamp) {
        Date d = new Date(Integer.parseInt(timestamp)* 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        return sdf.format(d);
    }

}
