export interface ImageType {
	id: number;
	name: string;
	data: string;
	filtered: number;
}

export class ImageClass implements ImageType {
	id: number;
	name: string;
	data: string;
	filtered: number;
	constructor(id: number, title: string, data: string = "", filtered: number = -1) {
		this.id = id
		this.name = title
		this.data = data
		this.filtered = filtered
	}
}