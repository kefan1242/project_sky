package com.sky.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetMealDishMapper {
    /**
     * 根据菜品id查套餐
     * @param dishIds
     * @return
     */
    List<Long>getSetMealIdByDishId(List<Long> dishIds);

    /**
     * 删除菜品
     * @param id
     */
    @Delete("delete from dish where id =#{id}")
    void deleteById(Long id);
}
