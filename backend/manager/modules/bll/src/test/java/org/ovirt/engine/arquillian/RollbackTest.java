package org.ovirt.engine.arquillian;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.springframework.dao.DuplicateKeyException;

@Category(IntegrationTest.class)
public class RollbackTest extends TransactionalTestBase {

    @Inject
    VmDao vmDao;

    private final Guid VM1_GUID = Guid.createGuidFromString("0fe4bc81-5999-4ab6-80f8-7a4a2d4bfacd");
    private final Guid VM2_GUID = Guid.createGuidFromString("0ff4bd81-5899-4ab6-80f8-7a4a2d4bfacd");

    @Before
    public void setUp() {
        vmBuilder.id(VM1_GUID).cluster(clusterBuilder.reset().persist()).persist();
    }

    @Deployment(name = "RollbackTest")
    public static JavaArchive deploy(){
        return createDeployment();
    }

    @Test(expected = DuplicateKeyException.class)
    public void shouldFailOnExistingEntity() {
        vmBuilder.id(VM1_GUID).persist();
    }

    @Test
    public void shouldRollbackAfterPersistingPart1() {
        vmBuilder.id(VM2_GUID).persist();
    }

    @Test
    public void shouldRollbackAfterPersistingPart2() {
        vmBuilder.id(VM2_GUID).persist();
    }

    @Test
    public void shouldHaveAccessToMockedContainerTransactionManager() {
        TransactionSupport.executeInNewTransaction(() -> null);
    }
}
