package cn.edu.zjut.dockermanager.config;

import cn.edu.zjut.dockermanager.util.DockerUtil;
import cn.edu.zjut.dockermanager.enums.ErrorCodeEnum;
import cn.edu.zjut.dockermanager.exception.BusinessException;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration  // 声明该类为Spring的配置类
public class DockerConfiguration {

    @PostConstruct  // 在类的构造函数执行后自动调用该方法
    public void init() {
        // 确保登录用户为root代码，暂时删除
//        String user = SystemUtil.execStr("whoami");
//        if (!"root".equals(user)) {
//            throw new BusinessException(ErrorCodeEnum.RUN_WITH_ROOT);
//        }

        // 检查Docker是否安装，若未安装抛出BusinessException
        if (!DockerUtil.checkDocker()) {
            throw new BusinessException(ErrorCodeEnum.INSTALL_DOCKER_FIRST);  // 如果未安装Docker，则抛出异常
        }
    }
}
