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
			required: true,
		}
	},
	data() {
		return {
			imageSelectedId: -1,
			sure: false,
			timer: null as ReturnType<typeof setTimeout> | null,
			open: false,
			editor: false
		}
	},
	created() {
		if (this.$route.params.id !== "" && this.$route.params.id !== undefined) {
			this.imageSelectedId = parseInt(this.$route.params.id as string);
		}
		else
			this.updateImageSelectedId();
	},
	watch: {
		images() {
			this.updateImageSelectedId();
		},
		imageSelectedId() {
			this.sure = false;
		}
	},
	methods: {
		updateImageSelectedId() {
			if (this.images.size === 0)
				this.imageSelectedId = -1;
			else
				this.imageSelectedId = this.images.keys().next().value;
		},
		confirmDelete() {
			if (this.sure)
				this.$emit('delete', this.imageSelectedId);
			else
				this.sure = true;
		},
		startTimer() {
			if (this.timer !== null)
				clearTimeout(this.timer);
			this.timer = setTimeout(() => {
				this.editor = !this.editor;
			}, 1900);
		},
		clearTimer() {
			if (this.timer !== null) {
				clearTimeout(this.timer);
				this.timer = null;
			}
		}
	}
})
</script>
<template>
	<div class="flex" v-if="images.size > 0">
		<Image @mouseleave="clearTimer()" @mouseenter="startTimer()" class="home"
			v-if="imageSelectedId !== undefined && imageSelectedId !== -1" :id="imageSelectedId" :images="images">
		</Image>
		<div v-if="!editor" class="selector">
			<label>Selectioner une image: </label>
			<div class="custom-select" :tabindex="imageSelectedId" @blur="open = false">
				<div class="selected" :class="{ open: open }" @click="open = !open">
					<span>{{ images.get(imageSelectedId)?.name }}</span>
				</div>
				<div class="items" :class="{ selectHide: !open }">
					<div v-for="[id, image] of images" :key="id" @click="imageSelectedId = image.id; open = false;">
						<span>{{ image.name }}</span>
					</div>
				</div>
			</div>
			<button class="button" @mouseleave="sure = false" @click="confirmDelete()" :class="{ confirm: sure }">
				{{ sure ? "Confirmer" : "Supprimer" }}</button>
			<a class="button" :href="images.get(imageSelectedId)?.data"
				:download="images.get(imageSelectedId)?.name">Télécharger</a>
		</div>
		<div v-else>

		</div>
	</div>
	<h1 v-else>Aucune image trouvée</h1>
</template>
<style scoped>
@import url(@/customSelect.css);

h1 {
	color: white;
}

a {
	text-decoration: none;
}

html {
	overflow: hidden;
}

.selector {
	margin-bottom: 5%;
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
.home.imageContainer {
	max-width: 60%;
	width: auto;
	height: auto;
	max-height: 60vh;
	margin: 0 0 2vw 0;
}

div.home.imageContainer:hover {
	animation: zoom-in-zoom-out 2s cubic-bezier(1, 0, 0.51, 1.01);
	transform: none;
}

@keyframes zoom-in-zoom-out {
	0% {
		transform: scale(1);
	}

	60% {
		transform: scale(1.5);
	}

	75% {
		transform: scale(10);
	}

	100% {
		transform: none;
	}
}
</style>
