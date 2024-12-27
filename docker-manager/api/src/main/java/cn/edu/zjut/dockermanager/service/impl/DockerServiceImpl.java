package cn.edu.zjut.dockermanager.service.impl;

import cn.edu.zjut.dockermanager.util.DockerUtil;
import cn.edu.zjut.dockermanager.util.SystemUtil;
import cn.edu.zjut.dockermanager.service.DockerService;
import org.springframework.stereotype.Service;

@Service
public class DockerServiceImpl implements DockerService {
    @Override
    public String startDocker() {
        String cmd = getCommand("start");
        return SystemUtil.execStr(cmd);
    }

    @Override
    public String stopDocker() {
        String cmd = getCommand("stop");
        return SystemUtil.execStr(cmd);
    }

    @Override
    public String restartDocker() {
        String stopCmd = getCommand("stop");
        String startCmd = getCommand("start");
        return SystemUtil.execStr(stopCmd) + "\n" + SystemUtil.execStr(startCmd);
    }

    /**
     * 根据操作系统返回对应的命令
     */
    private String getCommand(String action) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) { // Windows 环境
            switch (action) {
                case "start":
                    return "net start com.docker.service";
                case "stop":
                    return "net stop com.docker.service";
                case "restart":
                    return "net stop com.docker.service && net start com.docker.service";
                default:
                    throw new IllegalArgumentException("Unsupported action: " + action);
            }
        } else { // Linux 环境
            return DockerUtil.systemd
                    ? "systemctl " + action + " docker"
                    : "service docker " + action;
        }
    }
}
