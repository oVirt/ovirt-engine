package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.hibernate.validator.util.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ LoggerFactory.class })
public class ValidationUtilsTest {

    @Before
    public void setup() throws SecurityException, NoSuchMethodException, Exception {
        mockStatic(LoggerFactory.class);

        Logger mock = mock(Logger.class);
        when(LoggerFactory.make()).thenReturn(mock);

    }

    @Test
    public void testcontainsIlegalCharacters() {
        String[] straValid = new String[] { "www_redhat_com", "127001", "www_REDHAT_1" };
        String[] straInvalid = new String[] { "www.redhatcom", "me@localhost", "no/worries" };
        for (String s : straValid) {
            assertTrue("Valid strings: " + s, !ValidationUtils.containsIlegalCharacters(s));
        }

        for (String s : straInvalid) {
            assertTrue("Invalid strings: " + s, ValidationUtils.containsIlegalCharacters(s));
        }
    }

    @Test
    public void testIsInvalidHostname() {
        String[] straValidHosts =
                new String[] { "www.redhat.com", "127.0.0.1", "www.rhn.redhat.com" };
        String[] straInvalidHosts =
                new String[] { "www.redhat#com", "123/456", "www@redhat.com", "www.řhň.řěďháť.čőm", "你好世界",
                        "שלוםעולם" };
        for (String s : straValidHosts) {
            assertTrue("Valid host name: " + s, ValidationUtils.validHostname(s));
        }

        for (String s : straInvalidHosts) {
            assertTrue("Invalid host name: " + s, !ValidationUtils.validHostname(s));
        }
    }
}
