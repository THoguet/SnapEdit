export interface ImageType {
	id: number;
	name: string;
	data: string;
}

export class ImageClass implements ImageType {
	id: number;
	name: string;
	data: string;
	constructor(id: number, title: string, data: string = "") {
		this.id = id
		this.name = title
		this.data = data
	}
}