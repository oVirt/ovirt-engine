package org.ovirt.engine.core.vdsbroker.irsbroker;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

/**
 * Regression tests pinning that IrsProxyImpl.resetIrs() clears spm_vds_id in
 * the DB. Together with ResetIrsVDSCommandTest (ResetIrs with
 * ignoreStopFailed=true always ends in resetIrs()), this is what makes the
 * SPM lease guard in ReconstructMasterDomainCommand one-shot per SPM
 * episode: a refused reconstruct leaves spm_vds_id null, so the next
 * reconstruct attempt skips the guard entirely.
 */
@ExtendWith(MockitoExtension.class)
public class IrsProxyImplResetIrsTest {

    private Guid storagePoolId = Guid.newGuid();

    @Mock
    private StoragePoolDao storagePoolDao;

    @InjectMocks
    private IrsProxyImpl proxy = new IrsProxyImpl(storagePoolId);

    /**
     * resetIrs() must null out spm_vds_id and persist the pool row: this is
     * the DB write the reconstruct lease guard depends on.
     */
    @Test
    public void testResetIrsClearsSpmVdsIdInDb() {
        StoragePool pool = new StoragePool();
        pool.setId(storagePoolId);
        pool.setSpmVdsId(Guid.newGuid());
        when(storagePoolDao.get(storagePoolId)).thenReturn(pool);

        proxy.resetIrs();

        ArgumentCaptor<StoragePool> captor = ArgumentCaptor.forClass(StoragePool.class);
        verify(storagePoolDao).update(captor.capture());
        assertNull(captor.getValue().getSpmVdsId());
    }

    /**
     * A missing pool row must not throw; nothing is persisted.
     */
    @Test
    public void testResetIrsSkipsDbWhenPoolMissing() {
        when(storagePoolDao.get(storagePoolId)).thenReturn(null);

        proxy.resetIrs();

        verify(storagePoolDao, never()).update(any(StoragePool.class));
    }
}
