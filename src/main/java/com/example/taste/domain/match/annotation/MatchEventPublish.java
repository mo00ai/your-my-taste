package com.example.taste.domain.match.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.taste.domain.match.enums.MatchJobType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MatchEventPublish {
	MatchJobType matchJobType();
}
