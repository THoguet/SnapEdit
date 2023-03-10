export interface ImageType {
	id: number;
	name: string;
}

export class ImageClass implements ImageType {
	id: number
	name: string
	constructor(id: number, title: string) {
		this.id = id
		this.name = title
	}
}