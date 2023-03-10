<script setup lang="ts">
import Image from '@/components/Image.vue'
import { defineComponent } from 'vue'
import { ImageType } from '@/image'
</script>
<script lang="ts">
export default defineComponent({
	emits: {
		delete(id: number) {
			return id >= 0;
		}
	},
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true
		}
	},
	data() {
		return {
			imageSelectedId: 0 as number
		}
	},
	mounted() {
		this.imageSelectedId = this.images.keys().next().value;
	},
	watch: {
		images() {
			this.imageSelectedId = this.images.keys().next().value;
		}
	}
})
</script>
<template>
	<div class="flex" v-if="images.keys != null">
		<div>
			<select v-model="imageSelectedId" style="width: min-content;margin: 15px;">
				<option v-for="[id, image] in images" :key="id" :value="id">{{ image.name }}</option>
			</select>
			<button @click="$emit('delete', imageSelectedId)">Delete</button>
		</div>
		<Image v-if="imageSelectedId !== undefined" :id="imageSelectedId"></Image>
	</div>
	<h1 v-else>No images found</h1>
</template>
<style scoped>
.flex {
	display: flex;
	flex-direction: column;
	align-items: center;
}

img {
	max-width: 90%;
	max-height: 60%;
}
</style>
