<template>
    <div id="vnc-container">
      <canvas id="vnc-canvas" ref="canvas"></canvas>
    </div>
  </template>
  
  <script>
  import * as RFB from 'novnc/core/rfb.js'; // 导入 noVNC 的核心库
  
  export default {
    name: 'VncViewer',
    data() {
      return {
        rfb: null, // VNC 连接对象
      };
    },
    mounted() {
      this.connectVnc();
    },
    methods: {
      connectVnc() {
        const host = 'localhost'; // VNC 服务器地址
        const port = '5901'; // VNC 服务器端口号
        const password = ''; // 如果有密码，可以在这里设置
        const path = ''; // WebSocket 路径
  
        const url = `ws://${host}:${port}/${path}`; // VNC WebSocket 地址
  
        // 创建 noVNC RFB 客户端实例
        this.rfb = new RFB(this.$refs.canvas, url, { credentials: { password } });
  
        // 设置一些事件监听器
        this.rfb.addEventListener('connect', () => {
          console.log('VNC 连接成功');
        });
  
        this.rfb.addEventListener('disconnect', () => {
          console.log('VNC 连接断开');
        });
  
        this.rfb.addEventListener('credentialsrequired', () => {
          this.rfb.sendCredentials({ password });
        });
  
        this.rfb.addEventListener('securityfailure', (e) => {
          console.error('VNC 连接安全失败', e.detail);
        });
      },
    },
  };
  </script>
  
  <style scoped>
  #vnc-container {
    width: 100%;
    height: 100vh;
  }
  
  #vnc-canvas {
    width: 100%;
    height: 100%;
  }
  </style>
  