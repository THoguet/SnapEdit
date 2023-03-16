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
			file: null as FileList | null
		}
	},
	methods: {
		async updateImageList() {
			api.getImageList().then((newImages) => {
				const newImageList = new Map<number, ImageType>();
				for (const image of newImages.values())
					newImageList.set(image.id, image);
				this.images = newImageList;
			});
		},
		deleteFile(id: number) {
			api.deleteImage(id).then(() => {
				this.updateImageList();
			})
		}
	},
	created() {
		this.updateImageList();
	}
})
</script>
<template>
	<header>
		<div class="navi">
			<nav>
				<ul>
					<li><router-link :to="{ name: 'home' }">Home</router-link></li>
					<li><router-link :to="{ name: 'gallery' }">Gallery</router-link></li>
					<li><router-link :to="{ name: 'upload' }">Upload</router-link></li>
				</ul>
			</nav>
		</div>
	</header>
	<RouterView :images="images" @delete="deleteFile" @updateImageList="updateImageList()" />
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
}

.router-link-active {
	background-color: #646cff;
}

.router-link-active>a {
	color: rgb(205, 201, 194);
}

a {
	padding: 5px 15px;
	border-radius: 60px;
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
