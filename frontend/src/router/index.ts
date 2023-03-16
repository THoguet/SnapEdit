import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
	history: createWebHistory(import.meta.env.BASE_URL),
	routes: [
		{
			path: '/:id(\\d)?',
			name: 'home',
			component: () => import('../components/Home.vue')
		},
		{
			path: '/gallery',
			name: 'gallery',
			component: () => import('../components/Gallery.vue')
		},
		{
			path: '/upload',
			name: "upload",
			component: () => import('../components/Upload.vue')
		},
		{
			path: '/:pathMatch(.*)*',
			name: 'not-found',
			component: () => import('../components/NotFound.vue')
		}
	]
})

export default router
