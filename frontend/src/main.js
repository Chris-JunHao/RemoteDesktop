import { createApp } from 'vue';
import App from './App.vue';
import router from './router';  // 确保路由器正确导入

const app = createApp(App);
app.use(router);  // 使用路由器
app.mount('#app');
