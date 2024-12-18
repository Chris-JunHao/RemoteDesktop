package cn.edu.zjut.dockermanager.service.impl;

import cn.edu.zjut.dockermanager.util.DockerUtil;
import cn.edu.zjut.dockermanager.util.SystemUtil;
import cn.edu.zjut.dockermanager.service.DockerService;
import org.springframework.stereotype.Service;


@Service
public class DockerServiceImpl implements DockerService {
    @Override
    public String startDocker() {
        String cmd = DockerUtil.systemd ? "systemctl start docker" : "service docker start";
        return SystemUtil.execStr(cmd);
    }

    @Override
    public String stopDocker() {
        String cmd = DockerUtil.systemd ? "systemctl stop docker" : "service docker stop";
        return SystemUtil.execStr(cmd);
    }

    @Override
    public String restartDocker() {
        String cmd = DockerUtil.systemd ? "systemctl restart docker" : "service docker restart";
        return SystemUtil.execStr(cmd);
    }
}
