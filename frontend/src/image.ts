export interface ImageType {
	id: number;
	name: string;
	data: string;
	modifed: Map<string, string>;
}

export class ImageClass implements ImageType {
	id: number;
	name: string;
	data: string;
	modifed: Map<string, string>;
	constructor(id: number, title: string, data: string = "", modifed: Map<string, string> = new Map<string, string>()) {
		this.id = id
		this.name = title
		this.data = data
		this.modifed = modifed
	}
}