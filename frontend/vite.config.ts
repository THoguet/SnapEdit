import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from "path";

// https://vitejs.dev/config/
export default defineConfig({
	plugins: [vue()],
	resolve: {
		alias: {
			"@": path.resolve(__dirname, "./src"),
		},
	},
	server: {
		proxy: {
			'^/images': {
				target: 'http://localhost:8080' // Spring boot backend address
			},
			'^/algorithms': {
				target: 'http://localhost:8080'
			}
		}
	},
	build: {
		outDir: 'target/dist',
		assetsDir: 'static'
	}
})
