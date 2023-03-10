<script setup lang="ts">
import Home from './components/Home.vue'
import Gallery from './components/Gallery.vue';
import NotFound from './components/NotFound.vue'
import { defineComponent } from 'vue'
import { api } from '@/http-api'
import { ImageType, ImageClass } from './image';
</script>
<script lang="ts">
const routes: { [key: string]: string } = {
	'/': "Home",
	'Gallery': "Gallery"
}
interface InputFileEvent extends Event {
	target: HTMLInputElement;
}
export default defineComponent({
	components: {
		Home,
		Gallery,
		NotFound
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
		async submitImage() {
			if (this.sent == true || this.file == null)
				return;
			let formData = new FormData();
			formData.append('file', this.file[0]);
			await api.createImage(formData);
			this.sent = true;
			(document.getElementById('fileUpload') as HTMLInputElement).value = "";
			this.updateImageList(100);
		},
		handleFilesUpload(event: InputFileEvent) {
			this.file = event.target.files;
			this.sent = false;
		},
		async updateImageList(sleep: number) {
			setTimeout(() => {
				api.getImageList().then((newImages) => {
					const newImageList = new Map<number, ImageType>();
					for (const image of newImages.values()) {
						newImageList.set(image.id, image);
					}
					this.images = newImageList; console.log(this.images);
				})
			}, sleep);
		},
		deleteFile(id: number) {
			api.deleteImage(id).then(() => {
				this.updateImageList(100);
			})
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
	<div v-for="route in routes" class="button">
		<a :href="'#' + (routes[route] || '/')"> {{ route }}</a>
	</div>
	<div>
		<label>Files
			<input id="fileUpload" type="file" @change="handleFilesUpload($event as InputFileEvent)" />
		</label>
		<button :class="sent ? 'green' : 'normal'" @click="submitImage()">{{ sent ? 'Sent' : 'Submit' }}</button>
	</div>
	<component :is="currentView" :key="sent" :images="images" @delete="deleteFile" />
</template>
<style scoped>
.button {
	display: inline-block;
	color: #444;
	border: 1px solid #CCC;
	background: #DDD;
	box-shadow: 0 0 5px -1px rgba(0, 0, 0, 0.2);
	cursor: pointer;
	vertical-align: middle;
	max-width: 100px;
	padding: 5px;
	text-align: center;
	margin: 5px;
}

.button:active {
	color: red;
	box-shadow: 0 0 5px -1px rgba(0, 0, 0, 0.6);
}

.green {
	color: green;
}
</style>
