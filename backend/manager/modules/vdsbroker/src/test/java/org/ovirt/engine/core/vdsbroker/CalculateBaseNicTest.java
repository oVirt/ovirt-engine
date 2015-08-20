package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@RunWith(MockitoJUnitRunner.class)
public class CalculateBaseNicTest {

    @Mock
    private InterfaceDao interfaceDao;

    @InjectMocks
    private CalculateBaseNic calculateBaseNic;


    private VdsNetworkInterface baseNic = createNic("baseNic");
    private VdsNetworkInterface vlanNic = createVlanNic(baseNic);

    @Test(expected = NullPointerException.class)
    public void testGetBaseNicWithNullNic() throws Exception {
        calculateBaseNic.getBaseNic(null);
    }

    @Test()
    public void testGetBaseNicBaseNicIsSimplyReturned() throws Exception {
        verifyNoMoreInteractions(interfaceDao);
        assertThat(calculateBaseNic.getBaseNic(baseNic, null), is(baseNic));

    }

    @Test()
    public void testGetBaseNicVerifyDelegation() throws Exception {
        CalculateBaseNic spy = Mockito.spy(calculateBaseNic);

        spy.getBaseNic(baseNic);
        verify(spy).getBaseNic(Matchers.<VdsNetworkInterface> any(),
            Matchers.<Map<String, VdsNetworkInterface>> any());
    }

    @Test()
    public void testGetBaseNicWhenCacheIsNotProvided() throws Exception {
        when(interfaceDao.get(baseNic.getVdsId(), baseNic.getName())).thenReturn(baseNic);
        assertThat(calculateBaseNic.getBaseNic(vlanNic, null), is(baseNic));
        verify(interfaceDao).get(eq(baseNic.getVdsId()), eq(baseNic.getName()));
        verifyNoMoreInteractions(interfaceDao);
    }

    @Test()
    public void testGetBaseNicUsingCacheNotHavingRequiredRecord() throws Exception {
        when(interfaceDao.get(baseNic.getVdsId(), baseNic.getName())).thenReturn(baseNic);
        Map<String, VdsNetworkInterface> cachedExistingInterfaces =
            Collections.singletonMap("unrelatedNicName", new VdsNetworkInterface());
        assertThat(calculateBaseNic.getBaseNic(vlanNic, cachedExistingInterfaces), is(baseNic));
        verify(interfaceDao).get(eq(baseNic.getVdsId()), eq(baseNic.getName()));
        verifyNoMoreInteractions(interfaceDao);
    }

    @Test()
    public void testGetBaseNicUsingCache() throws Exception {
        Map<String, VdsNetworkInterface> cachedExistingInterfaces =
            Collections.singletonMap(baseNic.getName(), baseNic);
        assertThat(calculateBaseNic.getBaseNic(vlanNic, cachedExistingInterfaces), is(baseNic));
        verifyNoMoreInteractions(interfaceDao);
    }

    private VdsNetworkInterface createVlanNic(VdsNetworkInterface baseNic) {
        VdsNetworkInterface vlanNic = createNic("vlanNic");
        vlanNic.setBaseInterface(baseNic.getName());
        vlanNic.setVlanId(100);
        vlanNic.setVdsId(baseNic.getVdsId());
        return vlanNic;
    }

    private VdsNetworkInterface createNic(String nicName) {
        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setName(nicName);
        nic.setVdsId(Guid.newGuid());
        return nic;
    }
}
