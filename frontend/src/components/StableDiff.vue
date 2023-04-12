<template>
	<div>
		<div class="preview">
			<img :src="generatedImage" />
		</div>
		<div class="dl" v-if="generatedImage != '' && !started">
			<a :href="generatedImage" download="generated.png" class="button">Download</a>
			<button class="button" @click="sendImage()" :class="{ sent: sent }">{{ sent ? "Envoyé" : "Upload to server"
			}}</button>
		</div>
		<div class="form">
			<label for="promptImage">Prompt Image</label>
			<textarea v-model="promptImage" />
			<label for="negativePromptImage">Negative Prompt Image</label>
			<textarea v-model="negativePromptImage" />
			<div class="labelInput">
				<label for="height">Height</label>
				<input type="number" v-model.number="height" />
			</div>
			<input type="range" :min="minmaxHeight.min" :max="minmaxHeight.max" v-model.number="height" />
			<div class="labelInput">
				<label for="width">Width</label>
				<input type="number" v-model.number="width" />
			</div>
			<input type="range" :min="minmaxWidth.min" :max="minmaxWidth.max" v-model.number="width" />
			<div class="labelInput">
				<label for="steps">Steps</label>
				<input type="number" v-model.number="steps" />
			</div>
			<input type="range" :min="minmaxSteps.min" :max="minmaxSteps.max" v-model.number="steps" />
			<div class="labelInput">
				<label for="cfgScale">Cfg Scale</label>
				<input type="number" v-model.number="cfgScale" />
			</div>
			<input type="range" :min="minmaxCfgScale.min" :max="minmaxCfgScale.max" v-model.number="cfgScale" />
			<button :style="{ background: styledProgress() }" :disabled="started" class="button" @click="generate">{{
				started ? "ETA: " + eta + "s" : "Generate" }}</button>
		</div>
	</div>
</template>

<script setup lang="ts">
import { defineComponent } from 'vue';
import { api } from '@/http-api';
</script>
<script lang="ts">

const minmaxHeight = {
	min: 400,
	max: 512
}

const minmaxWidth = {
	min: 400,
	max: 512
}

const minmaxSteps = {
	min: 10,
	max: 40
}

const minmaxCfgScale = {
	min: 0,
	max: 30
}

export default defineComponent({
	emits: ['updateImageList'],
	data() {
		return {
			promptImage: '',
			negativePromptImage: '',
			height: 512,
			width: 512,
			steps: 20,
			cfgScale: 7,
			generatedImage: '',
			started: false,
			progress: 0,
			eta: 0,
			sent: false,
		}
	},
	methods: {
		async generate() {
			api.generateImage(this.promptImage, this.negativePromptImage, this.height, this.width, this.steps, this.cfgScale).then((data) => {
				this.generatedImage = "data:image/png;base64," + data.images[0];
				this.started = false;
			});
			this.sent = false;
			this.generatedImage = '';
			this.started = true;
		},
		urltoFile(url: string, filename: string, mimeType: string) {
			return (fetch(url)
				.then(function (res) { return res.arrayBuffer(); })
				.then(function (buf) { return new File([buf], filename, { type: mimeType }); })
			);
		},
		async sendImage() {
			if (!this.sent) {
				this.sent = true;
				const file = await this.urltoFile(this.generatedImage, "generated" + Date.now() + ".png", "image/png");
				const formData = new FormData();
				formData.append("file", file);
				api.createImage(formData).then((data) => {
					console.log(data);
					this.$emit('updateImageList');
				});
			}
		},
		styledProgress() {
			return "linear-gradient(to right, #00ff00 " + this.progress + "%, var(--button-color) " + this.progress + "%)";
		}
	},
	watch: {
		height() {
			if (this.height < minmaxHeight.min)
				this.height = minmaxHeight.min;
			else if (this.height > minmaxHeight.max)
				this.height = minmaxHeight.max;
		},
		width() {
			if (this.width < minmaxWidth.min)
				this.width = minmaxWidth.min;
			else if (this.width > minmaxWidth.max)
				this.width = minmaxWidth.max;
		},
		steps() {
			if (this.steps < minmaxSteps.min)
				this.steps = minmaxSteps.min;
			else if (this.steps > minmaxSteps.max)
				this.steps = minmaxSteps.max;
		},
		cfgScale() {
			if (this.cfgScale < minmaxCfgScale.min)
				this.cfgScale = minmaxCfgScale.min;
			else if (this.cfgScale > minmaxCfgScale.max)
				this.cfgScale = minmaxCfgScale.max;
		},
		started() {
			if (this.started) {
				const interval = setInterval(() => {
					api.generateProgress().then((data) => {
						this.progress = data.progress * 100;
						this.eta = Math.floor(data.eta_relative);
						if (data.current_image !== null) {
							this.generatedImage = "data:image/png;base64," + data.current_image;
						}
						if (data.state.job_count === 0) {
							clearInterval(interval);
						}
						if (!this.started) {
							clearInterval(interval);
						}
					});
				}, 1000);
			}
		},
	}

})
</script>
<style scoped>
.form {
	display: flex;
	flex-direction: column;
	align-items: center;
}

label {
	color: white;
}

.sent {
	color: green;
}

.form textarea {
	width: 100%;
	margin: 0;
	margin-bottom: 1vh;
	border-radius: 5px;
}

.form input {
	width: 100%;
	margin: 0;
	margin-bottom: 1vh;
}

a.button {
	text-decoration: none;
	height: 1.5em;
	width: min-content;
}

.dl {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	padding: 10px;
	gap: 1vw;
}

.labelInput {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
	width: 100%;
}

.labelInput input {
	width: 37px;
}

.preview {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.preview img {
	width: 100%;
}
</style>