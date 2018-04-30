package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@ExtendWith(MockitoExtension.class)
public class CalculateBaseNicTest {

    @Mock
    private InterfaceDao interfaceDao;

    @InjectMocks
    private CalculateBaseNic calculateBaseNic;


    private VdsNetworkInterface baseNic = createNic("baseNic");
    private VdsNetworkInterface vlanNic = createVlanNic(baseNic);

    @Test
    public void testGetBaseNicWithNullNic() {
        assertThrows(NullPointerException.class, () -> calculateBaseNic.getBaseNic(null));
    }

    @Test
    public void testGetBaseNicBaseNicIsSimplyReturned() {
        verifyNoMoreInteractions(interfaceDao);
        assertThat(calculateBaseNic.getBaseNic(baseNic, null), is(baseNic));

    }

    @Test
    public void testGetBaseNicVerifyDelegation() {
        CalculateBaseNic spy = spy(calculateBaseNic);

        spy.getBaseNic(baseNic);
        verify(spy).getBaseNic(any(), isNull());
    }

    @Test
    public void testGetBaseNicWhenCacheIsNotProvided() {
        when(interfaceDao.get(baseNic.getVdsId(), baseNic.getName())).thenReturn(baseNic);
        assertThat(calculateBaseNic.getBaseNic(vlanNic, null), is(baseNic));
        verify(interfaceDao).get(eq(baseNic.getVdsId()), eq(baseNic.getName()));
        verifyNoMoreInteractions(interfaceDao);
    }

    @Test
    public void testGetBaseNicUsingCacheNotHavingRequiredRecord() {
        when(interfaceDao.get(baseNic.getVdsId(), baseNic.getName())).thenReturn(baseNic);
        Map<String, VdsNetworkInterface> cachedExistingInterfaces =
            Collections.singletonMap("unrelatedNicName", new VdsNetworkInterface());
        assertThat(calculateBaseNic.getBaseNic(vlanNic, cachedExistingInterfaces), is(baseNic));
        verify(interfaceDao).get(eq(baseNic.getVdsId()), eq(baseNic.getName()));
        verifyNoMoreInteractions(interfaceDao);
    }

    @Test
    public void testGetBaseNicUsingCache() {
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
