package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigRule;

public class SyntaxCheckerTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    public boolean contains(SyntaxContainer res, String item) {
        return Arrays.asList(res.getCompletionArray()).contains(item);
    }

    /**
     * Test the following where each word should be the completion for the earlier portion Vms : Events =
     */
    @Test
    public void testVMCompletion() {
        SyntaxChecker chkr = new SyntaxChecker(20);
        SyntaxContainer res = null;
        res = chkr.getCompletion("");
        assertTrue("Vms", contains(res, "Vms"));
        res = chkr.getCompletion("V");
        assertTrue("Vms2", contains(res, "Vms"));
        res = chkr.getCompletion("Vms");
        assertTrue(":", contains(res, ":"));
        res = chkr.getCompletion("Vms : ");
        assertTrue("Events", contains(res, "Events"));
        res = chkr.getCompletion("Vms : Events");
        assertTrue("=", contains(res, "="));
    }

    /**
     * Test the following where each word should be the completion for the earlier portion Host : sortby migrating_vms
     * asc
     */
    public void testHostCompletion() {
        SyntaxChecker chkr = new SyntaxChecker(20);
        SyntaxContainer res = null;
        res = chkr.getCompletion("");
        assertTrue("Hosts", contains(res, "Hosts"));
        res = chkr.getCompletion("H");
        assertTrue("Hots2", contains(res, "Hosts"));
        res = chkr.getCompletion("Host");
        assertTrue(":", contains(res, ":"));
        res = chkr.getCompletion("Host : ");
        assertTrue("sortby", contains(res, "sortby"));
        res = chkr.getCompletion("Host : sortby");
        assertTrue("migrating_vms", contains(res, "migrating_vms"));
        res = chkr.getCompletion("Host : sortby migrating_vms");
        assertTrue("asc", contains(res, "asc"));
    }

    @Test
    public void testGetPagPhrase() {
        mcr.mockConfigValue(ConfigValues.DBPagingType, "wrongPageType");
        mcr.mockConfigValue(ConfigValues.DBPagingSyntax, "wrongPageSyntax");
        SyntaxChecker chkr = new SyntaxChecker(20);
        SyntaxContainer res = new SyntaxContainer("");
        res.setMaxCount(0);

        // check wrong config values
        assertTrue(chkr.getPagePhrase(res, "1") == "");

        mcr.mockConfigValue(ConfigValues.DBPagingType, "Range");
        mcr.mockConfigValue(ConfigValues.DBPagingSyntax, " WHERE RowNum BETWEEN %1$s AND %2$s");

        // check valid config values
        assertTrue(chkr.getPagePhrase(res, "1") != "");
    }
}
