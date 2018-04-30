package org.ovirt.engine.core.bll.network.predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;

@ExtendWith(MockitoExtension.class)
public class NetworkNotInSyncPredicateTest {

    @Mock
    private NetworkAttachment mockNetworkAttachment;

    @Mock
    private ReportedConfigurations mockReportedConfigurations;

    private NetworkNotInSyncPredicate underTest = new NetworkNotInSyncPredicate();

    private void initSyncStatus(NetworkAttachment networkAttachment, boolean sync) {
        when(networkAttachment.getReportedConfigurations()).thenReturn(mockReportedConfigurations);
        when(mockReportedConfigurations.isNetworkInSync()).thenReturn(sync);
    }

    @Test
    public void checkNotSync() {
        initSyncStatus(mockNetworkAttachment, false);
        assertTrue(underTest.test(mockNetworkAttachment));
    }

    @Test
    public void checkSync() {
        initSyncStatus(mockNetworkAttachment, true);
        assertFalse(underTest.test(mockNetworkAttachment));
    }

}
