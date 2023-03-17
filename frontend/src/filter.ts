export class Arg {
	name: string
	min: number
	max: number
	step: number
	value: number | undefined

	constructor(name: string, min: number, max: number, step: number = 1) {
		this.name = name
		this.min = min
		this.max = max
		this.value = undefined
		this.step = step
	}

}

export class Filter {
	name: string
	path: string
	args: Arg[]
	updated: boolean = false

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
}