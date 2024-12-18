package cn.edu.zjut.dockermanager.util;

import com.jcraft.jsch.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class SystemUtil {

    // 定义常用的空格、Tab符号和换行符
    public static final String BREAK = " "; // 空格
    public static final String TAB = "    "; // Tab
    public static final String WINDOWS_LINE_BREAK = "\r\n"; // Windows换行符
    public static final String UNIX_LINE_BREAK = "\r"; // UNIX换行符

    // 远程虚拟机的连接信息
    public static final String host = "192.168.177.128";  // 远程虚拟机的 IP 地址
    public static final String user = "zpf";  // 远程虚拟机的用户名
    public static final String password = "493827";  // 远程虚拟机的密码

    /**
     * 执行命令行命令，并返回命令执行结果。此方法使用系统默认字符集进行输出处理。
     *
     * @param cmdStr 命令字符串
     * @return 执行结果的字符串
     */
    public static String execStr(String cmdStr) {
        return execStr(getCharset(), host, user, password, cmdStr);
    }

    /**
     * 执行命令行命令，并返回命令执行结果。允许指定字符集来处理命令输出。
     * 如果命令字符串包含多个命令，可通过空格分隔传递。
     *
     * @param charset 字符集，用于处理命令输出
     * @param cmds 命令参数，可以是多个命令分开传递，也可以是一个包含多个命令的字符串（会根据空格分隔符拆分）
     * @return 执行结果的字符串
     * @throws NullPointerException 如果传入的命令为空或空字符串，将抛出空指针异常
     */
    public static String execStr(Charset charset, String host, String user, String password, String... cmds) {
        // 如果只有一个命令字符串，将其按空格分隔符拆分成多个命令
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
     * 判断当前操作系统是否为 Windows 系统
     * @return 如果是 Windows 系统，返回 true；否则返回 false
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 获取当前操作系统的字符编码
     * @return 当前操作系统使用的字符集
     */
    public static Charset getCharset() {
        return isWindows() ? Charset.forName("GBK") : Charset.defaultCharset();
    }

    /**
     * 获取当前操作系统的换行符
     * @return 当前操作系统使用的换行符（Windows 系统为 "\r\n"，其他系统为 "\r"）
     */
    public static String getLineBreak() {
        return isWindows() ? WINDOWS_LINE_BREAK : UNIX_LINE_BREAK;
    }
}
