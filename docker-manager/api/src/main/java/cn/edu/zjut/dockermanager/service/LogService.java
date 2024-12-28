package cn.edu.zjut.dockermanager.service;

import cn.edu.zjut.dockermanager.entity.Log;

import java.sql.Timestamp;
import java.util.List;

public interface LogService {

    List<Log> getLogs(int limit, int offset);

    void insert(String username, String action, Timestamp time);

    int countLogs();
}

