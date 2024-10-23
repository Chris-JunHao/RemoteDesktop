import { createRouter, createWebHistory } from 'vue-router';
import VncViewer from '../components/VncViewer.vue';

const routes = [
  {
    path: '/',
    name: 'VncViewer',
    component: VncViewer
  }
  // 你可以在此处添加其他路由
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

export default router;
