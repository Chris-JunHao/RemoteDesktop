<template>
  <div>
    <h1>VNC Viewer</h1>
    <div ref="vncContainer" style="border: 1px solid black; width: 800px; height: 600px;">
      <p v-if="connectionStatus">{{ connectionStatus }}</p>
    </div>
    <button @click="connectToVNC">Connect to VNC</button>
  </div>
</template>

<script>
import RFB from 'novnc-core'; // Ensure this is installed

export default {
  data() {
    return {
      rfb: null,
      connectionStatus: '' // To hold connection status messages
    };
  },
  methods: {
    connectToVNC() {
      const websocketUrl = 'ws://localhost:8080/vnc'; // WebSocket URL for the backend
      this.connectionStatus = 'Connecting to VNC server...'; // Update status message

      this.rfb = new RFB(this.$refs.vncContainer, websocketUrl);

      // Listen for connection event
      this.rfb.addEventListener('connect', () => {
        console.log('Connected to VNC server');
        this.connectionStatus = 'Connected to VNC server';
      });

      // Listen for disconnect event
      this.rfb.addEventListener('disconnect', () => {
        console.log('Disconnected from VNC server');
        this.connectionStatus = 'Disconnected from VNC server';
      });

      // Handle security failure event
      this.rfb.addEventListener('securityfailure', () => {
        console.error('Failed to connect to VNC server due to security failure');
        this.connectionStatus = 'Failed to connect to VNC server due to security failure';
      });

      // Handle other errors
      this.rfb.addEventListener('error', (error) => {
        console.error('Error:', error);
        this.connectionStatus = 'Error connecting to VNC server: ' + error;
      });
    }
  }
};
</script>

<style scoped>
/* Styles can be adjusted as needed */
</style>
