<script setup lang="ts">
import { defineComponent } from 'vue';
import { api } from '@/http-api';
import { ImageType } from '@/image';
import { Filter } from '@/filter';
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
			default: { xmin: 0, ymin: 0, xmax: 0, ymax: 0 }
		}
	},
	data() {
		return {
			data: "" as string,
			canvas: null as HTMLCanvasElement | null
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
			const image = new Image();
			image.src = this.data;
			image.onload = () => {
				this.canvas!.width = image.naturalWidth;
				this.canvas!.height = image.naturalHeight;
				ctx.drawImage(image, 0, 0, image.naturalWidth, image.naturalHeight);
			}
		}
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
		filter() {
			this.getImage(this.id, true);
		}
	},
	mounted() {
		this.canvas = document.getElementById('canvas-' + this.id) as HTMLCanvasElement;
		this.drawImage();
	},
})
</script>
<template>
	<div class="imageContainer">
		<div v-if="redirect === 'disabled'">
			<canvas :id="'canvas-' + id" />
		</div>
		<RouterLink v-else-if="redirect" :to="{ name: redirect, params: { id: id } }">
			<canvas :id="'canvas-' + id" />
		</RouterLink>
		<a v-else :href="this.data" target="_blank">
			<canvas :id="'canvas-' + id" />
		</a>
	</div>
</template>

<style>
.imageContainer canvas {
	max-width: 100%;
	max-height: 100%;
	object-fit: contain;
}

.imageContainer:hover {
	transform: scale(1.1);
	transition: transform 0.5s;
}
</style>