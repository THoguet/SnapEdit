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
		if (this.$route.params.id)
			this.imageSelectedId = parseInt(this.$route.params.id as string);
		else
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
	<div class="flex" v-if="images.keys != null">
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
/* SELECT */

.custom-select {
	width: 100%;
	text-align: left;
	outline: none;
	height: 47px;
	line-height: 47px;
}

.custom-select .selected {
	display: flex;
	justify-content: space-between;
	align-items: center;
	background-color: #1a1a1a;
	border-radius: 6px;
	/* border: 1px solid #666666; */
	color: #fff;
	padding-left: 1em;
	cursor: pointer;
	user-select: none;
	border: 1px solid transparent;
}

.custom-select .selected:hover {
	border-color: #646cff;
}

.custom-select span {
	padding-right: 1rem;
}

.custom-select .selected.open {
	border: 1px solid #646cff;
	border-radius: 6px 6px 0px 0px;
}

.custom-select .selected:after {
	content: "";
	margin-right: 15px;
	margin-top: 5px;
	border: 5px solid transparent;
	border-color: #fff transparent transparent transparent;
}

.custom-select .items {
	color: #fff;
	border-radius: 0px 0px 6px 6px;
	overflow: hidden;
	border-right: 1px solid #646cff;
	border-left: 1px solid #646cff;
	border-bottom: 1px solid #646cff;
	background-color: #1a1a1a;
}

.custom-select .items div {
	color: #fff;
	padding-left: 1em;
	cursor: pointer;
	user-select: none;
}

.custom-select .items div:hover {
	background-color: #646cff;
}

.selectHide {
	display: none;
}

/* FIN SELECT */

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
	max-height: 60vh;
	margin: 2vw;
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
