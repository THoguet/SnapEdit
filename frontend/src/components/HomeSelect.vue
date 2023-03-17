<script setup lang="ts">

import { defineComponent } from 'vue'
import { ImageType } from '@/image'

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
			open: false,
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
	}

})

</script>

<template>
	<div class="selector">
		<label>Selectioner une image: </label>
		<div class="custom-select" @blur="open = false">
			<div class="selected" :class="{ open: open }" @click="open = !open">
				<span>{{ images.get(imageSelectedId)?.name }}</span>
			</div>
			<div class="items" :class="{ selectHide: !open }">
				<div v-for="[id, image] of images" :key="id"
					@click="$emit('updateImageSelectedId', image.id); open = false;">
					<span>{{ image.name }}</span>
				</div>
			</div>
		</div>
		<button class="button" @mouseleave="sure = false" @click="confirmDelete()" :class="{ confirm: sure }">
			{{ sure ? "Confirmer" : "Supprimer" }}</button>
	</div>
</template>

<style scoped>
@import url(@/customSelect.css);

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