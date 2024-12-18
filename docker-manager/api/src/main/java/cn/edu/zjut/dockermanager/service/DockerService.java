package cn.edu.zjut.dockermanager.service;

public interface DockerService {

    /**
     * 启动 Docker
     * 启动 Docker 服务，使其能够正常运行并进行容器管理。
     * @return 返回 Docker 启动的结果信息
     * @author XanderYe
     * @date 2020/11/25
     */
    String startDocker();

    /**
     * 停止 Docker
     * 停止 Docker 服务，容器管理功能将不可用。
     * @return 返回 Docker 停止的结果信息
     * @author XanderYe
     * @date 2020/11/25
     */
    String stopDocker();

    /**
     * 重启 Docker
     * 重启 Docker 服务，通常用于解决 Docker 服务异常或配置更新等问题。
     * @return 返回 Docker 重启的结果信息
     * @author XanderYe
     * @date 2020/11/25
     */
    String restartDocker();
}
