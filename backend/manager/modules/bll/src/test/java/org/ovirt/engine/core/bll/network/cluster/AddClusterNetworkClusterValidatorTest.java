package org.ovirt.engine.core.bll.network.cluster;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner.Strict;

@RunWith(Strict.class)
public class AddClusterNetworkClusterValidatorTest extends
        NetworkClusterValidatorTestBase<AddClusterNetworkClusterValidator> {

    @Override
    protected AddClusterNetworkClusterValidator createValidator() {
        return new AddClusterNetworkClusterValidator(interfaceDao, networkDao, vdsDao, networkCluster);
    }
}
