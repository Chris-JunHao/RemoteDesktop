package cn.edu.zjut.dockermanager.mapper;

import cn.edu.zjut.dockermanager.entity.Log;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface LogMapper {

    // 插入日志方法
    @Insert("INSERT INTO logs (account_name, action, log_time) " +
            "VALUES (#{accountName}, #{action}, #{logTime})")
    void insert(@Param("accountName") String accountName,
                @Param("action") String action,
                @Param("logTime") Timestamp logTime);

    @Select("SELECT * FROM logs ORDER BY log_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Log> getLogs(int limit, int offset);

    // 获取日志总数
    @Select("SELECT COUNT(*) FROM logs")
    int countLogs();
}
