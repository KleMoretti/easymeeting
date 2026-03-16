package com.easymeeting.mappers;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BaseMapper<T,P> {
    /**
     * 插入数据
     * @param t
     * @return
     */
    Integer insert(@Param("bean")T t);
    /**
     * 插入或更新数据
     * @param t
     * @return
     */
    Integer insertOrUpdate(@Param("bean")T t);
    /**
     * 批量插入数据
     * @param list
     * @return
     */
    Integer insertBatch(@Param("list") List<T> list);
    /**
     * 批量插入或更新数据
     * @param list
     * @return
     */
    Integer insertOrUpdateBatch(@Param("list") List<T> list);

    /**
     * 根据参数查询数据列表
     * @param p
     * @return
     */
    List<T> selectList(@Param("query")P p);
    /**
     * 根据参数查询数据总数
     * @param p
     * @return
     */
    Integer selectCount(@Param("query")P p);

}
