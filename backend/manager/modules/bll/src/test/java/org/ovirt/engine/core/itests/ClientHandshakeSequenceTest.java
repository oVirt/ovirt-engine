package org.ovirt.engine.core.itests;

import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.RunVmParams;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 31, 2009 Time: 11:28:01 AM To change this template use File |
 * Settings | File Templates.
 */
@Ignore
public class ClientHandshakeSequenceTest extends AbstractBackendTest {

    @Test
    public void getDomainList() {
        VdcQueryReturnValue value = backend.RunPublicQuery(VdcQueryType.GetDomainList, new VdcQueryParametersBase());
        assertTrue(value.getSucceeded());
        assertNotNull(value.getReturnValue());
        System.out.println(value.getReturnValue());
    }

    @Test
    public void getVersion() {
        VdcQueryReturnValue value = backend.RunPublicQuery(VdcQueryType.GetConfigurationValue,
                new GetConfigurationValueParameters(ConfigurationValues.VdcVersion));
        assertNotNull(value);
        assertNotNull(value.getReturnValue());
        System.out.println("Version: " + value.getReturnValue());
    }

    @Test
    public void loginAdmin() {
        VdcReturnValueBase value = backend.Login(new LoginUserParameters("admin", "admin", "domain", "os", "browser",
                "client_type"));
        assertTrue(value.getSucceeded());
        assertNotNull(value.getActionReturnValue());
    }

    @Test
    public void testRegisterBookmarksQuery() throws InterruptedException {
        Guid guid = Guid.NewGuid();
        RegisterQueryParameters params = new RegisterQueryParameters(guid, VdcQueryType.GetAllBookmarks,
                new VdcQueryParametersBase());
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.RegisterQuery, params);
        assertNotNull(value);
        assertTrue(value.getSucceeded());
    }

    @Test
    public void testRegisterVmsSearch() {
        Guid guid = Guid.NewGuid();
        SearchParameters searchParams = new SearchParameters("VM:*", SearchType.VM);
        RegisterQueryParameters params = new RegisterQueryParameters(guid, VdcQueryType.Search, searchParams);
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.RegisterQuery, params);
        assertNotNull(value);
        assertTrue(value.getSucceeded());
    }

    @Test
    public void testRegisterDatacentersSearch() {
        Guid guid = Guid.NewGuid();
        SearchParameters searchParams = new SearchParameters("DATACENTER:*", SearchType.StoragePool);
        RegisterQueryParameters params = new RegisterQueryParameters(guid, VdcQueryType.Search, searchParams);
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.RegisterQuery, params);
        assertNotNull(value);
        assertTrue(value.getSucceeded());
    }

    @Test
    public void testRegisterHostsSearch() {
        Guid guid = Guid.NewGuid();
        SearchParameters searchParams = new SearchParameters("HOST:*", SearchType.VDS);
        RegisterQueryParameters params = new RegisterQueryParameters(guid, VdcQueryType.Search, searchParams);
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.RegisterQuery, params);
        assertNotNull(value);
        assertTrue(value.getSucceeded());
    }

    @Test
    public void testRunVm() {
        RunVmParams params = new RunVmParams(Guid.NewGuid());
        VdcReturnValueBase result = backend.runInternalAction(VdcActionType.RunVm, params);
    }

}
