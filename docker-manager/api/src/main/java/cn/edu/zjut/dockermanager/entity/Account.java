package cn.edu.zjut.dockermanager.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Account {
    private String username;
    private String password;
    private LocalDateTime createdTime;
}
