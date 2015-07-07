package org.ovirt.engine.extensions.aaa.builtin.kerberosldap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LdapFilterSearchEnginePreProcessorTest {

    @Test
    public void testNoOp() {
        String filter = "(|($SN=oVirt)($DEPARTMENT=acme))";
        NoOpLdapFilterSearchEnginePreProcessor preProcessor = new NoOpLdapFilterSearchEnginePreProcessor();
        String result = preProcessor.preProcess(filter);
        assertEquals(filter, result);
    }

    @Test
    public void testUpnSplit() {
        String filter = "(|($EMAIL=a@aovirt.org)($PRINCIPAL_NAME=testing@ovirt.org)($DUMMY=dummy)";
        UpnSplitterLdapFilterSearchEnginePreProcessor preProcessor =
                new UpnSplitterLdapFilterSearchEnginePreProcessor();
        String result = preProcessor.preProcess(filter);
        assertEquals("(|($EMAIL=a@aovirt.org)($PRINCIPAL_NAME=testing)($DUMMY=dummy)", result);
    }

}
