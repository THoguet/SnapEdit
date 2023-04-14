<script setup lang="ts">

import { defineComponent } from 'vue'
import { AreaParameters, Filter, FilterType, RangeParameters, SelectParameters } from '@/filter'
import { api } from "@/http-api";
import CustomSelector from './CustomSelector.vue';

</script>
<script lang="ts">
export default defineComponent({
	props: {
		imageFiltered: {
			type: Number,
			required: true,
		},
		filters: {
			type: Array as () => Filter[],
			required: true,
		},
		processed: {
			type: Boolean,
			required: true,
		},
		progress: {
			type: Number,
			required: true,
		},
		serverError: {
			type: Boolean,
			required: true,
		},
	},
	emits: {
		deleteImage() {
			return true;
		},
		applyFilter(filter: Filter) {
			return filter !== null && filter !== undefined;
		},
		updateImageSelectedId(id: number) {
			return id >= 0;
		}
	},
	data() {
		return {
			filterSelectId: 0,
			error: "",
			sent: false
		};
	},
	created() {
		this.error = this.areInputValid();
	},
	methods: {
		applyFilter() {
			if (this.error !== "")
				return;
			const filter = this.filters[this.filterSelectId];
			for (const arg of filter.parameters) {
				if (arg.type === FilterType.boolean && arg.value === undefined)
					arg.value = false;
				if (arg.type === FilterType.color && arg.value === undefined)
					arg.value = "#000000";
				if (arg.type !== FilterType.area && arg.value === undefined) {
					this.error = "Un paramètre n'a pas été renseigné";
					return;
				}
			}
			this.sent = true;
			this.$emit("applyFilter", filter);
		},
		areInputValid() {
			console.log(this.filters[this.filterSelectId])
			for (let p of this.filters[this.filterSelectId].parameters) {
				console.log(p.path);
				const input = document.getElementById(p.path) as HTMLInputElement;
				console.log(input);
				if (input !== null)
					if (!input.checkValidity())
						return "Paramètre(s) du filtre invalide(s)";
			}
			return "";
		},
		convertFilters() {
			const map = new Map<number, string>();
			for (let i = 0; i < this.filters.length; i++) {
				map.set(i, this.filters[i].name);
			}
			return map;
		},
		convertSelectParameters(parameters: SelectParameters) {
			const map = new Map<number, string>();
			for (let i = 0; i < parameters.options.length; i++) {
				map.set(i, parameters.options[i]);;
			}
			return map;
		},
		deleteFilter() {
			this.$emit('deleteImage');
			this.$emit('updateImageSelectedId', this.imageFiltered)
		},
		titleApply() {
			if (this.error !== "")
				return this.error;
			if (this.sent)
				return "Traitement en cours...";
			return "Appliquer le filtre";
		},
		styledProgress() {
			if (this.error != '') {
				return "red";
			}
			return "linear-gradient(to right, #00ff00 " + this.progress + "%, var(--button-color) " + this.progress + "%)";
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
		},
		processed() {
			this.sent = false;
		},
		serverError() {
			this.error = "Erreur serveur";
			this.sent = false;
		}
	},
	components: { CustomSelector }
})

</script>
<template>
	<div class="editor">
		<label>Sélectionner un filtre: </label>
		<CustomSelector :map="convertFilters()" :selected="filterSelectId" @update-selected="filterSelectId = $event" />
		<!-- Filter parameters -->
		<div v-if="filters.length !== 0" v-for="parameter in filters[filterSelectId].parameters">
			<div v-if="parameter.type === FilterType.range" class="rangeInput">
				<div class="labelInput">
					<label>{{ parameter.name }}: </label>
					<input type="number" :min="(parameter as RangeParameters).min" :max="(parameter as RangeParameters).max"
						:step="(parameter as RangeParameters).step" v-model.number="parameter.value" :id="parameter.path" />
				</div>
				<input type="range" :min="(parameter as RangeParameters).min" :max="(parameter as RangeParameters).max"
					:step="(parameter as RangeParameters).step" v-model.number="(parameter as RangeParameters).value" />
			</div>
			<div v-else-if="parameter.type === FilterType.select" class="selectParam">
				<label>{{ parameter.name }}: </label>
				<CustomSelector :map="convertSelectParameters(parameter as SelectParameters)"
					:selected="(parameter as SelectParameters).options.findIndex((o) => o === (parameter as SelectParameters).value)"
					@update-selected="parameter.value = (parameter as SelectParameters).options[$event]" />
			</div>
			<div v-else-if="parameter.type === FilterType.boolean">
				<label>{{ parameter.name }}: </label>
				<input type="checkbox" v-model.boolean="parameter.value" />
			</div>
			<div v-else-if="parameter.type === FilterType.color">
				<label>{{ parameter.name }}: </label>
				<input type="color" v-model="parameter.value" />
			</div>
			<div v-else-if="parameter.type === FilterType.area">
				<label v-if="(parameter as AreaParameters).cropImage"> Vous pouvez selectionner une
					{{ parameter.name }}</label>
				<label v-else> Veuillez selectioner une {{ parameter.name }}</label>
			</div>
		</div>
		<button :style="{ background: styledProgress() }" @mouseenter="error = areInputValid()"
			@mouseleave="error = areInputValid()" class="button" @click="applyFilter()" :disabled="sent">
			{{ titleApply() }}</button>
		<button v-if="imageFiltered !== -1" class="button" @click="deleteFilter()">Supprimer les filtres</button>
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
	flex-wrap: wrap;
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