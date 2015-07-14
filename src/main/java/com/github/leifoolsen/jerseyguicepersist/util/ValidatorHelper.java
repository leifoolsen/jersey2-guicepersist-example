package com.github.leifoolsen.jerseyguicepersist.util;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class ValidatorHelper {

    private ValidatorHelper() {}

    public static <T> T validate(@NotNull final T bean) {
        if(bean == null) {
            throw new ConstraintViolationException("Bean may not be null.", new HashSet<>());
        }
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(bean);
        if(!constraintViolations.isEmpty()) {
            ConstraintViolation<?> cv = constraintViolations.iterator().next();

            throw new ConstraintViolationException("Constraint(s) violated. " +
                    "First violation is: " + cv.getPropertyPath() + ": "  + cv.getMessage(),
                    new HashSet<ConstraintViolation<?>>(constraintViolations));
        }
        return bean;
    }
}
