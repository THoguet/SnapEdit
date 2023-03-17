<script setup lang="ts">

import { defineComponent } from 'vue'
import { Filter, clone } from '@/filter'
import { api } from "@/http-api";

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
			filters: [] as Filter[],
			// filters: [
			// 	new Filter("Luminosité", "changeLuminosity", [new Arg("delta", -255, 255)]),
			// 	new Filter("Egalisation d'histogramme", "histogram", []),
			// 	new Filter("Filtre de couleur", "colorFilter", [new Arg("hue", 0, 359)]),
			// 	new Filter("Filter moyenneur", "meanFilter", [new Arg("size", 1, 100, 2)]),
			// 	new Filter("Filtre gaussien", "gaussianFilter", [new Arg("size", 1, 100, 2)]),
			// 	new Filter("Filtre de contours", "contours", []),
			// ],
			error: false,
		}
	},
	created() {
		api.getAlgorithmList().then((filters) => {
			this.filters = filters;
		});
	},
	methods: {
		applyFilter() {
			if (this.filterSelectId === 0) {
				this.$emit("applyFilter", undefined);
				return;
			}
			const filter = clone(this.filters[this.filterSelectId - 1]);
			for (const arg of filter.parameters) {
				if (arg.value === undefined) {
					this.error = true;
					return;
				}
			}
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
		<div v-if="filters.length != 0 && filterSelectId !== 0" v-for="parameter in filters[filterSelectId - 1].parameters"
			class="rangeInput">
			<label>{{ parameter.name }}: ({{ parameter.value }}) </label>
			<input type="range" :min="parameter.min" :max="parameter.max" :step="parameter.step"
				v-model.number="parameter.value" />
		</div>
		<button @mouseleave="error = false" class="button" :class="{ erreurClass: error }" @click="applyFilter()">
			{{ error ? "Erreur" : "Appliquer le filtre" }}</button>
	</div>
</template>

<style scoped>
@import url("@/customSelect.css");

label {
	color: white;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.editor {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	gap: 10px;
}

.erreurClass {
	background-color: red;
	color: white;
}
</style>