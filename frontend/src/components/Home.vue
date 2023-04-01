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
		'images.size': {
			handler() { this.updateImageSelectedId(); },
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
		}
	}
})
</script>
<template>
	<div class="flex" v-if="images.size > 0">
		<div class="navi">
			<nav>
				<ul>
					<li>
						<h3 :class="{ active: !editor }" @click="editor = false">Mode sélection</h3>
					</li>
					<li>
						<h3 :class="{ active: editor }" @click="editor = true">Mode éditeur</h3>
					</li>
				</ul>
			</nav>
		</div>
		<HomeSelect v-if="!editor" :images="images" :image-selected-id="imageSelectedId" @delete="id => $emit('delete', id)"
			@updateImageSelectedId="id => updateImageSelectedId(id)"></HomeSelect>
		<HomeEditor v-else @apply-filter="(newFilter) => filter = newFilter"></HomeEditor>
		<Image class="home" v-if="imageSelectedId !== undefined && imageSelectedId !== -1" :id="imageSelectedId"
			:images="images" :filter="filter">
		</Image>
		<a class="button" :href="images.get(imageSelectedId)?.data"
			:download="images.get(imageSelectedId)?.name">Télécharger {{ images.get(imageSelectedId)?.name }}</a>
	</div>
	<h1 v-else>Aucune image trouvée</h1>
</template>
<style scoped>
@import url("@/navi.css");

h1 {
	color: white;
	margin: 0;
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
	gap: 4vh;
}

.active {
	background-color: #646cff;
}

.active a {
	color: rgb(205, 201, 194);
}

.navi h3 {
	cursor: pointer;
}

:deep(.imageContainer) {
	max-width: min(60vh, 60vw);
	max-height: min(60vh, 60vw);
	transform: none;
}
</style>
