package com.easymeeting.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component("redisUtils")
public class RedisUtils<V> {

    @Resource
    private RedisTemplate<String, V> redisTemplate;

    private static final Logger logger = LoggerFactory.getLogger(RedisUtils.class);

    /**
     * 删除缓存
     * @param key
     */
    public void delete(String... key){
        if(key!=null&&key.length>0){
            if(key.length==1){
                redisTemplate.delete(key[0]);
            }else{
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public V get(String key){
        return key==null?null:redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key
     * @return
     */
    public boolean set(String key,V value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        } catch (Exception e) {
            logger.error("redis set error,key={},value={}",key,value,e);
            return false;
        }
    }

    /**
     * 缓存放入并设置时间
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean setex(String key,V value,long time){
        try{
            if(time>0){
                redisTemplate.opsForValue().set(key,value,time, TimeUnit.SECONDS);
            }else{
                set(key,value);
            }
            return true;
        } catch (Exception e) {
            logger.error("redis set error,key={},value={}",key,value,e);
            return false;
        }
    }


    /**
     * Hash相关操作
     * @param key
     * @param hashKey
     * @param value
     */
    public void hset(String key,String hashKey,V value){
        redisTemplate.opsForHash().put(key,hashKey,value);
    }

    public V hget(String key,String hashKey){
        return (V)redisTemplate.opsForHash().get(key,hashKey);
    }
    public void hdel(String key,String... hashKey){
        redisTemplate.opsForHash().delete(key,hashKey);
    }
    public List<V> hvals(String key){
        return (List<V>) redisTemplate.opsForHash().values(key);
    }

}
