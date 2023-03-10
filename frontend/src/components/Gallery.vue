<template>
	<div class="flex" v-if="images.keys != null">
		<Image v-for="[id, image] in images" :key="id" :id="id" />
	</div>
	<h1 v-else>No image found</h1>
</template>
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

<style scoped>
.flex {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: center;
	flex-wrap: wrap;
}

.flex>img {
	margin: 5px;
}

img {
	max-width: 25%;
	max-height: 25%;
}
</style>