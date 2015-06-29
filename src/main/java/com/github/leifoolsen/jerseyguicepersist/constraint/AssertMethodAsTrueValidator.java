// Multiple properties validation, see: http://soadev.blogspot.no/2010/01/jsr-303-bean-validation.html

package com.github.leifoolsen.jerseyguicepersist.constraint;

import com.github.leifoolsen.jerseyguicepersist.util.SneakyThrow;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;

public class AssertMethodAsTrueValidator implements ConstraintValidator<AssertMethodAsTrue, Object> {
    private String methodName;

    @Override
    public void initialize(AssertMethodAsTrue assertMethodAsTrue) {
        methodName =  assertMethodAsTrue.value();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        try {
            Class clazz = object.getClass();
            Method validate = clazz.getMethod(methodName);
            return (Boolean) validate.invoke(object);
        } catch (Throwable t) {
            SneakyThrow.propagate(t);
        }
        return false;
    }
}