package com.example.taste.common.condition;

import java.util.Arrays;

@FunctionalInterface
public interface Validator<T> {
	boolean isSatisfiedBy(T factor);

	default Validator<T> and(Validator<T> other) {
		return command -> this.isSatisfiedBy(command) && other.isSatisfiedBy(command);
	}

	default Validator<T> or(Validator<T> other) {
		return command -> this.isSatisfiedBy(command) || other.isSatisfiedBy(command);
	}

	default Validator<T> not() {
		return command -> !this.isSatisfiedBy(command);
	}

	static <T> Validator<T> and(Validator<T> left, Validator<T>... right) {
		if (right == null || right.length == 0) {
			return left;
		}
		Validator[] remain = Arrays.stream(right)
			.toList()
			.subList(1, right.length)
			.toArray(new Validator[0]);
		return left.and(and(right[0], remain));
	}

	static <T> Validator<T> or(Validator<T> left, Validator<T>... right) {
		if (right == null || right.length == 0) {
			return left;
		}
		Validator[] remain = Arrays.stream(right)
			.toList()
			.subList(1, right.length)
			.toArray(new Validator[0]);
		return left.or(or(right[0], remain));
	}

	static <T> Validator<T> not(Validator<T> left) {
		return left.not();
	}
}
