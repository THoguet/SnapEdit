import axios, { AxiosResponse, AxiosError, HttpStatusCode } from 'axios';
import { ImageType } from '@/image';

const instance = axios.create({
	baseURL: "/",
	timeout: 15000,
});

const responseBody = (response: AxiosResponse) => response.data;
const responseStatus = (response: AxiosResponse) => response.status;

const requests = {
	get: (url: string, param: {}) => instance.get(url, param).then(responseBody),
	post: (url: string, body: {}) => instance.post(url, body, { headers: { "Content-Type": "multipart/form-data" }, }).then(responseStatus),
	put: (url: string, body: {}) => instance.put(url, body).then(responseBody),
	delete: (url: string) => instance.delete(url).then(responseBody)
};

export const api = {
	getImageList: (): Promise<ImageType[]> => requests.get('images', {}),
	getImage: (id: number): Promise<Blob> => requests.get(`images/${id}`, { responseType: "blob" }),
	createImage: (form: FormData): Promise<HttpStatusCode> => requests.post('images', form),
	deleteImage: (id: number): Promise<void> => requests.delete(`images/${id}`),
};