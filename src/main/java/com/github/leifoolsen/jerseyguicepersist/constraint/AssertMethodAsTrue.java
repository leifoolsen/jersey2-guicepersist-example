// Multiple properties validation, see: http://soadev.blogspot.no/2010/01/jsr-303-bean-validation.html

package com.github.leifoolsen.jerseyguicepersist.constraint;

import javax.validation.Constraint;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( {TYPE, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = {AssertMethodAsTrueValidator.class} )
@Documented
public @interface AssertMethodAsTrue {
    String message() default "{value} returned false";
    String value() default "isValid";
    Class[] groups() default {};
    Class[] payload() default {};
}