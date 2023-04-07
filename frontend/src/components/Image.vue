<script setup lang="ts">
import { defineComponent } from 'vue';
import { api } from '@/http-api';
import { ImageType } from '@/image';
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
			type: Object as () => { xmin: number, ymin: number, xmax: number, ymax: number },
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
			if (!force && (this.images.has(id) && this.images.get(id)!.data != null))
				this.data = this.images.get(id)!.data;
			else
				api.getImage(id).then((imageData) => {
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
				this.image.src = this.data;
				this.image.onload = () => {
					if (this.image === null) return;
					this.canvas!.width = this.image.naturalWidth;
					this.canvas!.height = this.image.naturalHeight;
					ctx.drawImage(this.image, 0, 0, this.image.naturalWidth, this.image.naturalHeight);
				}
			} else {
				ctx.drawImage(this.image, 0, 0, this.image.naturalWidth, this.image.naturalHeight);
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
			const ratio = this.image!.naturalWidth / this.canvas!.clientWidth;
			if (this.canvas === null) return;
			const ctx = this.canvas.getContext('2d');
			if (ctx === null) return;
			const rect = this.canvas.getBoundingClientRect();
			const x = event.clientX - rect.left;
			const y = event.clientY - rect.top;
			if (event.type === "mousedown") {
				this.selectedArea.xmin = x * ratio;
				this.selectedArea.ymin = y * ratio;
				this.selectedArea.xmax = x * ratio;
				this.selectedArea.ymax = y * ratio;
				this.mouseDowned = true;
			}
			else if (this.mouseDowned && event.type === "mousemove") {
				this.selectedArea.xmax = x * ratio;
				this.selectedArea.ymax = y * ratio;
			}
			else if (event.type === "mouseup") {
				this.mouseDowned = false;
				this.selectedArea.xmax = x * ratio;
				this.selectedArea.ymax = y * ratio;
				if (this.selectedArea.xmin > this.selectedArea.xmax) {
					const tmp = this.selectedArea.xmin;
					this.selectedArea.xmin = this.selectedArea.xmax;
					this.selectedArea.xmax = tmp;
				}
				if (this.selectedArea.ymin > this.selectedArea.ymax) {
					const tmp = this.selectedArea.ymin;
					this.selectedArea.ymin = this.selectedArea.ymax;
					this.selectedArea.ymax = tmp;
				}
			}
			this.drawImage();
			ctx.fillStyle = "rgba(0, 0, 50, 0.5)";
			ctx.fillRect(this.selectedArea.xmin, this.selectedArea.ymin, this.selectedArea.xmax - this.selectedArea.xmin, this.selectedArea.ymax - this.selectedArea.ymin);
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

.imageContainer:hover {
	transform: scale(1.1);
	transition: transform 0.5s;
}
</style>