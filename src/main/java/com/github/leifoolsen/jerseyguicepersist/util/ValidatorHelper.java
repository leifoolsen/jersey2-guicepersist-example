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

    public static <T> void validate(@NotNull final T bean) {
        if(bean == null) {
            throw new ConstraintViolationException("Bean may not be null.", new HashSet<ConstraintViolation<?>>());
        }
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(bean);
        if(!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException("Bean Validation constraint(s) violated.",
                    new HashSet<ConstraintViolation<?>>(constraintViolations));
        }
    }
}
