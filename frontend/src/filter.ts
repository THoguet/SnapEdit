export class Arg {
	name: string
	min: number
	max: number
	step: number
	value: number | undefined

	constructor(name: string, min: number, max: number, step: number = 1, value: number | undefined = undefined) {
		this.name = name
		this.min = min
		this.max = max
		this.value = value
		this.step = step
	}

}

export class Filter {
	name: string
	path: string
	args: Arg[]

	constructor(name: string, path: string, args: Arg[]) {
		this.name = name
		this.path = path
		this.args = args
	}

	/**
	 * getArgs
	 */
	public getArgs(): string {
		return '&' + this.args.map((arg) => arg.name + "=" + arg.value).join("&");
	}

	public clone(): Filter {
		return new Filter(this.name, this.path, this.args.map((arg) => new Arg(arg.name, arg.min, arg.max, arg.step, arg.value)))
	}
}