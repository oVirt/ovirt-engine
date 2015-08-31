package org.ovirt.engine.core.bll.network.host.function;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;

@RunWith(MockitoJUnitRunner.class)
public class NetworkNameFromNetworkAttachmentTransformationFunctionTest {

    private NetworkNameFromNetworkAttachmentTransformationFunction underTest =
            new NetworkNameFromNetworkAttachmentTransformationFunction();

    @Mock
    private Guid mockNetworkGuid;

    private final String NETWORK_ID = "NETWORK_ID";
    private final String NETWORK_NAME = "NETWORK_NAME";

    private NetworkAttachment createNetworkAttachmentWithMockedId() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        networkAttachment.setNetworkId(mockNetworkGuid);
        return networkAttachment;
    }

    @Test
    public void checkInvalidNetworkNameNullNetworkId() {
        NetworkAttachment networkAttachment = new NetworkAttachment();
        Assert.assertEquals(null, underTest.eval(networkAttachment));
    }

    @Test
    public void checkValidNetworkNameInvalidNetworkId() {
        NetworkAttachment networkAttachment = createNetworkAttachmentWithMockedId();
        networkAttachment.setNetworkName(NETWORK_NAME);
        Assert.assertEquals(NETWORK_NAME, underTest.eval(networkAttachment));
    }

    @Test
    public void checkValidNetworkNameValidNetworkId() {
        NetworkAttachment networkAttachment = createNetworkAttachmentWithMockedId();
        when(mockNetworkGuid.toString()).thenReturn(NETWORK_ID);
        networkAttachment.setNetworkName(NETWORK_NAME);
        Assert.assertEquals(NETWORK_NAME, underTest.eval(networkAttachment));
    }

    @Test
    public void checkInvalidNetworkNameValidNetworkId() {
        NetworkAttachment networkAttachment = createNetworkAttachmentWithMockedId();
        when(mockNetworkGuid.toString()).thenReturn(NETWORK_ID);
        Assert.assertEquals(NETWORK_ID, underTest.eval(networkAttachment));
    }

}
