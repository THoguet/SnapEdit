<script setup lang="ts">

import { defineComponent } from 'vue'
import { Filter, clone, FilterType, RangeParameters, SelectParameters } from '@/filter'
import { api } from "@/http-api";
import CustomSelector from './CustomSelector.vue';

</script>
<script lang="ts">
export default defineComponent({
	emits: {
		applyFilter(filter: Filter) {
			return filter !== null && filter !== undefined;
		}
	},
	data() {
		return {
			filterSelectId: 0,
			filters: [] as Filter[],
			error: "",
			filterApplied: false,
		};
	},
	created() {
		api.getAlgorithmList().then((filters) => {
			this.filters = filters;
			// fill value attribute
			for (const filter of this.filters) {
				for (const arg of filter.parameters) {
					if (arg.type === FilterType.range) {
						const range = arg as RangeParameters;
						range.value = range.min;
					}
					else if (arg.type === FilterType.select) {
						const select = arg as SelectParameters;
						select.value = select.options[0];
					}
				}
			}
			this.error = this.areInputValid();
		});
	},
	methods: {
		applyFilter() {
			if (this.error !== "")
				return;
			const filter = this.filters[this.filterSelectId];
			for (const arg of filter.parameters) {
				if (arg.value === undefined) {
					this.error = "";
					return;
				}
			}
			this.filterApplied = true;
			this.$emit("applyFilter", filter);
		},
		areInputValid() {
			for (const p of this.filters[this.filterSelectId].parameters) {
				const input = document.getElementById(p.name) as HTMLInputElement;
				if (input === null)
					return "";
				if (!input.checkValidity())
					return "Paramètre(s) du filtre invalide(s)";
			}
			return "";
		}
	},
	watch: {
		filterSelectId() {
			this.error = this.areInputValid();
		},
		filters: {
			handler() {
				this.error = this.areInputValid();
			},
			deep: true
		}
	},
	components: { CustomSelector }
})

</script>
<template>
	<div class="editor">
		<label>Sélectionner un filtre: </label>
		<CustomSelector :list="filters.map((f) => { return f.name })" :selected="filterSelectId"
			@update-selected="filterSelectId = $event" />
		<!-- Filter parameters -->
		<div v-if="filters.length !== 0" v-for="parameter in filters[filterSelectId].parameters" class="rangeInput">
			<div v-if="parameter.type === FilterType.range">
				<div class="labelInput">
					<label>{{ parameter.name }}: </label>
					<input type="number" :min="(parameter as RangeParameters).min" :max="(parameter as RangeParameters).max"
						:step="(parameter as RangeParameters).step" v-model.number="parameter.value" :id="parameter.name" />
				</div>
				<input type="range" :min="(parameter as RangeParameters).min" :max="(parameter as RangeParameters).max"
					:step="(parameter as RangeParameters).step" v-model.number="(parameter as RangeParameters).value" />
			</div>
			<div v-else-if="parameter.type === FilterType.select" class="selectParam">
				<label>{{ parameter.name }}: </label>
				<CustomSelector :list="(parameter as SelectParameters).options"
					:selected="(parameter as SelectParameters).options.findIndex((o) => o === (parameter as SelectParameters).value)"
					@update-selected="parameter.value = (parameter as SelectParameters).options[$event]" />
			</div>
		</div>
		<button @mouseenter="error = areInputValid()" @mouseleave="error = areInputValid()" class="button"
			:class="{ errorClass: error !== '' }" @click="applyFilter()">
			{{ error !== "" ? error : "Appliquer le filtre" }}</button>
		<button v-if="filterApplied" class="button" @click="$emit('deleteImage')">Supprimer les filtres</button>
	</div>
</template>

<style scoped>
label {
	color: white;
	flex-shrink: 0;
}

.labelInput {
	display: flex;
	flex-direction: row;
	align-items: center;
	justify-content: space-between;
}

.labelInput input {
	width: min-content;
	height: 1em;
	font-size: 0.8em;
}

.editor {
	display: flex;
	flex-direction: row;
	justify-content: center;
	align-items: center;
	gap: 10px;
}

.errorClass {
	background-color: red;
	color: white;
}

input:invalid {
	background-color: red;
}

.selectParam {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 10px;
}
</style>