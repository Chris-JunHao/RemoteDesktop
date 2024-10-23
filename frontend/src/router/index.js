import { createRouter, createWebHistory } from 'vue-router';
import VncViewer from '../components/VncViewer.vue'; // Ensure the path is correct

const routes = [
  {
    path: '/',
    name: 'VncViewer',
    component: VncViewer,
  },
  // You can add more routes here if needed
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
