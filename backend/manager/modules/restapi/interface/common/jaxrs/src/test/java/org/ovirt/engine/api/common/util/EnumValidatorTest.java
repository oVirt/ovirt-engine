/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.common.util;

import static org.ovirt.engine.api.common.util.EnumValidator.validateEnum;

import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.api.model.Fault;

public class EnumValidatorTest extends Assert {

    @Test
    public void testValid() throws Exception {
        assertEquals(Thread.State.NEW, validateEnum(Thread.State.class, "NEW"));
    }

    @Test
    public void testInvalid() throws Exception {
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
