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
			imageSelectedId: 0 as number,
			sure: false as boolean
		}
	},
	mounted() {
		this.imageSelectedId = this.images.keys().next().value;
	},
	watch: {
		images() {
			this.imageSelectedId = this.images.keys().next().value;
		},
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
		}
	}
})
</script>
<template>
	<div class="flex" v-if="images.keys != null">
		<div>
			<label>Selectioner une image: </label>
			<select v-model="imageSelectedId" style="width: min-content;margin: 15px;">
				<option v-for="[id, image] in images" :key="id" :value="id">{{ image.name }}</option>
			</select>
			<button @mouseleave="sure = false" @click="confirmDelete()" :class="sure ? 'confirm' : ''">
				{{ sure ? "Confirmer" : "Supprimer" }}</button>
		</div>
		<Image class="home" v-if="imageSelectedId !== undefined" :id="imageSelectedId"></Image>
	</div>
	<h1 v-else>Aucune image trouvée</h1>
</template>
<style scoped>
.flex {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.confirm {
	background-color: red;
	color: white;
}

label {
	color: white;
}
</style>
<style>
.home img {
	max-width: 60%;
	max-height: 60vh;
}
</style>
