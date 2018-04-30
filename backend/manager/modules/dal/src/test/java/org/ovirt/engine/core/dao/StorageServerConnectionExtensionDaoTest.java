package org.ovirt.engine.core.dao;

import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.businessentities.storage.StorageServerConnectionExtension;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;
import org.ovirt.engine.core.utils.RandomUtilsSeedingExtension;

@ExtendWith(RandomUtilsSeedingExtension.class)
public class StorageServerConnectionExtensionDaoTest
        extends BaseGenericDaoTestCase<Guid, StorageServerConnectionExtension, StorageServerConnectionExtensionDao> {

    @Override
    protected StorageServerConnectionExtension generateNewEntity() {
        Guid newId = Guid.newGuid();
        StorageServerConnectionExtension newssce = new StorageServerConnectionExtension();
        newssce.setId(newId);
        fillWithRandomData(newssce);
        return newssce;
    }

    @Override
    protected void updateExistingEntity() {
        fillWithRandomData(existingEntity);
    }

    @Override
    protected Guid getExistingEntityId() {
        return new Guid("9f0852ba-7c96-4974-9cb8-b214a6bf90d8");
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return 2;
    }

    private void fillWithRandomData(StorageServerConnectionExtension ssce) {
        ssce.setHostId(Guid.newGuid());
        ssce.setIqn(RandomUtils.instance().nextXmlString(10));
        ssce.setUserName(RandomUtils.instance().nextXmlString(10));
        ssce.setPassword(RandomUtils.instance().nextXmlString(10));
    }
}
