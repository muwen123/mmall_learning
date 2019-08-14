package com.mmall.common;

/**
 * @Author xuqian
 * @Date 2019/6/12 11:35
 */
//这个类是枚举类，用于枚举status的值
public enum ResponseCode {
    //以后想要扩展相应值status的值时，就在下面继续写就行
    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;

    ResponseCode(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public int getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
