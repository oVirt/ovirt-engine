package org.ovirt.engine.core.bll.network.cluster;

public class AddClusterNetworkClusterValidatorTest extends
        NetworkClusterValidatorTestBase<AddClusterNetworkClusterValidator> {

    @Override
    protected AddClusterNetworkClusterValidator createValidator() {
        return new AddClusterNetworkClusterValidator(interfaceDao, networkDao, vdsDao, networkCluster);
    }
}
