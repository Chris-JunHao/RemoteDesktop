package cn.edu.zjut.dockermanager.service;

public interface LoginService {
    boolean match(String username, String password);
}
