# 从头开始构建VncServer Docker镜像

## 1. 初始化容器

从官方的基础镜像 Ubuntu 22.04 开始构建

```bash
sudo docker run --name vncserver -p 5900:5901 -p 6080:6080 -di ubuntu:22.04
```

参数说明：
--name vncserver 将容器命名为 vncserver；
-p 5900:5901 将容器内的 5901 端口映射到宿主机的 5900 端口（VNC 的默认端口），方便外部 VNC 客户端直接连接；

-p 6080:6080 映射 noVNC 端口；
-d 使容器在后台运行；
-i 保持容器的交互状态（由于容器未配置后台程序，缺少此参数会导致容器立即退出）。这两个参数可合并为 -di；
ubuntu:22.04 指定使用 Ubuntu 22.04 版本的基础镜像。

执行结果如下图：

![本地图片](https://www.hnote.cc/wp-content/uploads/2023/04/Screenshot-from-2023-04-09-11-31-07.png)

输入 `sudo docker ps -a` 命令可以查看当前所有容器的状态。

![本地图片](https://www.hnote.cc/wp-content/uploads/2023/04/Screenshot-from-2023-04-09-11-39-39-1024x47.png)

接下来，登录到这个容器：

```bash
sudo docker exec -it 88dc /bin/bash
```

其中，-it 参数用于创建交互式终端；88dc 是容器的 ID（虽然完整 ID 较长，但只需输入能够区分其他容器的前几位即可）；/bin/bash 指定要在容器中运行的 shell 程序。

## 2. 增加用户

这个容器里目前只有root用户，但是root用户下执行许多程序会有莫名其妙的坑，与下面所要展示的步骤结果会有很多不同，然后你一通谷哥和度娘，也不明所以。还是先增加一个非root用户用于运行服务程序吧。

```bash
#先更新系统
apt update
#因为初始化的Ubuntu环境是一个极简系统，sudo也需要安装
apt install sudo
#修改root密码，主要是为了设定自己的密码，便于从其他用户转回root
#change root password
passwd
#增加用户run
adduser run
#将新建用户加入sudo组
usermod -aG sudo run
#后面将用新用户来执行指令
su run
```

## 3. **安装配置GUI环境**

Linux有多种GUI界面可选，Ubuntu常用的是gnome。为减少内存需求，还是安装一个轻量的xfce4

```bash
#安装xorg
sudo apt-get install xorg openbox
#安装xfce4
sudo apt install xfce4 xfce4-goodies
#安装GUI需要的字体
sudo apt install xfonts-100dpi
sudo apt install xfonts-75dpi
#安装终端
sudo apt-get install xfce4-terminal
#选择设定终端为xfce4-terminal
sudo update-alternatives --config x-terminal-emulator
#安装中文环境
sudo apt install locales
#设置语言环境，建议选择
#160. en_US.UTF-8 UTF-8， 491. zh_CN.GBK GBK，492. zh_CN.UTF-8 UTF-8
#缺省为zh_CN.UTF-8
sudo dpkg-reconfigure locales
#安装基本的中文字体
sudo apt install fonts-wqy-zenhei
#选为中文环境
echo "export LANG=zh_CN.UTF-8" >>  ~/.bashr
```

## 4. 安装tightvncserver

Linux有多种VNC服务器，这里我们选择较为常用的TightVNC Server

```bash
#安装tightvncserver
sudo apt install tightvncserver
#设置VNC密码
vncserver
vncserver -kill :1
#将startxfce4加入VNC的配置文件xstartup
echo "startxfce4 &" >> ~/.vnc/xstartup
```

首次执行vncserver时，系统会提示设置VNC客户端的登录密码，并询问是否需要设置一个仅供查看的密码（这里我们不需要）。执行后效果如下图：

![本地图片](https://www.hnote.cc/wp-content/uploads/2023/04/Screenshot-from-2023-04-09-14-21-25.png)

从上图可以看到，一个VNC Server实例已经启动并在监听5901端口（标记为:1）。由于现在只是配置阶段，我们随即通过vncserver -kill :1命令终止了该实例。为了使VNC Server使用xfce4作为图形界面，我们需要在VNC Server的启动配置文件~/.vnc/xstartup中添加startxfce4启动命令。

接下来，我们编写一个用于启动和停止vncserver的脚本：

```bash
#!/bin/sh
# /etc/init.d/vncserver
VNCUSER='run'
case "$1" in
        start)
                rm -r -f /tmp/.X11-unix
                rm -r -f /tmp/.X1-lock
                su $VNCUSER -c 'vncserver :1 -geometry 1600x900'
                echo "Starting VNC Server for $VNCUSER"
        ;;
        stop)
                su $VNCUSER -c 'vncserver -kill :1'
                echo "TightVNC Server stopped"
        ;;
        *)
                echo "Usage: /etc/init.d/vncserver {start|stop}"
                exit 1
        ;;
esac
exit 0
```

这个脚本有两个重要设置：首先，必须指定非root用户来执行vncserver，否则无法正常运行；其次，在启动前需要删除/tmp/.X11-unix和/tmp/.X1-lock这两个文件，因为它们可能是上次非正常退出时留下的，会影响新实例的启动。

将脚本保存到/etc/init.d/vncserver，并设置可执行权限：

```bash
sudo chmod 755 /etc/init.d/vncserver
```

虽然将脚本放在/etc/init.d/目录下是为了系统启动时自动执行，但在Docker容器中，传统的Linux自启动方式都不起作用。

现在VNC Server的安装配置已经完成，可以通过以下命令手动启动服务：

```bash
sudo /etc/init.d/vncserver start
```

终端结果输出如下

![本地图片](https://www.hnote.cc/wp-content/uploads/2023/04/Screenshot-from-2023-04-09-14-50-00.png)

## 5. 安装 noVNC 服务器

进入容器后，按照以下步骤安装 `noVNC` 和它的依赖项。

1. **更新容器内的包管理器**：

    ```bash
    apt update
    ```

2. **安装 `git` 和 `python3`**，因为 `noVNC` 需要这些工具来安装和运行：

    ```bash
    apt install -y git python3 python3-pip
    ```

3. **克隆 `noVNC` 仓库**：

    ```bash
    git clone https://github.com/novnc/noVNC.git /opt/noVNC
    ```

4. **安装 `websockify`**，它是 `noVNC` 运行所需的 WebSocket 代理：

    ```bash
    cd /opt/noVNC
    sudo pip3 install websockify
    ```

5. **启动 `noVNC`**：

    你需要将 `noVNC` 连接到 VNC 服务，并让它通过 WebSockets 转发流量。你可以使用以下命令启动 `noVNC`：

    ```bash
    ./utils/novnc_proxy --vnc localhost:5901
    ```

    这里，`localhost:5901` 是你之前配置的 VNC 服务器的地址（对应 Docker 容器中的 VNC 服务）。这个命令会启动一个 WebSocket 代理，将 VNC 数据转发到浏览器。
