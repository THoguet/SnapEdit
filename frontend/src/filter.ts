export enum FilterType {
	"range" = "range",
	"select" = "select",
}


export abstract class Parameters {
	name: string
	displayName: string
	value: number
	type: FilterType

	constructor(name: string, displayName: string, value: any = undefined, type: FilterType) {
		this.name = name
		this.displayName = displayName
		this.value = value
		this.type = type
	}

}

export class RangeParameters extends Parameters {
	min: number
	max: number
	step: number
	constructor(name: string, displayName: string, min: number, max: number, step: number) {
		const value = max / 2;
		super(name, displayName, value, FilterType.range)
		this.min = min
		this.max = max
		this.step = step
	}
}

export class SelectParameters extends Parameters {
	options: string[]
	constructor(name: string, displayName: string, options: string[]) {
		super(name, displayName, 0, FilterType.select)
		this.options = options
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
	return '&' + f.parameters.map((param) => {
		switch (param.type) {
			case FilterType.range:
				const paramCastRange = param as RangeParameters
				return paramCastRange.name + "=" + paramCastRange.value
			case FilterType.select:
				const paramCastSelect = param as SelectParameters
				return paramCastSelect.name + "=" + paramCastSelect.value
		}
	}
	).join("&")
}

export function clone(f: Filter): Filter {
	return new Filter(f.name, f.path, f.parameters.map((param) => {
		switch (param.type) {
			case FilterType.range:
				const paramCastRange = param as RangeParameters
				return new RangeParameters(param.name, param.displayName, paramCastRange.min, paramCastRange.max, paramCastRange.step)
			case FilterType.select:
				const paramCastSelect = param as SelectParameters
				return new SelectParameters(param.name, param.displayName, paramCastSelect.options)
		}
	}))
}