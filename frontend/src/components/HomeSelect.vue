<script setup lang="ts">

import { defineComponent } from 'vue'
import { ImageType } from '@/image'
import CustomSelector from './CustomSelector.vue';
</script>
<script lang="ts">

export default defineComponent({
	emits: {
		delete(id: number) {
			return id >= 0;
		},
		updateImageSelectedId(id: number) {
			return id >= 0;
		}
	},
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true,
		},
		imageSelectedId: {
			type: Number,
			required: true,
		},
	},
	data() {
		return {
			sure: false,
		}
	},
	watch: {
		imageSelectedId() {
			this.sure = false;
		}
	},
	methods: {
		confirmDelete() {
			if (this.sure)
				this.$emit('delete', this.imageSelectedId);
			else
				this.sure = true;
		},
		convertMapImage() {
			const map = new Map<number, string>();
			this.images.forEach((value, key) => {
				map.set(key, value.name);
			});
			return map;
		}
	}

})

</script>

<template>
	<div class="selector">
		<label>Sélectionner une image: </label>
		<CustomSelector :selected="imageSelectedId" @update-selected="$emit('updateImageSelectedId', $event)"
			:map="convertMapImage()" />
		<button class="button" @mouseleave="sure = false" @click="confirmDelete()" :class="{ confirm: sure }">
			{{ sure ? "Confirmer" : "Supprimer" }}</button>
	</div>
</template>

<style scoped>
.selector {
	display: flex;
	flex-direction: row;
	align-content: center;
	justify-content: center;
	gap: 1em;
}

.selector label {
	display: flex;
	align-items: center;
	flex-shrink: 0;
}

.confirm {
	background-color: red;
	color: white;
}

label {
	color: white;
}
</style>