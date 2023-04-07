import axios, { AxiosResponse, AxiosError, HttpStatusCode } from 'axios';
import { ImageType } from '@/image';
import { Filter, getParameters } from './filter';

type area = { xmin: number, ymin: number, xmax: number, ymax: number }

interface stateInterface {
	skipped: boolean;
	interrupted: boolean;
	job: string;
	job_count: number;
	job_timestamp: string;
	job_no: number;
	sampling_step: number;
	sampling_steps: number;
}

interface progressInterface {
	progress: number;
	eta_relative: number;
	state: stateInterface;
	current_image: string | null;
	text_info: string | null;
}

interface generateImageInterface {
	images: string[];
	parameters: {};
	info: string;
}


export const cancelRequest = axios.CancelToken.source();

const instance = axios.create({
	baseURL: "/",
	timeout: 0,
	cancelToken: cancelRequest.token
});

const stableDiffInstance = axios.create({
	baseURL: "http://stable.nessar.fr/",
	timeout: 0,
	cancelToken: cancelRequest.token
});

const responseBody = (response: AxiosResponse) => response.data;
const responseStatus = (response: AxiosResponse) => response.status;

const requests = {
	get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
	post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseStatus),
	put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
	delete: (url: string) => instance.delete(url).then(responseBody),
};

const stableDiffRequests = {
	get: (url: string, param: {}) => stableDiffInstance.get(url, param).then(responseBody),
	post: (url: string, body: {}) => stableDiffInstance.post(url, body, { headers: { "Content-Type": "application/json" }, }).then(responseBody),
};

function areaToString(area: area): string {
	return `&xmin=${Math.round(area.xmin)}&ymin=${Math.round(area.ymin)}&xmax=${Math.round(area.xmax)}&ymax=${Math.round(area.ymax)}`;
}

export const api = {
	getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
	getAlgorithmList: (): Promise<Filter[]> => requests.get('algorithms', {}),
	getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob" }),
	applyAlgorithm: (id: number, filter: Filter, selectedArea: area): Promise<string> => requests.get(`images/${id}?algorithm=${filter.path + getParameters(filter) + areaToString(selectedArea)}`, {}),
	createImage: (form: FormData): Promise<HttpStatusCode> => requests.post('images', form),
	deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
	generateImage: (prompt: string, negativePrompt: string, height: number, width: number, steps: number, cfgScale: number): Promise<generateImageInterface> => stableDiffRequests.post('sdapi/v1/txt2img', {
		"prompt": prompt,
		"negative_prompt": negativePrompt,
		"height": height,
		"width": width,
		"steps": steps,
		"cfg_scale": cfgScale,
		"do_not_save_samples": true,
		"do_not_save_grid": true,
		"sampler_index": "Euler a",
		"send_images": true,
		"save_images": false,
	}),
	progress: (): Promise<progressInterface> => stableDiffRequests.get('sdapi/v1/progress', {}),
};