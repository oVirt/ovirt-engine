package org.ovirt.engine.core.vdsbroker;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

@RunWith(MockitoJUnitRunner.class)
public class EffectiveHostNetworkQosTest {

    @Mock
    private HostNetworkQosDao hostNetworkQosDao;

    @InjectMocks
    private EffectiveHostNetworkQos effectiveHostNetworkQos;

    @Test
    public void testGetQosWithNullNetworkAttachmentAndNetworkWithoutQos() throws Exception {
        assertThat(effectiveHostNetworkQos.getQos(null, new Network()), nullValue());
    }

    @Test
    public void testGetQosWithNullNetworkAttachmentAndNetworkWithQos() throws Exception {
        HostNetworkQos hostNetworkQos = createHostNetworkQos();
        Network network = createNetworkWithQos(hostNetworkQos);
        Mockito.when(hostNetworkQosDao.get(network.getQosId())).thenReturn(hostNetworkQos);

        assertThat(effectiveHostNetworkQos.getQos(null, network), is(hostNetworkQos));
    }

    @Test(expected = NullPointerException.class)
    public void testGetQosWithNullNetwork() throws Exception {
        effectiveHostNetworkQos.getQos(createNetworkAttachmentWithoutOverriddenQos(), null);
    }

    @Test
    public void testGetQosWhenNetworkAttachmentDoesNotHaveOverriddenQos() throws Exception {
        HostNetworkQos hostNetworkQos = createHostNetworkQos();
        Network network = createNetworkWithQos(hostNetworkQos);
        NetworkAttachment networkAttachment = createNetworkAttachmentWithoutOverriddenQos();

        Mockito.when(hostNetworkQosDao.get(network.getQosId())).thenReturn(hostNetworkQos);

        assertThat(effectiveHostNetworkQos.getQos(networkAttachment, network), is(hostNetworkQos));
        Mockito.verify(hostNetworkQosDao).get(Mockito.eq(network.getQosId()));
        Mockito.verifyNoMoreInteractions(hostNetworkQosDao);
    }

    @Test
    public void testGetQosWhenNetworkAttachmentHasOverriddenQos() throws Exception {
        Network network = createNetworkWithQos(createHostNetworkQos());
        NetworkAttachment networkAttachment = createNetworkAttachentWithOverriddenQos();
        HostNetworkQos networkAttachmentHostNetworkQos = HostNetworkQos.fromAnonymousHostNetworkQos(networkAttachment.getHostNetworkQos());

        assertThat(effectiveHostNetworkQos.getQos(networkAttachment, network), is(networkAttachmentHostNetworkQos));
        Mockito.verifyNoMoreInteractions(hostNetworkQosDao);

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
