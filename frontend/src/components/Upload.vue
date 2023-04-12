
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
	emits: ['updateImageList'],
	data() {
		return {
			file: null as FileList | null,
			titleSendButton: "Envoyer",
			classSendButton: "",
			requestSent: 0,
			requestEnded: 0,
			requestError: 0
		}
	},
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true
		},
		fileDefault: {
			type: FileList,
			default: null
		}
	},
	created() {
		this.file = this.fileDefault;
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
			if (this.requestError > 0) {
				this.titleSendButton = "Erreur lors de l'envoi"
				this.classSendButton = "error"
				this.requestError = 0;
				return;
			}
			this.requestEnded = 0;
			this.requestSent = 0;
			this.titleSendButton = "Envoi en cours (" + this.requestEnded + "/" + this.file.length + ")";
			Array.from(this.file).forEach((file, id) => {
				let formData = new FormData();
				formData.append('file', file);
				this.requestSent++;
				const resp = api.createImage(formData);
				resp.then(() => { this.requestEnded++; });
				resp.catch(() => { this.requestError++; });
			});
			this.resetFile();
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
		},
		requestEnded() {
			if (this.requestEnded === this.requestSent) {
				if (this.requestEnded > 1)
					this.titleSendButton = "Images envoyées"
				else
					this.titleSendButton = "Image envoyée"
				this.classSendButton = "sent"
				this.$emit('updateImageList');
			} else
				this.titleSendButton = "Envoi en cours (" + this.requestEnded + "/" + this.requestSent + ")";
		},
		requestError() {
			if (this.requestError > 0) {
				this.titleSendButton = "Erreur lors de l'envoi"
				this.classSendButton = "error"
			}
		}
	}
})

</script>
<template>
	<div class="form">
		<div class="inputform" for="fileUpload">
			<input style="position: absolute; left: -9999px; opacity: 0;" class="fileUpload" id="fileUpload" type="file"
				@change="handleFilesUpload($event as InputFileEvent)" accept="image/png, image/jpeg" multiple />
			<label class="button" for="fileUpload" id="labelInput"><span>Choisir une ou plusieurs image(s)...</span></label>
			<label class="filename button" for="fileUpload">{{ nameLabelInput() }}</label>
		</div>
		<button class="button" :class="classSendButton" @click="submitImage()" @mouseleave="resetSendButton()">{{
			titleSendButton }}</button>
		<button class="button" v-if="file !== null && file.length !== 0" @click="resetFile()">Réinitialiser</button>
	</div>
	<h2 v-if="file !== null && file.length !== 0">Preview :</h2>
	<div class="previewBox">
		<div class="imagePreview" v-for="(f, id) in (file as FileList)" :key="id">
			<label>{{ (f as File).name }}</label>
			<img :id="'preview-' + id" />
		</div>
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
	flex-wrap: wrap;
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
	border: 1px solid #1a1a1a;
	border-radius: 0 8px 8px 0;
	/* white-space: nowrap; */
}

#labelInput {
	border-radius: 8px 0 0 8px;
	background-color: #1a1a1a;
}

.inputform {
	flex-shrink: 0;
	display: flex;
	height: 47px;
}

.inputform label {
	height: 45px;
	padding-top: 0;
	padding-bottom: 0;
}

.inputform:hover label {
	border-color: #646cff;
}

.previewBox {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-evenly;
	flex-wrap: wrap;
	white-space: nowrap;
}

.imagePreview {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	max-width: 33%;
	height: 33vh;
	margin: 5px;
	padding: 0.5rem;
	background-color: #1a1a1a;
	color: white;
	border-radius: 8px;
	border: 1px solid transparent;
}

.imagePreview:hover {
	border-color: #646cff;
}

.error {
	color: red;
	font-weight: bold;
}

.sent {
	color: green;
	font-weight: bold;
}

@media screen and (max-width: 600px) {
	.filename {
		display: none;
	}

	#labelInput {
		border-radius: 8px;
	}

}
</style>