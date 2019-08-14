package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.util.Date;

/**
 * @Author xuqian
 * @Date 2019/6/23 11:59
 */
public class DateTimeUtil {
    //使用joda-time 开源时间/日期库来处理日期和时间，相比于Calendar很方便，代码少

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";

    //方法1：str->Date
    public static Date strToDate(String dateTimeStr, String formatStr){ //参数1：str,参数2是要转换成的格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr); //将String类型的转换成DateTime类型的
        return dateTime.toDate(); //将DateTime类型的转换成Date类型的
    }

    //方法2：Date->str
    public static String dateToStr(Date date, String formatStr){ //参数1：str,参数2是要转换成的格式
        if(date == null){
            return StringUtils.EMPTY; //EMPTY表示""
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }

    //以上两种方法的重载
    public static Date strToDate(String dateTimeStr){ //参数1：str,参数2是要转换成的格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr); //将String类型的转换成DateTime类型的
        return dateTime.toDate(); //将DateTime类型的转换成Date类型的
    }
    public static String dateToStr(Date date){ //参数1：str,参数2是要转换成的格式
        if(date == null){
            return StringUtils.EMPTY; //EMPTY表示""
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }

    //main函数测试
    public static void main(String[] args) {
        System.out.println(DateTimeUtil.dateToStr(new Date(),STANDARD_FORMAT));
        System.out.println(DateTimeUtil.strToDate("2019-06-23 20:20:20", STANDARD_FORMAT)); //结果为Date类型的：Sun Jun 23 20:20:20 CST 2019
        System.out.println(DateTimeUtil.dateToStr(new Date()));
        System.out.println(DateTimeUtil.strToDate("2019-01-01 11:11:11"));
    }

}
