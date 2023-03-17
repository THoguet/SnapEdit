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
		filter: {
			type: Filter
		}
	},
	data() {
		return {
			data: "" as string,
		}
	},
	methods: {
		getImage(id: number) {
			if (this.images.has(id) && this.images.get(id)!.data != null)
				this.data = this.images.get(id)!.data;
			else
				this.downloadImage(id);
		},
		async downloadImage(id: number) {
			const data = await api.getImage(id, this.filter);
			const reader = new window.FileReader();
			reader.readAsDataURL(data);
			reader.onload = () => {
				this.data = (reader.result as string)
				this.images.get(id)!.data = this.data;
			};
		}
	},
	created() {
		this.getImage(this.id);
	},
	watch: {
		id() {
			this.getImage(this.id)
		},
		filter: {
			handler() {
				this.downloadImage(this.id);
			}
		}
	}
})
</script>
<template>
	<div class="imageContainer">
		<RouterLink v-if="redirect" :to="{ name: redirect, params: { id: id } }">
			<img :id="'img-' + id" :src="data" />
		</RouterLink>
		<a v-else :href="'./images/' + id" target="_blank">
			<img :id="'img-' + id" :src="data" />
		</a>
	</div>
</template>

<style>
.imageContainer img {
	max-width: 100%;
	max-height: 100vh;
	height: auto;
	width: auto;
	object-fit: contain;
}

.imageContainer:hover {
	transform: scale(1.1);
	transition: transform 0.5s;
}
</style>