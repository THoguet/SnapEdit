<script setup lang="ts">
import { defineComponent } from 'vue'
import { ImageType } from '@/image'
import Image from '@/components/Image.vue'
</script>
<script lang="ts">

export default defineComponent({
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true
		}
	}
})
</script>

<template>
	<div class="flex" v-if="images.keys !== null">
		<div v-for="[id, image] in images" :key="id" class="image">
			<label>{{ image.name }}</label>
			<Image class="gallery" :id="id" />
		</div>
	</div>
	<h1 v-else>No image found</h1>
</template>
<style scoped>
.flex {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
}

.image {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

label {
	font-size: 1.5em;
	color: white;
}
</style>
<style>
.gallery>img {
	margin: 5px;
	max-width: max(33vw, calc(90vw/v-bind('images.size')));
	max-height: max(33vh, calc(90vh/v-bind('images.size')));
	margin: 2vw;
}
</style>