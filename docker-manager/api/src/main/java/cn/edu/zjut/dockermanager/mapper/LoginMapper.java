package cn.edu.zjut.dockermanager.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LoginMapper {
    // 根据用户名查询用户密码
    @Select("SELECT password_hash FROM accounts WHERE username = #{username}")
    String findPasswordHashByUsername(String username);

}

