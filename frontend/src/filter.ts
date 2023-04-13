export enum FilterType {
	"range" = "range",
	"select" = "select",
	"color" = "color",
	"boolean" = "boolean",
	"area" = "area"
}


export abstract class Parameters {
	name: string
	displayName: string
	type: FilterType
	value: any

	constructor(name: string, displayName: string, type: FilterType) {
		this.name = name
		this.displayName = displayName
		this.type = type
	}

}

export class ColorParameters extends Parameters {
	value: string
	constructor(name: string, displayName: string, value: string) {
		super(name, displayName, FilterType.color)
		this.value = value;
	}
}

export class RangeParameters extends Parameters {
	min: number
	max: number
	step: number
	value: number
	constructor(name: string, displayName: string, min: number, max: number, step: number, value: number = min) {
		super(name, displayName, FilterType.range)
		this.value = value;
		this.min = min
		this.max = max
		this.step = step
	}
}

export class SelectParameters extends Parameters {
	options: string[]
	value: string
	constructor(name: string, displayName: string, options: string[], value = options[0]) {
		super(name, displayName, FilterType.select)
		this.value = value;
		this.options = options
	}
}

export class Area {
	xMin: number
	xMax: number
	yMin: number
	yMax: number

	constructor(xMin: number, xMax: number, yMin: number, yMax: number) {
		this.xMin = xMin
		this.xMax = xMax
		this.yMin = yMin
		this.yMax = yMax
	}

	toString(): string {
		return Math.round(this.xMin) + ";" + Math.round(this.yMin) + ";" + Math.round(this.xMax) + ";" + Math.round(this.yMax)
	}

	clear(): void {
		this.xMin = 0
		this.xMax = 0
		this.yMin = 0
		this.yMax = 0
	}

	clone(): Area {
		return new Area(this.xMin, this.xMax, this.yMin, this.yMax)
	}
}

export class AreaParameters extends Parameters {
	value: Area
	constructor(name: string, displayName: string, value: Area) {
		super(name, displayName, FilterType.select)
		this.value = value;
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
			case FilterType.color:
				const paramCastColor = param as ColorParameters
				return paramCastColor.name + "=" + paramCastColor.value
			case FilterType.area:
				console.log("area")
				const paramCastArea = param as AreaParameters
				return paramCastArea.name + "=" + paramCastArea.value.toString()
		}
	}
	).join("&")
}