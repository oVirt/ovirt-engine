package org.ovirt.engine.core.bll.network.predicate;

import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;

@RunWith(MockitoJUnitRunner.class)
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
        Assert.assertTrue(underTest.test(mockNetworkAttachment));
    }

    @Test
    public void checkSync() {
        initSyncStatus(mockNetworkAttachment, true);
        Assert.assertFalse(underTest.test(mockNetworkAttachment));
    }

}
