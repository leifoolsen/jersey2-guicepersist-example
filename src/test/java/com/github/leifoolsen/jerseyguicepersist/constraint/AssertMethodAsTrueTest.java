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

    @Test
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

    @AssertMethodsAsTrue({
            @AssertMethodAsTrue(value="validatorA", message="A must be greater than B"),
            @AssertMethodAsTrue(value="validatorMethodB", message="A must be at least twice as big as B")
    })
    public static class BeanC {
        int a = 1;
        int b = 2;

        public boolean validatorA() { return a > b; }
        public boolean validatorMethodB() { return a*2 >= b; }
    }

    @Test(expected = ConstraintViolationException.class)
    public void beanCIsNotValid() {
        BeanC c = new BeanC();
        ValidatorHelper.validate(c);
    }

    @Test
    public void beanCIsValid() {
        BeanC c = new BeanC();
        c.a = 3;
        ValidatorHelper.validate(c);
    }
}
