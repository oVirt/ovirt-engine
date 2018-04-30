package org.ovirt.engine.core.bll.storage.ovfstore;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

@ExtendWith(MockitoExtension.class)
public class OvfDataUpdaterTest {
    @InjectMocks
    @Spy
    private OvfDataUpdater ovfDataUpdater;
    private Map<Guid, Map<Guid, Boolean>> map;

    @Mock
    private StoragePoolDao storagePoolDao;

    @BeforeEach
    public void setUp() {
        map = new HashMap<>();
        mockAnswers();

        StoragePool pool1 = new StoragePool();
        pool1.setId(Guid.newGuid());

        StoragePool pool2 = new StoragePool();
        pool2.setId(Guid.newGuid());

        doReturn(Arrays.asList(pool1, pool2)).when(storagePoolDao).getAllByStatus(StoragePoolStatus.Up);
    }

    @Test
    public void poolUpdateOvfStoreOnAnyDomainSupported() {
        ovfDataUpdater.ovfUpdate();
        verify();
    }

    private void mockAnswers() {
        doAnswer(invocation -> {
            ActionReturnValue returnValueBase = new ActionReturnValue();
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
        }).when(ovfDataUpdater).performOvfUpdateForStoragePool(any());

        doAnswer(invocation -> {
            Guid storagePoolId = (Guid) invocation.getArguments()[0];
            Guid storageDomainId = (Guid) invocation.getArguments()[1];
            map.get(storagePoolId).put(storageDomainId, Boolean.TRUE);
            return null;
        }).when(ovfDataUpdater).performOvfUpdateForDomain(any(), any());
    }

    private void verify() {
        assertTrue(map.values().stream().flatMap(x -> x.values().stream()).allMatch(x -> x));
    }
}
