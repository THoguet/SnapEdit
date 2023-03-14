
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
			file: null as FileList | null,
			titleSendButton: "Envoyer",
			classSendButton: ""
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
			if (this.classSendButton === "sent") {
				this.titleSendButton = "Images déjà envoyées"
				this.classSendButton = "error"
				return;
			}
			if (this.file === null || this.file.length === 0) {
				this.titleSendButton = "Aucune image sélectionnée"
				this.classSendButton = "error"
				return;
			}
			Array.from(this.file).forEach((file, id) => {

				let formData = new FormData();
				formData.append('file', file);
				api.createImage(formData);
			});
			this.$emit('updateImageList');
			this.resetFile();
			this.titleSendButton = "Envoyé"
			this.classSendButton = "sent"
		},
		handleFilesUpload(event: InputFileEvent) {
			this.file = event.target.files;
			this.resetSendButton(true)

		},
		resetFile() {
			this.file = null;
			(document.getElementById('fileUpload') as HTMLInputElement).value = "";
		},
		nameLabelInput() {
			if (this.file === null || this.file.length === 0)
				return "Aucune image sélectionnée";
			if (this.file.length === 1)
				return this.file[0].name;
			return this.file.length + " images sélectionnées";
		},
		resetSendButton(force: boolean = false) {
			if (force || this.classSendButton === "error") {
				this.titleSendButton = "Envoyer"
				this.classSendButton = ""
			}
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
	<div class="form">
		<div class="inputform" for="fileUpload">
			<input style="position: absolute; left: -9999px; opacity: 0;" class="fileUpload" id="fileUpload" type="file"
				@change="handleFilesUpload($event as InputFileEvent)" accept="image/png, image/jpeg" multiple />
			<label class="button" for="fileUpload" id="labelInput">Choisir une ou plusieurs image(s)...</label>
			<label class="filename button" for="fileUpload">{{ nameLabelInput() }}</label>
		</div>
		<button class="button" :class="classSendButton" @click="submitImage()" @mouseleave="resetSendButton()">{{
			titleSendButton }}</button>
		<button class="button" v-if="file !== null && file.length !== 0" @click="resetFile()">Reinitialiser</button>
	</div>
	<h2 v-if="file !== null && file.length !== 0">Preview :</h2>
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
}

.form {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-evenly;
	gap: 20px;
}

label {
	color: white;
}


h2 {
	color: white;
}

.fileUpload:focus~label,
.fileUpload:focus-visible~label {
	outline: 4px auto -webkit-focus-ring-color;
}

.filename {
	background-color: white;
	color: black;
	border: 1px solid rgb(255, 0, 98);
	border-radius: 0 8px 8px 0;
}

#labelInput {
	border-radius: 8px 0 0 8px;
	background-color: rgb(255, 0, 98);
}

.inputform:hover label {
	border-color: #646cff;
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

.error {
	color: red;
	font-weight: bold;
}

.sent {
	color: green;
	font-weight: bold;
}
</style>