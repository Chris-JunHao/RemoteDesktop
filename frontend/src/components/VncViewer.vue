<template>
  <div>
    <h1>VNC Viewer</h1>
    <div ref="vncContainer" style="border: 1px solid black; width: 800px; height: 600px;"></div>
    <button @click="connectToVNC">Connect to VNC</button>
  </div>
</template>

<script>
import RFB from 'novnc-core'; // 如果你使用的是 npm 安装的 noVNC

export default {
  data() {
    return {
      rfb: null
    };
  },
  methods: {
    connectToVNC() {
      const websocketUrl = 'ws://localhost:8080/vnc'; // WebSocket 连接后端的地址
      this.rfb = new RFB(this.$refs.vncContainer, websocketUrl);

      // 监听连接事件
      this.rfb.addEventListener('connect', () => {
        console.log('Connected to VNC server');
      });

      // 监听断开连接事件
      this.rfb.addEventListener('disconnect', () => {
        console.log('Disconnected from VNC server');
      });

      // 处理错误事件
      this.rfb.addEventListener('securityfailure', () => {
        console.error('Failed to connect to VNC server due to security failure');
      });
    }
  }
};
</script>

<style scoped>
/* 样式可以根据需要调整 */
</style>
