package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @Author xuqian
 * @Date 2019/6/12 11:15
 */
//以下注解的作用：保证在序列化json的时候，如果是null 的对象，key也会消失。例如：调用的构造器中只有status参数时，data和msg序列化后不会出现
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status){
        this.status = status;
    }
    private ServerResponse(int status, T data){
        this.status = status;
        this.data = data;
    }
    private ServerResponse(int status, String msg, T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }
    private ServerResponse(int status, String msg){
        this.status = status;
        this.msg = msg;
    }

    @JsonIgnore
    //使之不在json序列化结果中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode(); //如果是0则响应成功。
    }

    public int getStatus(){
        return status;
    }
    public T getData(){
        return data;
    }
    public String getMsg(){
        return msg;
    }

    //响应成功:status=0(ResponseCode.SUCCESS.getCode())
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode()); //调用参数为status的构造器
    }
    //若响应成功，还会返回一个msg，消息文本，供前端提示使用
    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg); //调用参数为msg的构造器
    }
    //若响应成功，还会返回一个data
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data); //调用参数为data的构造器
    }
    //若响应成功，还会返回一个msg和data
    public static <T> ServerResponse<T> createBySuccess(String msg, T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data); //调用参数为msg, data的构造器
    }

    //响应失败
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc()); //调用参数为status的构造器
    }

    //若响应失败，还会返回一个msg，消息文本，供前端提示使用
    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    //若响应失败，还会返回一个暴露服务端参数错误的提示
    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){
        return new ServerResponse<T>(errorCode,errorMessage);
    }


}
