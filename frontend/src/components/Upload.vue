
<script setup lang="ts">

import { defineComponent } from 'vue';
import { api } from '@/http-api';
import { ImageType, ImageClass } from '@/image';

</script>
<script lang="ts">

interface InputFileEvent extends Event {
	target: HTMLInputElement;
}

export default defineComponent({
	emits: ['updateImageList', 'delete'],
	data() {
		return {
			sent: false,
			file: null as FileList | null
		}
	},
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true
		}
	},
	methods: {
		async submitImage() {
			if (this.sent === true || this.file === null)
				return;
			let formData = new FormData();
			formData.append('file', this.file[0]);
			await api.createImage(formData);
			this.sent = true;
			this.$emit('updateImageList');
			this.resetFile();
		},
		handleFilesUpload(event: InputFileEvent) {
			this.file = event.target.files;
			this.sent = false;
		},
		resetFile() {
			this.file = null;
			(document.getElementById('fileUpload') as HTMLInputElement).value = "";
		}
	},
	watch: {
		file() {
			if (this.file === null)
				return;
			Array.from(this.file).forEach((file, id) => {
				const reader = new window.FileReader();
				reader.readAsDataURL(file);
				reader.onload = () => {
					const imgElt = document.getElementById("preview-" + id) as HTMLImageElement;
					if (imgElt !== null) {
						if (reader.result as string) {
							imgElt.setAttribute("src", (reader.result as string));
						}
					}
				};
			});
		}
	}
})

</script>
<template>
	<div>
		<label>Files
			<input id="fileUpload" type="file" @change="handleFilesUpload($event as InputFileEvent)" accept="image/jpeg"
				multiple />
		</label>
		<button :class="sent ? 'green' : 'normal'" @click="submitImage()">{{ sent ? 'Sent' : 'Submit' }}</button>
		<button v-if="file !== null" @click="resetFile()">Reset</button>
	</div>
	<h2 v-if="file !== null">Preview :</h2>
	<div class="previewBox">
		<label v-for="(f, id) in (file as FileList)" :key="id" class="imageLabel">{{ (f as File).name }}:
			<img :id="'preview-' + id" />
		</label>
	</div>
</template>
<style scoped>
img {
	max-width: 100%;
	max-height: 90%;
	border: 1px solid black;
}

.imageLabel {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	max-width: 33%;
	height: 33vh;
	margin: 5px;
}

.previewBox {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-evenly;
	flex-wrap: wrap;
}
</style>