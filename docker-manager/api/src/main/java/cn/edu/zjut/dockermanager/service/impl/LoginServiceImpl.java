package cn.edu.zjut.dockermanager.service.impl;

import cn.edu.zjut.dockermanager.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.edu.zjut.dockermanager.util.MD5Util;
import cn.edu.zjut.dockermanager.mapper.LoginMapper;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private LoginMapper loginMapper;
    @Override
    public boolean match(String username, String password){
        // 使用MD5加密用户输入的密码
        String encryptedPassword = MD5Util.md5(password);

        // 从数据库中查询存储的加密密码
        String storedPassword = loginMapper.findPasswordHashByUsername(username);

        // 如果没有找到用户名或密码不匹配，返回false
        if (storedPassword == null) {
            return false;
        }

        // 比较数据库中的密码和用户输入密码是否一致
        return storedPassword.equals(encryptedPassword);
    }
}
