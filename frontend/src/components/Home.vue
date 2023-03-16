<script setup lang="ts">
import Image from '@/components/Image.vue'
import HomeSelect from '@/components/HomeSelect.vue'
import HomeEditor from '@/components/HomeEditor.vue'
import { defineComponent } from 'vue'
import { ImageClass, ImageType } from '@/image'
import { Filter } from '@/filter'
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
			timer: null as ReturnType<typeof setTimeout> | null,
			editor: false,
			filter: undefined as Filter | undefined,
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
	},
	methods: {
		updateImageSelectedId(id: number = -1) {
			if (this.images.size === 0)
				this.imageSelectedId = -1;
			else if (id !== -1 && this.images.has(id))
				this.imageSelectedId = id;
			else
				this.imageSelectedId = this.images.keys().next().value;
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
		<h3 v-if="!editor">Restez sur l'image pour passer en mode éditeur</h3>
		<h3 v-else>Restez sur l'image pour passer en mode selection</h3>
		<a class="button" :href="images.get(imageSelectedId)?.data"
			:download="images.get(imageSelectedId)?.name">Télécharger {{ images.get(imageSelectedId)?.name }}</a>
		<Image @mouseleave="clearTimer()" @mouseenter="startTimer()" class="home"
			v-if="imageSelectedId !== undefined && imageSelectedId !== -1" :id="imageSelectedId" :images="images"
			:filter="filter">
		</Image>
		<HomeSelect v-if="!editor" :images="images" :image-selected-id="imageSelectedId" @delete="id => $emit('delete', id)"
			@updateImageSelectedId="id => updateImageSelectedId(id)"></HomeSelect>
		<HomeEditor v-else @apply-filter="(newFilter) => filter = newFilter"></HomeEditor>
	</div>
	<h1 v-else>Aucune image trouvée</h1>
</template>
<style scoped>
h1 {
	color: white;
}

h3 {
	color: white;
}

a {
	text-decoration: none;
}

html {
	overflow: hidden;
}


.flex {
	display: flex;
	flex-direction: column;
	align-items: center;
}
</style>
<style>
.home.imageContainer {
	max-width: 60%;
	width: auto;
	height: auto;
	max-height: 60vh;
	margin: 2vw 0;
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
