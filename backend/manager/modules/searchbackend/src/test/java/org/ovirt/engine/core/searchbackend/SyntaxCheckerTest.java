package org.ovirt.engine.core.searchbackend;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigCommon;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.config.IConfigUtilsInterface;
import org.ovirt.engine.core.utils.MockConfigRule;

public class SyntaxCheckerTest {

    @Rule
    public MockConfigRule mcr = new MockConfigRule();

    public boolean contains(SyntaxContainer res, String item) {
        return Arrays.asList(res.getCompletionArray()).contains(item);
    }

    @Before
    public void setup() {
        final IConfigUtilsInterface configUtils = Mockito.mock(IConfigUtilsInterface.class);
        Mockito.when(configUtils.getValue(ConfigValues.SearchResultsLimit, ConfigCommon.defaultConfigurationVersion))
                .thenReturn(100);
        Mockito.when(configUtils.getValue(ConfigValues.DBPagingType, ConfigCommon.defaultConfigurationVersion))
                .thenReturn("Range");
        Mockito.when(configUtils.getValue(ConfigValues.DBSearchTemplate, ConfigCommon.defaultConfigurationVersion))
                .thenReturn("SELECT * FROM (%2$s) %1$s) as T1 %3$s");
        Mockito.when(configUtils.getValue(ConfigValues.DBPagingSyntax, ConfigCommon.defaultConfigurationVersion))
                .thenReturn("OFFSET (%1$s -1) LIMIT %2$s");

        Config.setConfigUtils(configUtils);
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
    @Test
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

    @Test
    public void testAlerts() {
        SyntaxChecker chkr = new SyntaxChecker(100);

        ISyntaxChecker curSyntaxChecker = SyntaxCheckerFactory.createBackendSyntaxChecker("foo");

        SyntaxContainer res = curSyntaxChecker.analyzeSyntaxState("Events: severity=error", true);
        String query = chkr.generateQueryFromSyntaxContainer(res, true);
        Assert.assertEquals("SELECT * FROM (SELECT audit_log.* FROM  audit_log   WHERE  audit_log.severity = '2'  and (not deleted)  ORDER BY audit_log_id DESC ) as T1 OFFSET (1 -1) LIMIT 0",
                query);
    }

    @Test
    public void testTemplates() {
        SyntaxChecker chkr = new SyntaxChecker(100);

        ISyntaxChecker curSyntaxChecker = SyntaxCheckerFactory.createBackendSyntaxChecker("foo");

        SyntaxContainer res = curSyntaxChecker.analyzeSyntaxState("Templates: ", true);
        String query = chkr.generateQueryFromSyntaxContainer(res, true);
        Assert.assertEquals(
                "SELECT * FROM ((SELECT * FROM vm_templates_view )  ORDER BY name ASC ) as T1 OFFSET (1 -1) LIMIT 0",
                query);
    }

    @Test
    public void testVmPools() {
        SyntaxChecker chkr = new SyntaxChecker(100);

        ISyntaxChecker curSyntaxChecker = SyntaxCheckerFactory.createBackendSyntaxChecker("foo");

        SyntaxContainer res = curSyntaxChecker.analyzeSyntaxState("Pools: ", true);

        String query = chkr.generateQueryFromSyntaxContainer(res, true);

        Assert.assertEquals(
                "SELECT * FROM ((SELECT * FROM vm_pools_full_view )  ORDER BY vm_pool_name ASC ) as T1 OFFSET (1 -1) LIMIT 0",
                query);
    }
}
