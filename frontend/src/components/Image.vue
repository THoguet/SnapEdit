<script setup lang="ts">
import { defineComponent } from 'vue';
import { api } from '@/http-api';
import { ImageType } from '@/image';
import { Area } from '@/filter';
</script>
<script lang="ts">
export default defineComponent({
	props: {
		id: {
			type: Number,
			required: true
		},
		images: {
			type: Map<number, ImageType>,
			required: true
		},
		redirect: {
			type: String
		},
		selectedArea: {
			type: Area,
		}
	},
	data() {
		return {
			data: "" as string,
			canvas: null as HTMLCanvasElement | null,
			mouseDowned: false,
			image: null as HTMLImageElement | null,
		}
	},
	methods: {
		getImage(id: number, force: boolean = false) {
			if (!force && (this.images.has(id) && this.images.get(id)!.data != null)) {
				this.data = this.images.get(id)!.data;
			}
			else
				api.getImage(this.images.get(id)!.id).then((imageData) => {
					const reader = new window.FileReader();
					reader.readAsDataURL(imageData);
					reader.onload = () => {
						this.data = reader.result as string;
						this.images.get(id)!.data = this.data;
					};
				});
		},
		drawImage() {
			if (this.data === "") return;
			if (this.canvas === null) return;
			const ctx = this.canvas.getContext('2d');
			if (ctx === null) return;
			if (this.image === null) {
				this.image = new Image();
			}
			this.image.src = this.data;
			this.image.onload = () => {
				if (this.image === null) return;
				this.canvas!.width = this.image.naturalWidth;
				this.canvas!.height = this.image.naturalHeight;
				ctx.drawImage(this.image, 0, 0, this.image.naturalWidth, this.image.naturalHeight);
				if (this.selectedArea === undefined) return;
				ctx.fillStyle = "rgba(0, 0, 50, 0.5)";
				ctx.fillRect(this.selectedArea.xMin, this.selectedArea.yMin, this.selectedArea.xMax - this.selectedArea.xMin, this.selectedArea.yMax - this.selectedArea.yMin);
			}
		},
		handleMouse(event: MouseEvent) {
			if (this.redirect === undefined || this.redirect === '') {
				if (event.type === "mousedown" || event.type === "mouseup") {
					event.preventDefault();
					var newTab = window.open("images/" + this.id, '_blank');
					if (!newTab) return;
					newTab.focus();
					return;
				}
			}
			else if (this.redirect !== "disabled") {
				if (event.type === "mousedown" || event.type === "mouseup") {
					event.preventDefault();
					this.$router.push({ name: this.redirect, params: { id: this.id } });
					return;
				}
			}
			if (this.selectedArea === undefined) return;
			if (this.canvas === null) return;
			const ratio = this.image!.naturalWidth / this.canvas!.clientWidth;
			const rect = this.canvas.getBoundingClientRect();
			const x = event.clientX - rect.left;
			const y = event.clientY - rect.top;
			if (event.type === "mousedown") {
				this.selectedArea.xMin = x * ratio;
				this.selectedArea.yMin = y * ratio;
				this.selectedArea.xMax = x * ratio;
				this.selectedArea.yMax = y * ratio;
				this.mouseDowned = true;
				this.drawImage();
			}
			else if (this.mouseDowned && event.type === "mousemove") {
				this.selectedArea.xMax = x * ratio;
				this.selectedArea.yMax = y * ratio;
				this.drawImage();
			}
			else if (event.type === "mouseup") {
				this.mouseDowned = false;
				this.selectedArea.xMax = x * ratio;
				this.selectedArea.yMax = y * ratio;
				if (this.selectedArea.xMin > this.selectedArea.xMax) {
					const tmp = this.selectedArea.xMin;
					this.selectedArea.xMin = this.selectedArea.xMax;
					this.selectedArea.xMax = tmp;
				}
				if (this.selectedArea.yMin > this.selectedArea.yMax) {
					const tmp = this.selectedArea.yMin;
					this.selectedArea.yMin = this.selectedArea.yMax;
					this.selectedArea.yMax = tmp;
				}
				this.drawImage();
			}
		},
	},
	created() {
		this.getImage(this.id);
	},
	watch: {
		id() {
			this.getImage(this.id)
		},
		data() {
			this.drawImage();
		},
		selectedArea() {
			this.drawImage();
		},
	},
	mounted() {
		this.canvas = document.getElementById('canvas-' + this.id) as HTMLCanvasElement;
		this.drawImage();
	},
})
</script>
<template>
	<div class="imageContainer">
		<canvas :id="'canvas-' + id" redirect="disabled" @mousedown="handleMouse($event)" @mouseup="handleMouse($event)"
			@mousemove="handleMouse($event)" />
	</div>
</template>

<style>
.imageContainer canvas {
	max-width: 100%;
	max-height: 100%;
	object-fit: contain;
	cursor: v-bind("redirect === 'disabled' ? 'crosshair' : 'pointer'");
}

.imageContainer {
	display: flex;
	justify-content: center;
	align-items: center;
}

.imageContainer:hover {
	transform: scale(1.1);
	transition: transform 0.5s;
}
</style>