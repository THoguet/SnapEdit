<script setup lang="ts">
import Home from './components/Home.vue'
import Gallery from './components/Gallery.vue';
import NotFound from './components/NotFound.vue'
import Upload from './components/Upload.vue'
import StableDiff from './components/StableDiff.vue';
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
			this.images.delete(id);
			api.deleteImage(id);
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
					<li><router-link :to="{ name: 'generate' }">Generate Images</router-link></li>
				</ul>
			</nav>
		</div>
	</header>
	<RouterView :images="images" @delete="deleteFile" @updateImageList="updateImageList()" />
</template>
<style scoped>
@import url("@/navi.css");

header {
	display: block;
	position: fixed;
	padding-top: 30px;
	top: 0;
	left: 0;
	right: 0;
	pointer-events: none;
}

.router-link-active {
	background-color: #646cff;
}

.router-link-active>a {
	color: rgb(205, 201, 194);
}
</style>
