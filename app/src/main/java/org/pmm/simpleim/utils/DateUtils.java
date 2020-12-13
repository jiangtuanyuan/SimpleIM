package org.pmm.simpleim.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 时间工具类
 * Created by caoyu on 2017/11/29/029.
 */

public class DateUtils {


    public static String getTime(Date date, boolean[] booleans) {
        if (booleans[1]) {
            //可根据需要自行截取数据显示
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format(date);
        } else {
            //可根据需要自行截取数据显示
            SimpleDateFormat format = new SimpleDateFormat("yyyy");
            return format.format(date);
        }
    }

    public static String getTime(Date date) {
        //可根据需要自行截取数据显示
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    public static String getYearMonthDay(long time) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date(time);
        return fmt.format(d);
    }

    public static String getHour(long time) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        Date d = new Date(time);
        return fmt.format(d);
    }

    /**
     * 判斷是否時過期時間
     *
     * @param targetDate
     * @return true 过期 ， false 没有过期
     */
    public static boolean comparaData(String targetDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String currentTime = df.format(d);
        try {
            Date targetTime = df.parse(targetDate);
            Date curentTime = df.parse(currentTime);
            if (targetTime.getTime() <= curentTime.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判斷是否時過期時間
     *
     * @param targetDate
     * @return true 过期 ， false 没有过期
     */
    public static boolean comparaMonth(String targetDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date();
        String currentTime = df.format(d);
        try {
            Date targetTime = df.parse(targetDate);
            Date curentTime = df.parse(currentTime);
            if (targetTime.getTime() <= curentTime.getTime()) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 时间戳转换为字符串
     *
     * @param time:时间戳
     * @return
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }


    /**
     * 时间戳转换为字符串
     *
     * @param time:时间戳
     * @return
     */
    public static String getToday(long time) {
        Date d = new Date(time);
        SimpleDateFormat sf = new SimpleDateFormat("yyyy年MM月");
        return sf.format(d);
    }

    /**
     * 判断时间是不是今天
     *
     * @param date
     * @return 是返回true，不是返回false
     */
    public static boolean isNow(String date) {
        //当前时间
        Date now = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        //获取今天的日期
        String nowDay = sf.format(now);
        //对比的时间
        return date.equals(nowDay);

    }

    /**
     * 判断addtime是否在七天之内
     *
     * @param addtime
     * @return
     */
    public static boolean isLatestWeek(String addtime) {
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date now1;
        Date now2;
        try {
            now1 = format1.parse(getBeforeNow());
            now2 = format1.parse(addtime);
            Calendar calendar = Calendar.getInstance();  //得到日历
            calendar.setTime(now1);//把当前时间赋给日历
            calendar.add(Calendar.DAY_OF_MONTH, -7);  //设置为7天前
            Date before7days = calendar.getTime();   //得到7天前的时间
            if (before7days.getTime() <= now2.getTime())
                return true;

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取前一天的时间 yyyy-MM-dd
     * return:String
     */
    public static String getBeforeNow() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        date = calendar.getTime();
        return sdf.format(date);
    }

    /**
     * 通过日期判断是周几
     *
     * @throws ParseException
     */
    public static String DateToDay(String daydate) {
        String dayNames[] = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar c = Calendar.getInstance();// 获得一个日历的实例
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            c.setTime(sdf.parse(daydate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dayNames[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    /**
     * 获取时间 yyyy-MM-dd hh:mm:ss
     */

    public static String getDate() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");//如果hh为小写 那么就搜12小时制 如果为大写 那么就是24小时制
            Date day = new Date();
            return df.format(day);
        } catch (Exception e) {
            return "0000-00-00 00:00";
        }
    }

    public static String getDateYMDHMS() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//如果hh为小写 那么就搜12小时制 如果为大写 那么就是24小时制
            Date day = new Date();
            return df.format(day);
        } catch (Exception e) {
            return "0000-00-00 00:00";
        }
    }

    /**
     * 获取时间 yyyy-MM-dd hh:mm:ss
     */

    public static String getDateYMD() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//如果hh为小写 那么就搜12小时制 如果为大写 那么就是24小时制
            Date day = new Date();
            return df.format(day);
        } catch (Exception e) {
            return "0000-00-00";
        }

    }
}
