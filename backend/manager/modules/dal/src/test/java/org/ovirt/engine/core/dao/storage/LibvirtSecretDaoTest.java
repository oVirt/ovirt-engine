package org.ovirt.engine.core.dao.storage;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseGenericDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;
import org.ovirt.engine.core.dao.LibvirtSecretDao;

/**
 * Unit tests to validate {@link LibvirtSecretDao}.
 */
public class LibvirtSecretDaoTest extends BaseGenericDaoTestCase<Guid, LibvirtSecret, LibvirtSecretDao> {

    private static final int TOTAL_LIBVIRT_SECRETS = 1;

    @Override
    protected LibvirtSecret generateNewEntity() {
        LibvirtSecret libvirtSecret = new LibvirtSecret();
        libvirtSecret.setId(Guid.newGuid());
        libvirtSecret.setValue("MTIzNDU2Cg==");
        libvirtSecret.setUsageType(LibvirtSecretUsageType.CEPH);
        libvirtSecret.setProviderId(FixturesTool.CINDER_PROVIDER_ID);
        return libvirtSecret;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setDescription("123");
    }

    @Override
    protected Guid getExistingEntityId() {
        return FixturesTool.EXISTING_LIBVIRT_SECRET_ID;
    }

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEntitiesTotalCount() {
        return TOTAL_LIBVIRT_SECRETS;
    }
}
