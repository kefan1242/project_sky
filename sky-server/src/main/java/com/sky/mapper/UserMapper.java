package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("Select *from user where openid=#{openid}")
    User getByOpenId(@Param("openid") String openid);

    void insert(User user);

    @Select("Select *from user where id=#{id}")
    User getById(String id);
}
