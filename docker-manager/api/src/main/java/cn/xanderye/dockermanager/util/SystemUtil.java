package cn.xanderye.dockermanager.util;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class SystemUtil {

    /**
     * 空格
     */
    public static final String BREAK = " ";
    /**
     * Tab
     */
    public static final String TAB = "    ";
    /**
     * Windows换行符
     */
    public static final String WINDOWS_LINE_BREAK = "\r\n";
    /**
     * UNIX换行符
     */
    public static final String UNIX_LINE_BREAK = "\r";

    public static final String host = "192.168.177.128";  // 远程虚拟机 IP 地址
    public static final String user = "zpf";      // 远程虚拟机用户名
    public static final String password = "493827";  // 远程虚拟机密码

    /**
     * 执行命令行命令，并返回命令执行结果。
     * 此方法调用时默认使用系统默认字符集。
     *
     * @param cmdStr 命令字符串
     * @return 执行结果的字符串
     */
    public static String execStr(String cmdStr) {
        return execStr(getCharset(), host , user , password ,cmdStr);
    }

    /**
     * 执行命令行命令，并返回命令执行结果。可以指定字符集来处理命令输出。
     * 该方法允许通过分割命令字符串来执行多个命令。
     *
     * @param charset 处理输出的字符集
     * @param cmds 命令参数，可以是多个命令分开传递，或者是一个包含多个命令的字符串（会根据 BREAK 分隔符拆分）
     * @return 执行结果的字符串
     * @throws NullPointerException 如果传入的命令为空或空字符串，将抛出空指针异常
     */
    public static String execStr(Charset charset, String host, String user, String password, String... cmds) {
        // 如果只有一个命令字符串，将其按 BREAK 分隔符拆分成多个命令
        if (1 == cmds.length) {
            if (cmds[0] == null || "".equals(cmds[0])) {
                throw new NullPointerException("Empty command !");
            }
            cmds = cmds[0].split(BREAK);
        }

        Session session = null;
        ChannelExec channel = null;
        BufferedReader buffer = null;
        StringBuilder sb = new StringBuilder();

        try {
            // 使用 JSch 创建一个 SSH 会话
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);  // 使用提供的虚拟机 IP 地址和用户名
            session.setPassword(password);               // 设置密码
            session.setConfig("StrictHostKeyChecking", "no");  // 禁止验证主机密钥

            // 连接到远程虚拟机
            session.connect();

            // 创建执行命令的 channel
            channel = (ChannelExec) session.openChannel("exec");
            String command = String.join(" ", cmds);  // 将命令数组连接为一个单一的命令字符串
            channel.setCommand(command);

            // 获取执行结果的输入流
            InputStream inputStream = channel.getInputStream();
            channel.connect();  // 开始执行命令

            // 使用字符集包装输入流，读取命令的输出
            buffer = new BufferedReader(new InputStreamReader(inputStream, charset));
            String line;

            // 逐行读取输出并拼接
            while ((line = buffer.readLine()) != null) {
                sb.append(line).append(System.lineSeparator()); // 添加行分隔符
            }

            // 返回命令输出的字符串（去除末尾的空格和换行符）
            return sb.toString().trim();
        } catch (Exception e) {
            e.printStackTrace(); // 如果出错，打印异常信息
        } finally {
            // 关闭流、通道和会话
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        // 如果发生异常或其他问题，返回 null
        return null;
    }


    /**
     * 判断系统环境
     * @param
     * @return boolean
     * @author XanderYe
     * @date 2020/11/5
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 获取系统字符编码
     * @param
     * @return java.nio.charset.Charset
     * @author XanderYe
     * @date 2020/11/5
     */
    public static Charset getCharset() {
        return isWindows() ? Charset.forName("GBK") : Charset.defaultCharset();
    }

    /**
     * 获取系统换行符
     * @param
     * @return java.lang.String
     * @author XanderYe
     * @date 2020/11/5
     */
    public static String getLineBreak() {
        return isWindows() ? WINDOWS_LINE_BREAK : UNIX_LINE_BREAK;
    }
}
