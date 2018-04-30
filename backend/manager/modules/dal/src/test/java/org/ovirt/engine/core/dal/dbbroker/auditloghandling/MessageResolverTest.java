package org.ovirt.engine.core.dal.dbbroker.auditloghandling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class MessageResolverTest {

    @Test
    public void testResolveUnknownVariable() {
        final String message = "This is my ${Variable}";
        final String expectedResolved = String.format("This is my %1s", MessageResolver.UNKNOWN_VARIABLE_VALUE);
        Map<String, String> values = Collections.emptyMap();
        String resolvedMessage = MessageResolver.resolveMessage(message, values);
        assertEquals(expectedResolved, resolvedMessage);
    }

    @Test
    public void testResolveKnownVariable() {
        final String message = "This is my ${Variable}";
        final String expectedResolved = "This is my value";
        Map<String, String> values = Collections.singletonMap("variable", "value");
        String resolvedMessage = MessageResolver.resolveMessage(message, values);
        assertEquals(expectedResolved, resolvedMessage);
    }

    @Test
    public void testResolveCombinedMessage() {
        final String message =
                "${first} equals one, ${second} equals two, '${blank}' equals blank and ${nonExist} is unknown";
        final String expectedResolved =
                String.format("one equals one, two equals two, ' ' equals blank and %1s is unknown",
                        MessageResolver.UNKNOWN_VARIABLE_VALUE);
        Map<String, String> values = new HashMap<>();
        values.put("first", "one");
        values.put("second", "two");
        values.put("blank", " ");
        String resolvedMessage = MessageResolver.resolveMessage(message, values);
        assertEquals(expectedResolved, resolvedMessage);
    }

    @Test
    public void testResolveAuditLogableBase() {
        final String vdsName = "TestVDS";
        final String vmName = "TestVM";
        final String message =
                "The VM name is ${vmName}, the VDS name is ${vdsName} and the template name is ${vmTemplateName}";
        final String expectedResolved =
                String.format("The VM name is %1s, the VDS name is %2s and the template name is %3s",
                        vmName,
                        vdsName,
                        MessageResolver.UNKNOWN_VARIABLE_VALUE);

        AuditLogableBase logable = mock(AuditLogableBase.class, RETURNS_DEFAULTS);
        when(logable.getVdsName()).thenReturn("TestVDS");
        when(logable.getVmName()).thenReturn("TestVM");

        String resolvedMessage = MessageResolver.resolveMessage(message, logable);
        assertEquals(expectedResolved, resolvedMessage);
    }
}
