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
	<div class="flex" v-if="images.size > 0">
		<div v-for="[id, image] in images" :key="id" class="image">
			<label>{{ image.name }}</label>
			<Image class="gallery" :id="id" :images="images" redirect="home" />
		</div>
	</div>
	<h1 v-else>Aucune image trouvée</h1>
</template>
<style scoped>
h1 {
	color: white;
}

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
	margin: 5px;
	padding: 0.5rem;
	background-color: #1a1a1a;
	color: white;
	border-radius: 8px;
	border: 1px solid transparent;
}

.image:hover {
	border-color: #646cff;
}

label {
	font-size: 1.5em;
	color: white;
}
</style>
<style>
.gallery.imageContainer {
	margin: 2vw;
	max-height: min(33vh, 33vw);
	max-width: min(33vw, 33vh);
	height: auto;
	width: auto;
}
</style>