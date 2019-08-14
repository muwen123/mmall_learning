package com.mmall.common;

import com.google.common.collect.Sets;
import org.apache.commons.lang.enums.Enum;

import java.util.Set;

/**
 * @Author xuqian
 * @Date 2019/6/12 16:28
 */

//常量类
public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface ProductListOrderBy{
        Set<String> PRICE_ASC_DESC = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int CHECKED = 1; //即购物车中产品为选中
        int UN_CHECKED = 0; //购物车中产品未选中

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";

    }

    public interface Role{
        int ROLE_CUSTOMER = 0;  //普通用户
        int ROLE_ADMIN = 1; //管理员
    }

    public enum ProductStatusEnum{
        ON_SALE(1,"在线");

        private String value;
        private int code;
        ProductStatusEnum(int code, String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }
    }


    //支付状态枚举，需要有汉字，所以不用通常定义常量的方法来定义常量
    public enum OrderStatusEnum{
        //利用构造函数传参
        CANCELED(0,"已取消"),
        NO_PAY(10,"未支付"),
        PAID(20,"已付款"),
        SHIPPED(40,"已发货"),
        ORDER_SUCCESS(50,"订单完成"),
        ORDER_CLOSE(60,"订单关闭");

        //定义私有变量
        private String value;
        private int code;

        //定义构造器，传参
        OrderStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public static OrderStatusEnum codeOf(int code){
            for(OrderStatusEnum orderStatusEnum : values()){
                if(orderStatusEnum.getCode() == code){
                    return orderStatusEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }

    //回调状态，直接定义常量，不需要用后面的汉字
    public interface AlipayCallback{
        String TRADE_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY"; //交易创建，等待买家付款
        String TRADE_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS"; //交易支付成功

        String RESPONSE_SUCCESS = "success";
        String RESPONSE_FAILED = "failed";
    }

    //支付平台
    public enum PayPlatformEnum{

        //利用构造函数传参
        ALIPAY(1,"支付宝");

        //定义私有变量
        private String value;
        private int code;

        //定义构造器，传参
        PayPlatformEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }
    }

    //支付方式
    public enum PaymentTypeEnum{
        //利用构造函数传参
        ONLINE_PAY(1,"在线支付");

        //定义私有变量
        private String value;
        private int code;

        //定义构造器，传参
        PaymentTypeEnum(int code,String value){
            this.code = code;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public int getCode() {
            return code;
        }

        public static PaymentTypeEnum codeOf(int code){
            for(PaymentTypeEnum paymentTypeEnum : values()){
                if(paymentTypeEnum.getCode() == code){
                    return paymentTypeEnum;
                }
            }
            throw new RuntimeException("没有找到对应的枚举");
        }
    }



}
