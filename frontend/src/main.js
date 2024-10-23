import { createApp } from 'vue';
import App from './App.vue';
import router from './router';  // 确保正确导入路由器

const app = createApp(App);
app.use(router); // 使用路由器
app.mount('#app'); // 挂载应用到 DOM
