package com.mmall.util;

import java.math.BigDecimal;

/**
 * @Author xuqian
 * @Date 2019/6/28 20:34
 */
public class BigDecimalUtil {
    //此工具类不能在外部实例化，所以构造器是私有的
    private BigDecimalUtil(){

    }

    //加减乘除运算
    public static BigDecimal add(double d1, double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double d1, double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double d1, double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double d1, double d2){
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP); //四舍五入，保留两位小数

        //除不尽的情况--用divide的重载方法：divide(BigDecimal divisor, int scale, int roundingMode)


    }
}
