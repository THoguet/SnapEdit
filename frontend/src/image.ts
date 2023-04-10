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
// return true if a and b are equal (ignoring data)
export function imageEquals(a: ImageType, b: ImageType): boolean {
	return a.id === b.id && a.name === b.name && a.filtered === b.filtered
}