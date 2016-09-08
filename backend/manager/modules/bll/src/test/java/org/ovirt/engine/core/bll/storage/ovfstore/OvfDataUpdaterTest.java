package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@RunWith(MockitoJUnitRunner.class)
public class OvfDataUpdaterTest {
    private OvfDataUpdater ovfDataUpdater;
    private Map<Guid, Map<Guid, Boolean>> map;

    @Mock
    private StoragePoolDao storagePoolDao;

    @Before
    public void setUp() {
        ovfDataUpdater = spy(OvfDataUpdater.getInstance());
        map = new HashMap<>();
        doReturn(storagePoolDao).when(ovfDataUpdater).getStoragePoolDao();
        mockAnswers();

        StoragePool pool1 = new StoragePool();
        pool1.setId(Guid.newGuid());

        StoragePool pool2 = new StoragePool();
        pool2.setId(Guid.newGuid());

        doReturn(Arrays.asList(pool1, pool2)).when(storagePoolDao).getAllByStatus(StoragePoolStatus.Up);
    }

    @Test
    public void poolUpdateOvfStoreOnAnyDomainSupported() throws Exception {
        ovfDataUpdater.ovfUpdateTimer();
        verify();
    }

    private void mockAnswers() {
        doAnswer(invocation -> {
            VdcReturnValueBase returnValueBase = new VdcReturnValueBase();
            Map<Guid, Boolean> domains = new HashMap<>();
            Set<Guid> domainIds = new HashSet<>();
            domainIds.add(Guid.newGuid());
            domainIds.add(Guid.newGuid());
            for (Guid domainId : domainIds) {
                domains.put(domainId, Boolean.FALSE);
            }
            returnValueBase.setActionReturnValue(domainIds);
            Guid storagePoolId = (Guid) invocation.getArguments()[0];
            map.put(storagePoolId, domains);
            return returnValueBase;
        }).when(ovfDataUpdater).performOvfUpdateForStoragePool(any(Guid.class));

        doAnswer(invocation -> {
            Guid storagePoolId = (Guid) invocation.getArguments()[0];
            Guid storageDomainId = (Guid) invocation.getArguments()[1];
            map.get(storagePoolId).put(storageDomainId, Boolean.TRUE);
            return null;
        }).when(ovfDataUpdater).performOvfUpdateForDomain(any(Guid.class), any(Guid.class));
    }

    private void verify() {
        assertTrue(map.values().stream().flatMap(x -> x.values().stream()).allMatch(x -> x));
    }
}
