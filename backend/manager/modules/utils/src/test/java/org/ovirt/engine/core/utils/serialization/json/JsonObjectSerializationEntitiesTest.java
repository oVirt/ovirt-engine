package org.ovirt.engine.core.utils.serialization.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.ovirt.engine.api.extensions.ExtMap;
import org.ovirt.engine.api.extensions.aaa.Authn;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VdsTransparentHugePagesState;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.RandomUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This test is designed to test that our business entities can be serialized/deserialized by Jackson correctly.
 */
public class JsonObjectSerializationEntitiesTest {
    public static Stream<BusinessEntity<?>> data() {
        RandomUtils random = RandomUtils.instance();
        VdsStatic vdsStatic = new VdsStatic(random.nextString(10),
                random.nextString(10),
                random.nextInt(),
                random.nextInt(),
                random.nextString(10),
                Guid.newGuid(),
                Guid.newGuid(),
                random.nextString(10),
                random.nextBoolean(),
                random.nextEnum(VDSType.class),
                Guid.newGuid());
        return Stream.of(
                vdsStatic,
                randomVdsDynamic(),
                randomVdsStatistics(),
                new VdsSpmIdMap(Guid.newGuid(), Guid.newGuid(), random.nextInt()),
                randomStorageDomainStatic(),
                new StorageDomainDynamic(random.nextInt(), Guid.newGuid(), random.nextInt()),
                randomStoragePool(),
                new StoragePoolIsoMap(Guid.newGuid(), Guid.newGuid(), random.nextEnum(StorageDomainStatus.class)),
                randomRole(),
                new IdContainerClass<>(new VdsSpmIdMap(Guid.newGuid(), Guid.newGuid(), random.nextInt())),
                new IdContainerClass<>(Guid.newGuid()));
    }

    private static StoragePool randomStoragePool() {
        RandomUtils random = RandomUtils.instance();
        StoragePool sp = new StoragePool();
        sp.setdescription(random.nextString(10));
        sp.setComment(random.nextString(10));
        sp.setName(random.nextString(10));
        sp.setId(Guid.newGuid());
        sp.setIsLocal(random.nextBoolean());
        sp.setStatus(random.nextEnum(StoragePoolStatus.class));
        return sp;
    }

    private static StorageDomainStatic randomStorageDomainStatic() {
        RandomUtils random = RandomUtils.instance();
        StorageDomainStatic sds = new StorageDomainStatic();
        sds.setId(Guid.newGuid());
        sds.setStorage(random.nextString(10));
        sds.setStorageType(random.nextEnum(StorageType.class));
        sds.setStorageName(random.nextString(10));
        sds.setDescription(random.nextString(10));
        sds.setWarningLowSpaceIndicator(5);
        sds.setCriticalSpaceActionBlocker(10);
        return sds;
    }

    private static VdsDynamic randomVdsDynamic() {
        RandomUtils random = RandomUtils.instance();
        VdsDynamic vdsDynamic = new VdsDynamic();
        vdsDynamic.setCpuCores(random.nextInt());
        vdsDynamic.setCpuThreads(random.nextInt());
        vdsDynamic.setCpuModel(random.nextString(10));
        vdsDynamic.setCpuSpeedMh(random.nextDouble());
        vdsDynamic.setIfTotalSpeed(random.nextString(10));
        vdsDynamic.setKvmEnabled(random.nextBoolean());
        vdsDynamic.setMemCommited(random.nextInt());
        vdsDynamic.setPhysicalMemMb(random.nextInt());
        vdsDynamic.setStatus(random.nextEnum(VDSStatus.class));
        vdsDynamic.setId(Guid.newGuid());
        vdsDynamic.setVmActive(random.nextInt());
        vdsDynamic.setVmCount(random.nextInt());
        vdsDynamic.setVmMigrating(random.nextInt());
        vdsDynamic.setReservedMem(random.nextInt());
        vdsDynamic.setGuestOverhead(random.nextInt());
        vdsDynamic.setPreviousStatus(random.nextEnum(VDSStatus.class));
        vdsDynamic.setSoftwareVersion(random.nextNumericString(5) + '.' + random.nextNumericString(5));
        vdsDynamic.setVersionName(random.nextString(10));
        vdsDynamic.setPendingVcpusCount(random.nextInt());
        vdsDynamic.setPendingVmemSize(random.nextInt());
        vdsDynamic.setNetConfigDirty(random.nextBoolean());
        vdsDynamic.setTransparentHugePagesState(random.nextEnum(VdsTransparentHugePagesState.class));
        vdsDynamic.setHardwareUUID(Guid.newGuid().toString());
        vdsDynamic.setHardwareFamily(random.nextString(10));
        vdsDynamic.setHardwareSerialNumber(random.nextString(10));
        vdsDynamic.setHardwareVersion(random.nextString(10));
        vdsDynamic.setHardwareProductName(random.nextString(10));
        vdsDynamic.setHardwareManufacturer(random.nextString(10));

        return vdsDynamic;
    }

