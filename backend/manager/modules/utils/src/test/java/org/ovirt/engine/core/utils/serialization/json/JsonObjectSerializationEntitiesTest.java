package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.utils.RandomUtils;

/**
 * This test is designed to test that our business entities can be serialized/deserialized by Jackson correctly.
 */
@RunWith(Parameterized.class)
public class JsonObjectSerializationEntitiesTest {
    private final BusinessEntity<?> entity;

    public JsonObjectSerializationEntitiesTest(BusinessEntity<?> entity) {
        this.entity = entity;
    }

    @Parameters
    public static Collection<Object[]> data() {
        RandomUtils random = RandomUtils.instance();
        VdsStatic vdsStatic = new VdsStatic(random.nextString(10),
                                    random.nextString(10),
                                    random.nextString(10),
                                    random.nextInt(),
                                    Guid.NewGuid(),
                                    Guid.NewGuid(),
                                    random.nextString(10),
                                    random.nextBoolean(),
                                    random.nextEnum(VDSType.class));
        vdsStatic.setpm_options("option1=value1,option2=value2");
        Object[][] data =
                new Object[][] {
                        { vdsStatic },
                        { new VdsDynamic(random.nextInt(),
                                random.nextString(10),
                                random.nextDouble(),
                                random.nextString(10),
                                random.nextBoolean(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextInt(),
                                Guid.NewGuid(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextEnum(VDSStatus.class),
                                random.nextNumericString(5) + "." + random.nextNumericString(5),
                                random.nextString(10),
                                random.nextString(10),
                                new Date(random.nextLong()),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextBoolean()) },
                        { new VdsStatistics(random.nextDouble(),
                                random.nextDouble(),
                                random.nextDouble(),
                                random.nextDouble(),
                                random.nextLong(),
                                random.nextLong(),
                                random.nextInt(),
                                random.nextInt(),
                                random.nextInt(),
                                Guid.NewGuid()) },
                        { new vds_spm_id_map(Guid.NewGuid(), Guid.NewGuid(), random.nextInt()) },
                        { new storage_domain_static(Guid.NewGuid(),
                                random.nextString(10),
                                random.nextInt(StorageDomainType.values().length),
                                random.nextString(10)) },
                        { new StorageDomainDynamic(random.nextInt(), Guid.NewGuid(), random.nextInt()) },
                        { new storage_pool(random.nextString(10),
                                Guid.NewGuid(),
                                random.nextString(10),
                                random.nextEnum(StorageType.class).getValue(),
                                random.nextInt(StoragePoolStatus.values().length)) },
                        { new storage_pool_iso_map(Guid.NewGuid(),
                                Guid.NewGuid(),
                                random.nextEnum(StorageDomainStatus.class)) },
                        { new Role(random.nextString(10), Guid.NewGuid(), random.nextString(10)) },
                        { new IdContainerClass<vds_spm_id_map>(new vds_spm_id_map(Guid.NewGuid(),
                                Guid.NewGuid(),
                                random.nextInt())) },
                        { new IdContainerClass<NGuid>(new NGuid(NGuid.NewGuid().toString())) },
                        { new IdContainerClass<Guid>(Guid.NewGuid()) }
                };
        return Arrays.asList(data);
    }

    @Test
    public void serializeAndDesrializeEntity() throws Exception {
        String serializedEntity = new JsonObjectSerializer().serialize(entity);
        assertNotNull(serializedEntity);
        Serializable deserializedEntity =
                new JsonObjectDeserializer().deserialize(serializedEntity, entity.getClass());
        assertNotNull(deserializedEntity);
        assertEquals(entity, deserializedEntity);
    }

    /**
     * This class is used to test that a container class with a field with no concrete type information gets serialized
     * an deserializde normally.
     *
     * @param <ID>
     *            The type of the id.
     */
    @SuppressWarnings("serial")
    public static class IdContainerClass<ID extends Serializable> implements BusinessEntity<ID> {
        ID id;

        @SuppressWarnings("unused")
        private IdContainerClass() {
        }

        public IdContainerClass(ID id) {
            this.id = id;
        }

        /**
         * @return the id
         */
        @Override
        public ID getId() {
            return id;
        }

        /**
         * @param id
         *            the id to set
         */
        @Override
        public void setId(ID id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            IdContainerClass other = (IdContainerClass) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }
    }
}
