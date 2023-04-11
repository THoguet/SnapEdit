<script setup lang="ts">
import Image from '@/components/Image.vue'
import HomeSelect from '@/components/HomeSelect.vue'
import HomeEditor from '@/components/HomeEditor.vue'
import { defineComponent } from 'vue'
import { ImageType } from '@/image'
import { Filter } from '@/filter'
import { api } from '@/http-api'
</script>
<script lang="ts">
export default defineComponent({
	emits: {
		delete(id: number) {
			return id >= 0;
		},
		updateImageList() {
			return true;
		}
	},
	props: {
		images: {
			type: Map<number, ImageType>,
			required: true,
		},
		filters: {
			type: Array as () => Filter[],
			required: true,
		},
	},
	data() {
		return {
			imageSelectedId: -1,
			editor: false,
			newId: -1,
			selectedArea: { xmin: 0, ymin: 0, xmax: 0, ymax: 0 },
			processed: false,
			progress: 0
		}
	},
	created() {
		if (this.$route.params.id !== "" && this.$route.params.id !== undefined) {
			this.imageSelectedId = parseInt(this.$route.params.id as string);
		}
		else
			this.updateImageSelectedId(-1);
	},
	watch: {
		images: {
			handler() {
				this.updateImageSelectedId(this.imageSelectedId);
			},
			deep: true
		},
		editor() {
			this.selectedArea = { xmin: 0, ymin: 0, xmax: 0, ymax: 0 };
		}
	},
	methods: {
		updateImageSelectedId(id: number) {
			if (this.newId !== -1) {
				id = this.newId;
				this.newId = -1;
			}
			if (this.images.size === 0)
				this.imageSelectedId = -1;
			else if (id !== -1 && this.images.has(id))
				this.imageSelectedId = id;
			else
				this.imageSelectedId = this.images.keys().next().value;
		},
		async applyFilter(filter: Filter) {
			if (filter === undefined || filter === null) {
				return;
			}
			console.log(filter);
			const newImage = api.applyAlgorithm(this.imageSelectedId, filter, this.selectedArea);
			newImage.then((id) => {
				this.processed = !this.processed;
				this.newId = parseInt(id);
				this.$emit("updateImageList");
				clearInterval(timer);
				this.progress = 0;
			});
			// set a timer to get the progress of the filter
			const timer = setInterval(async () => {
				const progress = (await api.algorithmsProgress()).progress;
				this.progress = progress;
			}, 50);
		},
		isImageFiltered(id: number) {
			if (id === undefined || id === -1)
				return -1;
			if (!this.images.has(id))
				return -1;
			return this.images.get(id)!.filtered;
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
			@updateImageSelectedId="id => updateImageSelectedId(id)" />
		<HomeEditor v-else @apply-filter="(filter) => applyFilter(filter)" :filters="filters"
			:image-filtered="isImageFiltered(imageSelectedId)" @delete-image="$emit('delete', imageSelectedId)"
			@update-image-selected-id="(newid) => updateImageSelectedId(newid)" :processed="processed"
			:progress="progress" />
		<Image class="home" v-if="imageSelectedId !== undefined && imageSelectedId !== -1" :id="imageSelectedId"
			:images="images" :redirect="editor ? 'disabled' : ''" :selected-area="selectedArea">
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
	margin-top: 60px;
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
	width: min(60vh, 60vw);
	height: min(60vh, 60vw);
	transform: none;
}
</style>
