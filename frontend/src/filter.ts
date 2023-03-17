export class Parameters {
	name: string
	displayName: string
	min: number
	max: number
	step: number
	value: number | undefined

	constructor(name: string, displayName: string, min: number, max: number, step: number = 1, value: number | undefined = undefined) {
		this.name = name
		this.displayName = displayName
		this.min = min
		this.max = max
		this.step = step
		this.value = value
	}

}

export class Filter {
	name: string
	path: string
	parameters: Parameters[]

	constructor(name: string, path: string, parameters: Parameters[]) {
		this.name = name
		this.path = path
		this.parameters = parameters
	}

}

export function getParameters(f: Filter): string {
	return '&' + f.parameters.map((param) => param.name + "=" + param.value).join("&");
}

export function clone(f: Filter): Filter {
	return new Filter(f.name, f.path, f.parameters.map((arg) => new Parameters(arg.name, arg.displayName, arg.min, arg.max, arg.step, arg.value)))
}