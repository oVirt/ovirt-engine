package org.ovirt.engine.core.searchbackend;

import java.util.Arrays;

import junit.framework.TestCase;

public class SyntaxCheckerTest extends TestCase {

    public void dumpCompletionArray(SyntaxContainer res) {
        System.out.print("[");
        for (String item : res.getCompletionArray()) {
            System.out.print(" " + item);
        }
        System.out.print("]");
    }

    public boolean contains(SyntaxContainer res, String item) {
        boolean returnValue = Arrays.asList(res.getCompletionArray()).contains(item);
        if (!returnValue) {
            this.dumpCompletionArray(res);
        }
        return returnValue;
    }

    /**
     * Test the following where each word should be the completion for the earlier portion Vms : Events =
     */
    public void testVMCompletion() {
        SyntaxChecker chkr = new SyntaxChecker(20, true);
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
        SyntaxChecker chkr = new SyntaxChecker(20, true);
        SyntaxContainer res = null;
        res = chkr.getCompletion("");
        this.dumpCompletionArray(res);
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

}
