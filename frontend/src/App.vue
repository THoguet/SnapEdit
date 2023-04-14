<script setup lang="ts">
import Home from './components/Home.vue'
import Gallery from './components/Gallery.vue';
import NotFound from './components/NotFound.vue'
import Upload from './components/Upload.vue'
import { defineComponent } from 'vue'
import { api } from '@/http-api'
import { ImageType } from './image';
import { Filter, FilterType, RangeParameters, SelectParameters } from './filter';
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
			file: null as FileList | null,
			filters: [] as Filter[]
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
			if (!this.images.delete(id))
				console.warn("Cannot delete, image not found");
			api.deleteImage(id);
		}
	},
	created() {
		this.updateImageList();
		api.getAlgorithmList().then((filters) => {
			this.filters = filters;
			// fill value attribute
			for (const filter of this.filters) {
				for (const arg of filter.parameters) {
					if (arg.type === FilterType.range) {
						const range = arg as RangeParameters;
						range.value = range.min;
					}
					else if (arg.type === FilterType.select) {
						const select = arg as SelectParameters;
						select.value = select.options[0];
					}
				}
			}
		});
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
	<RouterView :filters="filters" :images="images" @delete="deleteFile($event)" @updateImageList="updateImageList()" />
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
