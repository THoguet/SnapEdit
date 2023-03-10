<script setup lang="ts">
import { defineComponent } from 'vue';
import { api } from '@/http-api';
</script>
<script lang="ts">
export default defineComponent({
	props: {
		id: {
			type: Number,
			required: true
		}
	},
	methods: {
		async getImage(id: number) {
			const data = await api.getImage(id);
			const reader = new window.FileReader();
			reader.readAsDataURL(data);
			reader.onload = () => {
				const imgElt = document.getElementById("img-" + id) as HTMLImageElement;
				if (imgElt !== null) {
					if (reader.result as string) {
						imgElt.setAttribute("src", (reader.result as string));
					}
				}
			};
		}
	},
	mounted() {
		this.getImage(this.id);
	},
	watch: {
		id() {
			this.getImage(this.id);
		}
	}
})
</script>
<template>
	<img :id="'img-' + id" />
</template>

<style></style>