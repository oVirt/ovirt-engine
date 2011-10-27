package org.ovirt.engine.core.itests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

/**
 * Created by IntelliJ IDEA. User: gmostizk Date: Aug 19, 2009 Time: 5:17:04 PM To change this template use File |
 * Settings | File Templates.
 */
public class SearchTest extends AbstractBackendTest {

    @Test
    public void searchVds() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("HOST: name=*",
                SearchType.VDS));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchVdsGroups() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("CLUSTER: name=*",
                SearchType.Cluster));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchVms() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("VM: name=*",
                SearchType.VM));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Ignore
    @Test
    public void searchAdGroups() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("ADGROUP: name=*",
                SearchType.AdGroup));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Ignore
    @Test
    public void searchAdUsers() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("ADUSER: name=*",
                SearchType.AdUser));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchTemplates() throws IOException {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("TEMPLATE: name=*",
                SearchType.VmTemplate));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchStorageDomains() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("STORAGE: name=*",
                SearchType.StorageDomain));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchStorageDomainsByPools() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search,
                new SearchParameters("storage: datacenter = " + getBasicSetup().getDataCenter().getname(), SearchType.StorageDomain));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
        assertTrue(((Collection<storage_domains>) value.getReturnValue()).size() == 1);
    }

    @Test
    public void searchVmByStorageDomains() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search,
                new SearchParameters("vms: storage.name = " + getBasicSetup().getStorage().getstorage_name(), SearchType.VM));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
        assertTrue(((Collection<VM>) value.getReturnValue()).size() == 1);
    }

    @Test
    public void searchHostByStorageDomains() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search,
                new SearchParameters("host: storage.name = " + getBasicSetup().getStorage().getstorage_name(), SearchType.VDS));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
        assertTrue(((Collection<VM>) value.getReturnValue()).size() == 1);
    }

    @Test
    public void searchClusterByStorageDomains() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search,
                new SearchParameters("cluster: storage.name = " + getBasicSetup().getStorage().getstorage_name(), SearchType.Cluster));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
        assertTrue(((Collection<VM>) value.getReturnValue()).size() == 1);
    }

    @Test
    public void searchPoolByStorageDomains() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search,
                new SearchParameters("datacenter: storage.name = " + getBasicSetup().getStorage().getstorage_name(), SearchType.StoragePool));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
        assertTrue(((Collection<VM>) value.getReturnValue()).size() == 1);
    }

    @Test
    public void searchStoragePools() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("DATACENTER: name=*",
                SearchType.StoragePool));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchVmPools() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("POOL: name=*",
                SearchType.VmPools));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchDbUsers() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("USER: name=*",
                SearchType.DBUser));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

    @Test
    public void searchEvents() {
        VdcQueryReturnValue value = backend.runInternalQuery(VdcQueryType.Search, new SearchParameters("EVENTS: event_vm=*",
                SearchType.AuditLog));
        assertNotNull(value);
        assertTrue(value.getSucceeded());
        assertNotNull((value.getReturnValue()));
    }

}
