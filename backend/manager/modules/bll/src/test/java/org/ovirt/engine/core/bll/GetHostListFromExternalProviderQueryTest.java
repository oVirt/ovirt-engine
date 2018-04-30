package org.ovirt.engine.core.bll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;

public class GetHostListFromExternalProviderQueryTest extends AbstractQueryTest<GetHostListFromExternalProviderParameters, GetHostListFromExternalProviderQuery<GetHostListFromExternalProviderParameters>> {

    private final VDS host1 = new VDS();
    private final VDS host2 = new VDS();
    private final VDS existingHost1 = new VDS();
    private final VDS existingHost2 = new VDS();
    private final Provider<?> hostProvider = new Provider();

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        host1.setHostName("host1");
        host2.setHostName("host2");
        existingHost1.setHostName("existingHost1");
        existingHost2.setHostName("existingHost2");
    }

    @Test
    public void testAllHosts() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(true);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<>();
        existingHosts.add(existingHost1);
        existingHosts.add(existingHost2);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assertEquals(allHosts, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testHostsContainExistingHosts() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(true);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<>();
        existingHosts.add(host1);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assertEquals(1, ((List<VDS>) getQuery().getQueryReturnValue().getReturnValue()).size());
    }

    @Test
    public void testAllHostsNonFiltered() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(false);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<>();
        allHosts.add(host1);
        allHosts.add(host2);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        getQuery().executeQueryCommand();
        assertEquals(allHosts, getQuery().getQueryReturnValue().getReturnValue());
    }

    @Test
    public void testHostsContainExistingHostsNonFiltered() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(false);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<>();
        allHosts.add(host1);
        allHosts.add(host2);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        getQuery().executeQueryCommand();
        assertEquals(2, ((List<VDS>) getQuery().getQueryReturnValue().getReturnValue()).size());
    }

}
