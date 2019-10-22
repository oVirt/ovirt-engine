/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.common.util;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.Fault;

public class EnumValidatorTest {

    @Test
    public void testValid() {
        assertEquals(Thread.State.NEW, validateEnum(Thread.State.class, "NEW"));
    }

    @Test
    public void testInvalid() {
        try {
            validateEnum(Thread.State.class, "foobar");
            fail("expected WebApplicationException on invalid value");
        } catch (WebApplicationException wae) {
            verifyInvalidValueException(wae, "foobar", "State");
        }
    }

    private void verifyInvalidValueException(WebApplicationException wae, String value, String typeName) {
        assertEquals(400, wae.getResponse().getStatus());
        Fault fault = (Fault)wae.getResponse().getEntity();
        assertNotNull(fault);
        assertEquals("Invalid value", fault.getReason());
        assertEquals(value + " is not a member of " + typeName
                + ". Possible values for State are: new, runnable, blocked, waiting, timed_waiting, terminated",
                fault.getDetail());
    }
}
