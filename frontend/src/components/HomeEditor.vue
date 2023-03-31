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
			error: "",
		}
	},
	created() {
		api.getAlgorithmList().then((filters) => {
			this.filters = filters;
		});
	},
	methods: {
		applyFilter() {
			if (this.error !== "")
				return;
			if (this.filterSelectId === 0) {
				this.$emit("applyFilter", undefined);
				return;
			}
			const filter = clone(this.filters[this.filterSelectId - 1]);
			for (const arg of filter.parameters) {
				if (arg.value === undefined) {
					this.error = "";
					return;
				}
			}
			this.$emit("applyFilter", filter);
		},
		areInputValid() {
			if (this.filterSelectId === 0)
				return "";
			for (const p of this.filters[this.filterSelectId - 1].parameters) {
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
		filters: {
			handler() {
				for (const f of this.filters) {
					for (const p of f.parameters) {
						if (p.value === undefined)
							p.value = Math.round((p.max - Math.abs(p.min)) / 2);
					}
				}
				this.error = this.areInputValid();
			},
			deep: true
		}
	}
})

</script>
<template>
	<div class="editor">
		<label>Sélectionner un filtre: </label>
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
			<div class="labelInput">
				<label>{{ parameter.name }}</label>
				<input type="number" :min="parameter.min" :max="parameter.max" :step="parameter.step"
					v-model.number="parameter.value" :id="parameter.name" />
			</div>
			<input type="range" :min="parameter.min" :max="parameter.max" :step="parameter.step"
				v-model.number="parameter.value" />
		</div>
		<button @mouseenter="error = areInputValid()" @mouseleave="error = areInputValid()" class="button"
			:class="{ errorClass: error !== '' }" @click="applyFilter()">
			{{ error !== "" ? error : "Appliquer le filtre" }}</button>
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

.errorClass {
	background-color: red;
	color: white;
}
</style>