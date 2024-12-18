package cn.edu.zjut.dockermanager.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    /**
     * 使用 MD5 算法对输入的字符串进行加密
     * @param str 需要加密的字符串
     * @return 返回加密后的 16 进制 MD5 字符串
     * @author XanderYe
     * @date 2020/11/25
     */
    public static String md5(String str) {
        byte[] digest = null;
        try {
            // 获取 MD5 MessageDigest 实例
            MessageDigest md5 = MessageDigest.getInstance("md5");
            // 使用 UTF-8 字符集编码将字符串转为字节数组，并计算 MD5 摘要
            digest  = md5.digest(str.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // 若出现无法找到算法异常，打印堆栈信息
            e.printStackTrace();
        }
        // 将字节数组转为 16 进制字符串并返回
        // BigInteger(1, digest) 表示将 byte 数组转换为正整数
        return new BigInteger(1, digest).toString(16);
    }
}