    private static VdsStatistics randomVdsStatistics() {
        RandomUtils random = RandomUtils.instance();
        VdsStatistics vdsStatistics = new VdsStatistics();
        vdsStatistics.setCpuIdle(random.nextDouble());
        vdsStatistics.setCpuLoad(random.nextDouble());
        vdsStatistics.setCpuSys(random.nextDouble());
        vdsStatistics.setCpuUser(random.nextDouble());
        vdsStatistics.setMemFree(random.nextLong());
        vdsStatistics.setMemShared(random.nextLong());
        vdsStatistics.setUsageCpuPercent(random.nextInt());
        vdsStatistics.setUsageMemPercent(random.nextInt());
        vdsStatistics.setUsageNetworkPercent(random.nextInt());
        vdsStatistics.setCpuOverCommitTimeStamp(new Date(random.nextLong()));
        return vdsStatistics;
    }

    private static Role randomRole() {
        RandomUtils random = RandomUtils.instance();
        Role role = new Role();
        role.setDescription(random.nextString(10));
        role.setId(Guid.newGuid());
        role.setName(random.nextString(10));
        return role;
    }

    @ParameterizedTest
    @MethodSource("data")
    public void serializeAndDesrializeEntity(BusinessEntity<?> entity) {
        String serializedEntity = new JsonObjectSerializer().serialize(entity);
        assertNotNull(serializedEntity);
        Serializable deserializedEntity =
                new JsonObjectDeserializer().deserialize(serializedEntity, entity.getClass());
        assertNotNull(deserializedEntity);
        assertEquals(entity, deserializedEntity);
    }

    @Test
    public void serializeAndDeserializeExtMap() throws Exception {
        ExtMap data = new ExtMap();
        data.put(Authn.AuthRecord.PRINCIPAL, "user1@BRQ-OPENLDAP.RHEV.LAB.ENG.BRQ.REDHAT.COM");
        String json = serialize(data);
        assertTrue(json.length() > 0);
        ExtMap extMap = deserialize(json, ExtMap.class);
        assertNotNull(extMap);
        assertEquals(data, extMap);
    }

    @Test
    public void serializeAndDeserializeExtMapList() throws Exception {
        List<ExtMap> users = new ArrayList<>();
        users.add(new ExtMap().mput(Authn.AuthRecord.PRINCIPAL, "user1@BRQ-OPENLDAP.RHEV.LAB.ENG.BRQ.REDHAT.COM"));
        users.add(new ExtMap().mput(Authn.AuthRecord.PRINCIPAL, "user2@BRQ-OPENLDAP.RHEV.LAB.ENG.BRQ.REDHAT.COM"));

        String json = serialize(users);
        assertTrue(json.length() > 0);

        List<ExtMap> deserializedUsers = deserialize(json, ArrayList.class);
        assertNotNull(deserializedUsers);
        assertEquals(users, deserializedUsers);
    }

    private String serialize(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator())
                .addMixIn(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.writeValueAsString(obj);
    }

    private <T> T deserialize(String json, Class<T> type) throws IOException {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(mapper.getPolymorphicTypeValidator())
                .addMixIn(ExtMap.class, JsonExtMapMixIn.class);
        return mapper.readValue(json, type);
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
            return Objects.hashCode(id);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof IdContainerClass)) {
                return false;
            }
            IdContainerClass other = (IdContainerClass) obj;
            return Objects.equals(id, other.id);
        }
    }
}
