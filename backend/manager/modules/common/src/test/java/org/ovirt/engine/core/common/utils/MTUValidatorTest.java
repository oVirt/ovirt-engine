package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.validation.annotation.MTU;

public class MTUValidatorTest {

    private Validator validator;

    @Before
    public void setup() throws Exception {
        validator = ValidationUtils.getValidator();
    }

    @Test
    public void invalidLowMTU() {
        Set<ConstraintViolation<MtuContainer>> validate = validate(new MtuContainer(30));
        assertTrue(!validate.isEmpty());

    }

    @Test
    public void useDefaultMTU() {
        Set<ConstraintViolation<MtuContainer>> validate = validate(new MtuContainer(0));
        assertTrue(validate.isEmpty());
    }

    private <T> Set<ConstraintViolation<T>> validate(T object) {
        return validator.validate(object);
    }

    private static class MtuContainer {
        @MTU
        private int mtu;

        public MtuContainer(int mtu) {
            super();
            this.mtu = mtu;
        }
    }
}
