package cn.edu.zjut.dockermanager.service.impl;

import cn.edu.zjut.dockermanager.entity.Log;
import cn.edu.zjut.dockermanager.mapper.LogMapper;
import cn.edu.zjut.dockermanager.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private LogMapper logMapper;

    @Override
    public void insert(String username, String action, Timestamp time) {
        logMapper.insert(username,action,time);
    }

    @Override
    public List<Log> getLogs(int limit, int offset) {
        return logMapper.getLogs(limit, offset);
    }
    @Override
    public int countLogs(){
        return logMapper.countLogs();
    }
}
