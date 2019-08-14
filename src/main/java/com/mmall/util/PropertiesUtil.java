package com.mmall.util;

import com.sun.org.apache.xalan.internal.xsltc.dom.SimpleResultTreeImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 * @Author xuqian
 * @Date 2019/6/22 17:01
 */
public class PropertiesUtil {
    //声明一个logger
    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;

    //写PropertiesUtil的实现，在tomcat启动时就读取到Properties中的配置
    static {
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取错误",e);
        }
    }

    //工具类：通过mmall.properties中的Key获取value，因为是工具类所以是静态的
    public static String getProperty(String key){
        String value = props.getProperty(key.trim()); //trim()方法是去除字符串两边的空格，避免字符串两边好多空格影响字符串本身的值的情况
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    //上述方法的一个重载:多一个参数defaultValue，当所得value为空时将defaultValue返回，而不是返回null
    //这两种方法根据实际业务场景来选择
    public static String getProperty(String key, String defaultValue){
        String value = props.getProperty(key.trim()); //trim()方法是去除字符串两边的空格，避免字符串两边好多空格影响字符串本身的值的情况
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }




}
