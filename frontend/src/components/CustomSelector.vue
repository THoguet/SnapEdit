<template>
	<div class="custom-select" @blur="open = false">
		<div class="selected" :class="{ open: open }" @click="open = !open">
			<span>{{ map.get(selected) }}</span>
		</div>
		<div class="items" :class="{ selectHide: !open }">
			<div v-for="[index, name] in map" @click="open = false; $emit('updateSelected', index)">
				<span>{{ name }}</span>
			</div>
		</div>
	</div>
</template>

<script setup lang="ts">
import { defineComponent } from 'vue'
</script>
<script lang="ts">

export default defineComponent({
	props: {
		// map of id to name
		map: {
			type: Map<number, string>,
			required: true,
		},
		selected: {
			type: Number,
			required: true,
		},
	},
	emits: {
		updateSelected(id: number) {
			return id >= 0;
		}
	},
	data() {
		return {
			open: false,
		}
	},
})

</script>

<style scoped>
.custom-select {
	width: 100%;
	text-align: left;
	outline: none;
	height: 47px;
	line-height: 45px;
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
	position: sticky;
	color: #fff;
	border-radius: 0px 0px 6px 6px;
	overflow: hidden;
	border-right: 1px solid #646cff;
	border-left: 1px solid #646cff;
	border-bottom: 1px solid #646cff;
	background-color: #1a1a1a;
	overflow-y: auto;
	max-height: 50vh;
	z-index: 1;
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
</style>