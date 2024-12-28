package cn.edu.zjut.dockermanager.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Log {

    private int logId;
    private String accountName;
    private String action;
    private Timestamp logTime;

}
