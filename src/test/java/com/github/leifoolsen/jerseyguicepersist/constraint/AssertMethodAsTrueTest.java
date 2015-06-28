package com.github.leifoolsen.jerseyguicepersist.constraint;

import com.github.leifoolsen.jerseyguicepersist.util.ValidatorHelper;
import org.junit.Test;

import javax.validation.ConstraintViolationException;

public class AssertMethodAsTrueTest {

    @AssertMethodAsTrue(value="isValid", message="Did not validate")
    public static class BeanA {
        int a = 1;
        int b = 2;

        public boolean isValid() {
            return a > b;
        }
    }

    public void beanAIsValid() {
        BeanA a = new BeanA();
        a.a = 3;
        ValidatorHelper.validate(a);
    }

    @Test(expected = ConstraintViolationException.class)
    public void beanAIsNotValid() {
        BeanA a = new BeanA();
        ValidatorHelper.validate(a);
    }

    @AssertMethodAsTrue(value="wrongMethodValue", message="Did not validate")
    public static class BeanB {
        int a = 1;
        int b = 2;

        public boolean isValid() {
            return a > b;
        }
    }

    @Test(expected = NoSuchMethodException.class)
    public void beanBHasInvalidMethodValue() {
        BeanB b = new BeanB();
        ValidatorHelper.validate(b);
    }

}
