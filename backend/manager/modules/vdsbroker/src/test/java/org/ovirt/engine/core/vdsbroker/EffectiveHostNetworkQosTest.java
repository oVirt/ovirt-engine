package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

@ExtendWith(MockitoExtension.class)
public class EffectiveHostNetworkQosTest {

    @Mock
    private HostNetworkQosDao hostNetworkQosDao;

    @InjectMocks
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Test
    public void testGetQosWithNullNetworkAttachmentAndNetworkWithoutQos() {
        assertThat(effectiveHostNetworkQos.getQos(null, new Network()), nullValue());
    }

    @Test
    public void testGetQosWithNullNetworkAttachmentAndNetworkWithQos() {
        HostNetworkQos hostNetworkQos = createHostNetworkQos();
        Network network = createNetworkWithQos(hostNetworkQos);
        when(hostNetworkQosDao.get(network.getQosId())).thenReturn(hostNetworkQos);

        assertThat(effectiveHostNetworkQos.getQos(null, network), is(hostNetworkQos));
    }

    @Test
    public void testGetQosWithNullNetwork() {
        assertThrows(NullPointerException.class,
                () -> effectiveHostNetworkQos.getQos(createNetworkAttachmentWithoutOverriddenQos(), null));
    }

    @Test
    public void testGetQosWhenNetworkAttachmentDoesNotHaveOverriddenQos() {
        HostNetworkQos hostNetworkQos = createHostNetworkQos();
        Network network = createNetworkWithQos(hostNetworkQos);
        NetworkAttachment networkAttachment = createNetworkAttachmentWithoutOverriddenQos();

        when(hostNetworkQosDao.get(network.getQosId())).thenReturn(hostNetworkQos);

        assertThat(effectiveHostNetworkQos.getQos(networkAttachment, network), is(hostNetworkQos));
        verify(hostNetworkQosDao).get(eq(network.getQosId()));
        verifyNoMoreInteractions(hostNetworkQosDao);
    }

    @Test
    public void testGetQosWhenNetworkAttachmentHasOverriddenQos() {
        Network network = createNetworkWithQos(createHostNetworkQos());
        NetworkAttachment networkAttachment = createNetworkAttachentWithOverriddenQos();
        HostNetworkQos networkAttachmentHostNetworkQos = HostNetworkQos.fromAnonymousHostNetworkQos(networkAttachment.getHostNetworkQos());

        assertThat(effectiveHostNetworkQos.getQos(networkAttachment, network), is(networkAttachmentHostNetworkQos));
        verifyNoMoreInteractions(hostNetworkQosDao);

    }

    private Network createNetworkWithQos(HostNetworkQos hostNetworkQos) {
        Network network = new Network();
        network.setQosId(hostNetworkQos.getId());
        return network;
    }

    private HostNetworkQos createHostNetworkQos() {
        HostNetworkQos hostNetworkQos = new HostNetworkQos();
        hostNetworkQos.setId(Guid.newGuid());
        return hostNetworkQos;
    }

    private NetworkAttachment createNetworkAttachentWithOverriddenQos() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        HostNetworkQos hostNetworkQos = createHostNetworkQos();

        networkAttachment.setHostNetworkQos(AnonymousHostNetworkQos.fromHostNetworkQos(hostNetworkQos));

        return networkAttachment;
    }

    private NetworkAttachment createNetworkAttachmentWithoutOverriddenQos() {
        return new NetworkAttachment();
    }
}
