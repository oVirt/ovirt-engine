package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.QuotaDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class QuotaValidatorTest {

    @Mock
    private QuotaDao quotaDao;

    private Guid QUOTA_ID = Guid.newGuid();
    private Quota quota;
    private QuotaStorage quotaStorage;
    private QuotaCluster quotaCluster;

    @InjectMocks
    private QuotaValidator validator = new QuotaValidator(QUOTA_ID, false);

    @BeforeEach
    public void setup() {
        quota = new Quota();
        quota.setId(QUOTA_ID);

        quotaStorage = new QuotaStorage(
                Guid.newGuid(),
                QUOTA_ID,
                Guid.newGuid(),
                100L,
                0.0
        );

        quotaCluster = new QuotaCluster(
                Guid.newGuid(),
                QUOTA_ID,
                Guid.newGuid(),
                10, 0,
                100L, 0L
        );


        doReturn(quota).when(quotaDao).getById(QUOTA_ID);
    }

    @Test
    public void testNonexistingQuota() {
        doReturn(null).when(quotaDao).getById(any(Guid.class));
        assertThat(validator.isValid(), failsWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_EXIST));
    }

    @Test
    public void testEmptyQuota() {
        assertThat(validator.isValid(), failsWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_IS_NOT_VALID));
    }

    @Test
    public void testDefinedForDomain() {
        Guid domainId = quotaStorage.getStorageId();
        assertThat(validator.isDefinedForStorageDomain(domainId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_DEFINED_FOR_DOMAIN));

        quota.setGlobalQuotaStorage(quotaStorage);
        assertThat(validator.isDefinedForStorageDomain(domainId), isValid());

        quota.setGlobalQuotaStorage(null);
        quota.getQuotaStorages().add(quotaStorage);
        assertThat(validator.isDefinedForStorageDomain(domainId), isValid());
    }

    @Test
    public void testDefinedForCluster() {
        Guid clusterId = quotaCluster.getClusterId();
        assertThat(validator.isDefinedForCluster(clusterId),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_QUOTA_NOT_DEFINED_FOR_CLUSTER));

        quota.setGlobalQuotaCluster(quotaCluster);
        assertThat(validator.isDefinedForCluster(clusterId), isValid());

        quota.setGlobalQuotaCluster(null);
        quota.getQuotaClusters().add(quotaCluster);
        assertThat(validator.isDefinedForCluster(clusterId), isValid());
    }
}
