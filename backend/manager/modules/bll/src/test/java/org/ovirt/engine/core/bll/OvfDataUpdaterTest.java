package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDAO;

@RunWith(MockitoJUnitRunner.class)
public class OvfDataUpdaterTest {
    private OvfDataUpdater ovfDataUpdater;
    private Map<Guid, Map<Guid, Boolean>> map;
    private StoragePool pool1;
    private StoragePool pool2;

    @Mock
    private StoragePoolDAO storagePoolDAO;

    @Before
    public void setUp() {
        ovfDataUpdater = Mockito.spy(OvfDataUpdater.getInstance());
        map = new HashMap<>();
        doReturn(storagePoolDAO).when(ovfDataUpdater).getStoragePoolDao();
        mockAnswers();

        pool1 = new StoragePool();
        pool1.setId(Guid.newGuid());

        pool2 = new StoragePool();
        pool2.setId(Guid.newGuid());

        doReturn(Arrays.asList(pool1, pool2)).when(storagePoolDAO).getAllByStatus(StoragePoolStatus.Up);
    }

    @Test
    public void poolUpdateOvfStoreOnAnyDomainSupported() throws Exception {
        doReturn(true).when(ovfDataUpdater).ovfOnAnyDomainSupported(any(StoragePool.class));
        ovfDataUpdater.ovfUpdate_timer();
        verify(Boolean.TRUE);
    }

    @Test
    public void poolUpdateOvfStoreOnAnyDomainUnsupported() throws Exception {
        doReturn(false).when(ovfDataUpdater).ovfOnAnyDomainSupported(any(StoragePool.class));
        ovfDataUpdater.ovfUpdate_timer();
        verify(Boolean.FALSE);
    }

    private void mockAnswers() {
        doAnswer(new Answer<VdcReturnValueBase>() {
            @Override
            public VdcReturnValueBase answer(InvocationOnMock invocation) throws Throwable {
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
            }

        }).when(ovfDataUpdater).performOvfUpdateForStoragePool(any(Guid.class));

        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Guid storagePoolId = (Guid) invocation.getArguments()[0];
                Guid storageDomainId = (Guid) invocation.getArguments()[1];
                map.get(storagePoolId).put(storageDomainId, Boolean.TRUE);
                return null;
            }

        }).when(ovfDataUpdater).performOvfUpdateForDomain(any(Guid.class), any(Guid.class));
    }

    private void verify(Boolean expected) {
        for (Map<Guid, Boolean> map1 : map.values()) {
            for (Boolean b : map1.values()) {
                assertEquals(expected, b);
            }
        }
    }
}
