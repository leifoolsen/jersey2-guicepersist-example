// Multiple properties validation, see: http://soadev.blogspot.no/2010/01/jsr-303-bean-validation.html

package com.github.leifoolsen.jerseyguicepersist.constraint;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value={TYPE, ANNOTATION_TYPE})
@Retention(value=RUNTIME)
@Documented

public @interface AssertMethodsAsTrue {
    AssertMethodAsTrue[] value() default {};
}