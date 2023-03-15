<script setup lang="ts">
import Home from './components/Home.vue'
import Gallery from './components/Gallery.vue';
import NotFound from './components/NotFound.vue'
import Upload from './components/Upload.vue'
import { defineComponent } from 'vue'
import { api } from '@/http-api'
import { ImageType, ImageClass } from './image';
</script>
<script lang="ts">
const routes: { [key: string]: string } = {
	'/': "Home",
	'Gallery': "Gallery",
	'Upload': "Upload"
}
export default defineComponent({
	components: {
		Home,
		Gallery,
		NotFound,
		Upload
	},
	data() {
		return {
			images: new Map<Number, ImageType>(),
			currentPath: window.location.hash,
			sent: false,
			file: null as FileList | null
		}
	},
	computed: {
		currentView(): string {
			return routes[this.currentPath.slice(1) || '/'] || "notFound";
		}
	},
	methods: {
		async updateImageList(sleep: number) {
			setTimeout(() => {
				api.getImageList().then((newImages) => {
					const newImageList = new Map<number, ImageType>();
					for (const image of newImages.values())
						newImageList.set(image.id, image);
					this.images = newImageList;
				})
			}, sleep);
		},
		deleteFile(id: number) {
			api.deleteImage(id).then(() => {
				this.updateImageList(100);
			})
		},
		isActive(route: string) {
			if (route === undefined) {
				if (this.currentPath === "")
					return 'router active';
				route = "/";
			}
			return this.currentPath.endsWith(route) ? 'router active' : 'router';
		}
	},
	mounted() {
		this.updateImageList(0);
		window.addEventListener('hashchange', () => {
			this.currentPath = window.location.hash;
		});
	}
})
</script>
<template>
	<header>
		<div class="navi">
			<nav>
				<ul>
					<li v-for="route in routes" :class="isActive(routes[route])">
						<a :href="'#' + (routes[route] || '/')"> <span>{{ route }}</span></a>
					</li>
				</ul>
			</nav>
		</div>
	</header>
	<component :is="currentView" :key="sent" :images="images" @delete="deleteFile"
		@updateImageList="updateImageList(100)" />
</template>
<style scoped>
.navi {
	/* reduce size of children */
	display: flex;
	flex-direction: row-reverse;
	pointer-events: none;
}

header {
	display: block;
	position: fixed;
	padding-top: 30px;
	top: 0;
	left: 0;
	right: 0;
	pointer-events: none;
}

ul>li+li {
	margin-left: 30px;
}

ul>li {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 5px 15px;
	border-radius: 60px;
}

.active {
	background-color: #646cff;
}

.active>a {
	color: rgb(205, 201, 194);
}

a {
	color: rgb(172, 165, 154);
	text-decoration: none;
	font-family: "Aeonik", sans-serif;
}

nav {
	display: flex;
	justify-content: center;
	padding-left: 20px;
	height: 60px;
	padding-right: 20px;
	border-radius: 60px;
	background-color: rgb(24, 26, 27);
	margin-right: 20px;
	pointer-events: auto;
}


ul {
	display: flex;
	list-style: none;
	padding-inline-start: 0;
}
</style>
