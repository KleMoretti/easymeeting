package com.easymeeting.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataUtils {
    private static final Logger logger = LoggerFactory.getLogger(DataUtils.class);
    private static final Object lockObj = new Object();
    private static Map<String, ThreadLocal<SimpleDateFormat>> sdfMap = new HashMap<String, ThreadLocal<SimpleDateFormat>>();

    private static SimpleDateFormat getSdf(final String pattern) {
        ThreadLocal<SimpleDateFormat> tl = sdfMap.get(pattern);
        if (tl == null) {
            synchronized (lockObj) {
                tl = sdfMap.get(pattern);
                if (tl == null) {
                    tl = new ThreadLocal<SimpleDateFormat>() {
                        @Override
                        protected SimpleDateFormat initialValue() {
                            return new SimpleDateFormat(pattern);
                        }
                    };
                    sdfMap.put(pattern, tl);
                }
            }
        }
        return tl.get();
    }

    public static String format(Date date, String patten) {
        return getSdf(patten).format(date);
    }

    public static Date parse(String dateStr, String patten) {
        try {
            return getSdf(patten).parse(dateStr);
        } catch (Exception e) {
            logger.error("日期解析失败, dateStr={}, pattern={}", dateStr, patten, e);
        }
        return null;
    }
}
