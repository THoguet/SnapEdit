<script setup lang="ts">

import { defineComponent } from 'vue'
import { Filter, Arg } from '@/filter'

</script>
<script lang="ts">
export default defineComponent({
	emits: {
		applyFilter(filter: Filter | undefined) {
			return filter !== null;
		}
	},
	data() {
		return {
			filterSelectId: 0,
			open: false,
			filters: [
				new Filter("Luminosité", "changeLuminosity", [new Arg("delta", -255, 255)]),
				new Filter("Egalisation d'histogramme", "histogram", []),
				new Filter("Filtre de couleur", "colorFilter", [new Arg("hue", 0, 359)]),
				new Filter("Filter moyenneur", "meanFilter", [new Arg("size", 1, 100, 2)]),
				new Filter("Filtre gaussien", "gaussianFilter", [new Arg("size", 1, 100, 2)]),
				new Filter("Filtre de contours", "contours", []),
			]
		}
	},
	methods: {
		applyFilter() {
			if (this.filterSelectId === 0) {
				this.$emit("applyFilter", undefined);
				return;
			}
			const filter = this.filters[this.filterSelectId - 1];
			filter.updated = true;
			this.$emit("applyFilter", filter);
		}
	}
})

</script>
<template>
	<div class="editor">
		<label>Selectioner un filtre: </label>
		<div class="custom-select" @blur="open = false">
			<div class="selected" :class="{ open: open }" @click="open = !open">
				<span>{{ filterSelectId === 0 ? "Aucun filtre" : filters[filterSelectId - 1].name }}</span>
			</div>
			<div class="items" :class="{ selectHide: !open }">
				<div @click="open = false; filterSelectId = 0">
					<span>Aucun filtre</span>
				</div>
				<div v-for="(filter, index) in filters" @click="open = false; filterSelectId = index + 1">
					<span>{{ filter.name }}</span>
				</div>
			</div>
		</div>
		<div v-if="filterSelectId !== 0">
			<div v-for="argument in filters[filterSelectId - 1].args">
				<label>{{ argument.name }}: ({{ argument.value }}) </label>
				<input type="range" :id="filters[filterSelectId - 1].name + '-' + argument.name" :min="argument.min"
					:max="argument.max" :ref="filters[filterSelectId - 1].name + '-' + argument.name" :step="argument.step"
					v-model.number="argument.value">
			</div>
		</div>
		<button class="button" @click="applyFilter()">Appliquer le filter</button>
	</div>
</template>

<style scoped>
@import url("@/customSelect.css");

label {
	color: white;
}

.editor {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	gap: 10px;
}
</style>