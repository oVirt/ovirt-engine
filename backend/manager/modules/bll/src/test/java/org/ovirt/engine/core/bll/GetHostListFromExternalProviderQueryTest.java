package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetHostListFromExternalProviderParameters;

public class GetHostListFromExternalProviderQueryTest extends AbstractQueryTest<GetHostListFromExternalProviderParameters, GetHostListFromExternalProviderQuery<GetHostListFromExternalProviderParameters>> {

    private final VDS host1 = new VDS();
    private final VDS host2 = new VDS();
    private final VDS existingHost1 = new VDS();
    private final VDS existingHost2 = new VDS();
    private final Provider hostProvider = new Provider();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        host1.setHostName("host1");
        host2.setHostName("host2");
        existingHost1.setHostName("existingHost1");
        existingHost2.setHostName("existingHost2");
        Provider hostProvider = new Provider();
        hostProvider.setUsername("admin");
        hostProvider.setPassword("password");
        hostProvider.setUrl("http://provider.com");
        hostProvider.setType(ProviderType.FOREMAN);
    }

    @Test
    public void testAllHosts() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(true);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<VDS>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<VDS>();
        existingHosts.add(existingHost1);
        existingHosts.add(existingHost2);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assert(getQuery().getQueryReturnValue().getReturnValue().equals(allHosts));
    }

    @Test
    public void testHostsContainExistingHosts() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(true);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<VDS>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<VDS>();
        existingHosts.add(host1);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assertEquals(1, ((List<VDS>)(getQuery().getQueryReturnValue().getReturnValue())).size());
    }

    @Test
    public void testAllHostsNonFiltered() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(false);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<VDS>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<VDS>();
        existingHosts.add(existingHost1);
        existingHosts.add(existingHost2);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assert(getQuery().getQueryReturnValue().getReturnValue().equals(allHosts));
    }

    @Test
    public void testHostsContainExistingHostsNonFiltered() {
        when(getQueryParameters().isFilterOutExistingHosts()).thenReturn(false);
        doReturn(hostProvider).when(getQuery()).getProvider();
        List<VDS> allHosts = new ArrayList<VDS>();
        allHosts.add(host1);
        allHosts.add(host2);
        List<VDS> existingHosts = new ArrayList<VDS>();
        existingHosts.add(host1);
        doReturn(allHosts).when(getQuery()).getProviderHosts(hostProvider, null);
        doReturn(existingHosts).when(getQuery()).getExistingHosts();
        getQuery().executeQueryCommand();
        assertEquals(2, ((List<VDS>)(getQuery().getQueryReturnValue().getReturnValue())).size());
    }

}
