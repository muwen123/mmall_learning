package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Author xuqian
 * @Date 2019/6/15 11:09
 */
public class TokenCache {
    //先声明日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    //将token_ 声明为常量
    public static final String TOKEN_PREFIX = "token_";

    //声明一个静态的内存块：localCache，其类型为LoadingCache<String,String>，其中<key, value>
    //localCache = 后面具体每个函数的作用在项目本上记录
    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
        //默认的数据加载实现，当调用get取值的时候，发现key没有对应的值，就调用此方法进行加载
        //该方法是匿名内部类：{}后跟一个;
        @Override
        public String load(String s) throws Exception {
            //return null; 写成下面的，因为如果是返回null，当执行key.equal或者forgetToken.equal(key)方法时会返回空指针异常
            return "null";
        }
    });

    public static void setKey(String key, String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;
        try{
           value = localCache.get(key);
           if("null".equals(value)){ //即key没有对应的value值，则返回null
               return null;
           }
           return value; //key有对应的value值，则返回该value值
        }catch (Exception e){
            //打印异常堆栈
            logger.error("localCache get error", e);
        }
        return null;
    }
}
