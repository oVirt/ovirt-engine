package org.ovirt.engine.ui.uicommonweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.AuditLogSeverity;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkQoSModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ExternalNetwork;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

public final class Linq {

    public static class AuditLogComparer implements Comparator<AuditLog>, Serializable {
        private static final long serialVersionUID = 7488030875073130111L;

        @Override
        public int compare(AuditLog x, AuditLog y) {
            long xid = x.getAuditLogId();
            long yid = y.getAuditLogId();

            return Long.compare(xid, yid);
        }
    }

    public static final class IdentifiableComparator<T extends Identifiable> implements Comparator<T>, Serializable {

        private static final long serialVersionUID = 1698501567658288106L;

        @Override
        public int compare(T o1, T o2) {
            if (o1 == o2) {
                return 0;
            } else if (o1 == null || o2 == null) {
                return (o1 == null) ? -1 : 1;
            } else {
                return o1.getValue() - o2.getValue();
            }
        }
    }

    public static class VdsSPMPriorityComparer implements Comparator<VDS>, Serializable {
        private static final long serialVersionUID = 1114793850392069219L;

        @Override
        public int compare(VDS vds1, VDS vds2) {
            return Integer.compare(vds2.getVdsSpmPriority(), vds1.getVdsSpmPriority());
        }
    }

    public static class ServerBricksComparer implements Comparator<GlusterBrickEntity>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(GlusterBrickEntity brick1, GlusterBrickEntity brick2) {
            return brick1.getVolumeName().compareTo(brick2.getVolumeName());
        }

    }

    public static class StorageDeviceComparer implements Comparator<StorageDevice>, Serializable {

        private static final long serialVersionUID = -7569798731454543377L;

        @Override
        public int compare(StorageDevice device1, StorageDevice device2) {
            // Descending order on canCreateBrick then ascending order on Name
            int deviceStatusComparison =
                    device1.getCanCreateBrick() == device2.getCanCreateBrick() ? 0 : device2.getCanCreateBrick() ? 1
                            : -1;
            return deviceStatusComparison == 0 ? device1.getName().compareTo(device2.getName())
                    : deviceStatusComparison;
        }

    }

    public static class GlusterVolumeGeoRepSessionComparer implements Comparator<GlusterGeoRepSession>, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public int compare(GlusterGeoRepSession session0, GlusterGeoRepSession session1) {
            return session0.getSlaveVolumeName().compareTo(session1.getSlaveVolumeName());
        }
    }

    public static class GlusterVolumeSnapshotComparer implements Comparator<GlusterVolumeSnapshotEntity>, Serializable {
        private static final long serialVersionUID = -6085272225112945249L;

        @Override
        public int compare(GlusterVolumeSnapshotEntity snapshot0, GlusterVolumeSnapshotEntity snapshot1) {
            return snapshot0.getSnapshotName().compareTo(snapshot1.getSnapshotName());
        }
    }

    public static class GlusterVolumeSnapshotConfigComparator implements Comparator<GlusterVolumeSnapshotConfig>, Serializable {
        private static final long serialVersionUID = 2L;

        @Override
        public int compare(GlusterVolumeSnapshotConfig param0, GlusterVolumeSnapshotConfig param1) {
            return param0.getParamName().compareTo(param1.getParamName());
        }
    }

    public static class DiskImageByActualSizeComparer implements Comparator<DiskImage>, Serializable {
        private static final long serialVersionUID = -7287055507900698918L;

        @Override
        public int compare(DiskImage x, DiskImage y) {
            return Double.compare(x.getActualSize(), y.getActualSize());
        }

    }

    public static class StorageDomainModelByNameComparer implements Comparator<StorageDomainModel>, Serializable {

        private static final long serialVersionUID = 5142897643241941178L;

        @Override
        public int compare(StorageDomainModel x, StorageDomainModel y) {
            return x.getStorageDomain().getStorageName().compareTo(y.getStorageDomain().getStorageName());
        }

    }

    public static class DiskModelByAliasComparer implements Comparator<DiskModel>, Serializable {
        private static final long serialVersionUID = 4293731121213688683L;

        @Override
        public int compare(DiskModel x, DiskModel y) {
            return x.getDisk().getDiskAlias().compareTo(y.getDisk().getDiskAlias());
        }
    }

    public static class SnapshotByCreationDateCommparer implements Comparator<Snapshot>, Serializable {

        private static final long serialVersionUID = -4063737182979806402L;

        @Override
        public int compare(Snapshot x, Snapshot y) {
            return x.getCreationDate().compareTo(y.getCreationDate());
        }

    }

    /**
     * Checks if Any StorageDomain Is Master And Active
     */
    public static boolean isAnyStorageDomainIsMasterAndActive(List<StorageDomain> sdl) {
        for (StorageDomain a : sdl) {
            if (a.getStorageDomainType() == StorageDomainType.Master && a.getStatus() != null
                    && a.getStatus() == StorageDomainStatus.Active) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDataActiveStorageDomain(StorageDomain storageDomain) {
        boolean isData = storageDomain.getStorageDomainType().isDataDomain();

        boolean isActive = storageDomain.getStatus() == StorageDomainStatus.Active;

        return isData && isActive;
    }

    /**
     * Check if storage_domains item with specified Guid exist in List
     */
    public static boolean isSDItemExistInList(List<StorageDomain> items, Guid id) {
        return firstOrNull(items, new IdPredicate<>(id)) != null;
    }

    /**
     * Check if Cluster item with specified id exist in List
     */
    public static boolean isClusterItemExistInList(List<Cluster> items, Guid id) {
        return firstOrNull(items, new IdPredicate<>(id)) != null;
    }

    public static VdsNetworkInterface findInterfaceByName(List<VdsNetworkInterface> items, String name) {
        return firstOrNull(items, new NamePredicate(name));
    }

    public static VdsNetworkInterface findInterfaceByNetworkName(List<VdsNetworkInterface> items, final String name) {
        return firstOrNull(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return Objects.equals(i.getNetworkName(), name);
            }
        });
    }

    public static VdsNetworkInterface findInterfaceByIsBond(List<VdsNetworkInterface> items) {
        return firstOrNull(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return i.getBonded() != null && i.getBonded();
            }
        });
    }

    public static VdsNetworkInterface findInterfaceNetworkNameNotEmpty(List<VdsNetworkInterface> items) {
        return firstOrNull(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return !StringHelper.isNullOrEmpty(i.getNetworkName());
            }
        });
    }

    public static Collection<VdsNetworkInterface> findAllInterfaceNetworkNameNotEmpty(List<VdsNetworkInterface> items) {
        return where(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return !StringHelper.isNullOrEmpty(i.getNetworkName());
            }
        });
    }

    public static Collection<VdsNetworkInterface> findAllInterfaceBondNameIsEmpty(List<VdsNetworkInterface> items) {
        return where(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return StringHelper.isNullOrEmpty(i.getBondName());
            }
        });
    }

    public static Collection<VdsNetworkInterface> findAllInterfaceVlanIdIsEmpty(List<VdsNetworkInterface> items) {
        return where(items, new IPredicate<VdsNetworkInterface>() {
            @Override
            public boolean match(VdsNetworkInterface i) {
                return i.getVlanId() == null;
            }
        });
    }

    public static Network findManagementNetwork(List<Network> networks) {
        return firstOrNull(networks, new IPredicate<Network>() {
            @Override
            public boolean match(Network network) {
                return network.getCluster().isManagement();
            }
        });
    }

    public static Network findNetworkByName(List<Network> items, String name) {
        return firstOrNull(items, new NamePredicate(name));

    }

    public static NetworkQoS findNetworkQosById(Iterable<NetworkQoS> items, Guid qosId) {
        return firstOrDefault(items, new IdPredicate<>(qosId), NetworkQoSModel.EMPTY_QOS);
    }

    public static HostNetworkQos findHostNetworkQosById(Iterable<HostNetworkQos> items, Guid qosId) {
        return firstOrDefault(items, new IdPredicate<>(qosId), NetworkModel.EMPTY_HOST_NETWORK_QOS);
    }

    public static Collection<VDS> findAllVDSByPmEnabled(List<VDS> items) {
        return where(items, new IPredicate<VDS>() {
            @Override
            public boolean match(VDS i) {
                return i.isPmEnabled();
            }
        });
    }

    public static Collection<StorageDomain> findAllStorageDomainsBySharedStatus(List<StorageDomain> items,
            final StorageDomainSharedStatus status) {
        return where(items, new IPredicate<StorageDomain>() {
            @Override
            public boolean match(StorageDomain i) {
                return i.getStorageDomainSharedStatus() == status;
            }
        });
    }

    /**
     * Produces the set difference of two sequences by using the default equality
     *
     * @param first
     *            A {@link List} whose elements that are not also in second will be returned.
     * @param second
     *            A {@link List} whose elements that also occur in the first sequence will
     *            cause those elements to be removed from the returned sequence.
     * @return A sequence that contains the set difference of the elements of two sequences.
     */
    public static <TSource> ArrayList<TSource> except(ArrayList<TSource> first,
            ArrayList<TSource> second) {
        ArrayList<TSource> newIEnumerable = new ArrayList<>();

        if (first != null && second != null) {
            for (TSource t : first) {
                if (!second.contains(t)) {
                    newIEnumerable.add(t);
                }
            }
        }

        return second == null ? first : newIEnumerable;
    }

    public static int count(Iterable<?> source) {
        int result = 0;
        for (Object ignored : source) {
            result++;
        }

        return result;
    }

    public static <TSource> TSource firstOrNull(Iterable<TSource> source) {
        return firstOrNull(source, new TruePredicate<TSource>());
    }

    public static <TSource> TSource firstOrNull(Iterable<TSource> source, IPredicate<? super TSource> predicate) {
        return firstOrDefault(source, predicate, null);
    }

    public static <TSource> TSource firstOrDefault(Iterable<TSource> source, IPredicate<? super TSource> predicate,
            TSource defaultValue) {
        if (source != null) {
            for (TSource item : source) {
                if (predicate.match(item)) {
                    return item;
                }
            }
        }

        return defaultValue;
    }

    public static <TSource> boolean all(Iterable<TSource> source, IPredicate<? super TSource> predicate) {

        for (TSource item : source) {
            if (!predicate.match(item)) {
                return false;
            }
        }

        return true;
    }

    public static <TSource> Collection<TSource> where(Collection<TSource> source,
            IPredicate<? super TSource> predicate) {
        List<TSource> list = new ArrayList<>();

        for (TSource item : source) {
            if (predicate.match(item)) {
                list.add(item);
            }
        }

        return list;
    }

    public static Version selectHighestVersion(List<Version> versions) {
        Version retVersion = firstOrNull(versions);
        for (Version version : versions) {
            if (version.compareTo(retVersion) > 0) {
                retVersion = version;
            }
        }
        return retVersion;
    }

    /**
     * Returns a new instance of list containing all items of the provided source.
     */
    public static <TSource> ArrayList<TSource> toList(Iterable<TSource> source) {
        ArrayList<TSource> list = new ArrayList<>();
        for (TSource item : source) {
            list.add(item);
        }

        return list;
    }

    public static <TSource> ArrayList<TSource> distinct(ArrayList<TSource> source,
            IEqualityComparer<TSource> comparer) {
        ArrayList<TSource> list = new ArrayList<>();
        for (TSource a : source) {
            boolean found = false;
            for (TSource b : list) {
                if (comparer.equals(a, b)) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                list.add(a);
            }
        }

        return list;
    }

    public static <TResult> ArrayList<TResult> cast(Iterable<?> source) {
        ArrayList<TResult> list = new ArrayList<>();
        for (Object a : source) {
            TResult item = (TResult) a;
            list.add(item);
        }

        return list;
    }

    public static List concatUnsafe(List... lists) {
        List result = new ArrayList<>();
        for (List list : lists) {
            for (Object item : list) {
                result.add(item);
            }
        }

        return result;
    }

    @SafeVarargs
    public static <T> List<T> concat(List<T>... lists) {
        return concatUnsafe(lists);
    }

    public static <U, V> List<Pair<U, V>> zip(List<U> objects, List<V> vms) {
        if (objects.size() != vms.size()) {
            throw new RuntimeException("Zip called on lists of different lengths"); //$NON-NLS-1$
        }
        final List<Pair<U, V>> result = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            result.add(new Pair<>(objects.get(i), vms.get(i)));
        }
        return result;
    }

    public static <U, V> List<Pair<U, V>> wrapAsFirst(List<U> list, Class<V> secondComponentClass) {
        final List<Pair<U, V>> result = new ArrayList<>();
        for (U object : list) {
            result.add(new Pair<>(object, (V) null));
        }
        return result;
    }

    public static StorageDomain getStorageById(Guid storageId, List<StorageDomain> storageDomains) {
        return firstOrNull(storageDomains, new IdPredicate<>(storageId));
    }

    public static ArrayList<StorageDomain> getStorageDomainsByIds(List<Guid> storageIds,
            List<StorageDomain> storageDomains) {
        ArrayList<StorageDomain> list = new ArrayList<>();
        for (Guid storageId : storageIds) {
            StorageDomain storageDomain = getStorageById(storageId, storageDomains);
            if (storageDomain != null) {
                list.add(storageDomain);
            }
        }
        return list;
    }

    public static <T> ArrayList<EntityModel<T>> toEntityModelList(List<T> list) {
        ArrayList<EntityModel<T>> entityModelList = new ArrayList<>();

        if (list != null) {
            for (T item : list) {
                EntityModel<T> model = new EntityModel<>();
                model.setEntity(item);
                entityModelList.add(model);
            }
        }

        return entityModelList;
    }

    public static ArrayList<DiskModel> filterDisksByType(List<DiskModel> diskModels, DiskStorageType type) {
        ArrayList<DiskModel> filteredList = new ArrayList<>();

        if (diskModels != null) {
            for (DiskModel item : diskModels) {
                if (item.getDisk().getDiskStorageType() == type) {
                    filteredList.add(item);
                }
            }
        }

        return filteredList;
    }

    public static <I, T extends I> T findByType(Collection<I> models, Class<T> specific) {
        for (I model : models) {
            if (model.getClass().equals(specific)) {
                return (T) model;
            }
        }

        return null;
    }

    public static Collection<EntityModel<?>> findSelectedItems(Collection<EntityModel<?>> items) {
        if (items == null) {
            return Collections.emptyList();
        }

        return where(items, new IPredicate<EntityModel<?>>() {
            @Override
            public boolean match(EntityModel<?> entityModel) {
                return entityModel.getIsSelected();
            }
        });
    }

    public static DiskModel diskToModel(Disk disk) {
        DiskModel diskModel = new DiskModel();
        diskModel.getAlias().setEntity(disk.getDiskAlias());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            EntityModel<Integer> sizeEntity = new EntityModel<>();
            sizeEntity.setEntity((int) diskImage.getSizeInGigabytes());
            diskModel.setSize(sizeEntity);
            ListModel<VolumeType> volumeList = new ListModel<>();
            volumeList.setItems(diskImage.getVolumeType() == VolumeType.Preallocated ?
                    new ArrayList<>(Arrays.asList(new VolumeType[]{VolumeType.Preallocated}))
                    : AsyncDataProvider.getInstance().getVolumeTypeList());
            volumeList.setSelectedItem(diskImage.getVolumeType());
            diskModel.setVolumeType(volumeList);
        }

        diskModel.setDisk(disk);

        return diskModel;
    }

    public static ArrayList<DiskModel> disksToDiskModelList(List<Disk> disks) {
        ArrayList<DiskModel> diskModels = new ArrayList<>();

        for (Disk disk : disks) {
            diskModels.add(diskToModel(disk));
        }

        return diskModels;
    }

    public static Set<String> getDiskAliases(List<? extends Disk> disks) {
        Set<String> aliases = new HashSet<>();
        for (Disk disk : disks) {
            aliases.add(disk.getDiskAlias());
        }
        return aliases;
    }

    public static List<DiskImage> imagesSubtract(Iterable<DiskImage> images, Iterable<DiskImage> imagesToSubtract) {
        List<DiskImage> subtract = new ArrayList<>();
        for (DiskImage image : images) {
            if (Linq.getDiskImageById(image.getId(), imagesToSubtract) == null) {
                subtract.add(image);
            }
        }
        return subtract;
    }

    private static DiskImage getDiskImageById(Guid id, Iterable<DiskImage> diskImages) {
        return firstOrNull(diskImages, new IdPredicate<>(id));
    }

    public static class TimeZonePredicate implements IPredicate<TimeZoneModel> {
        private final String timeZone;

        public TimeZonePredicate(String timeZone) {
            this.timeZone = timeZone;
        }

        @Override
        public boolean match(TimeZoneModel source) {
            return Objects.equals(source.getTimeZoneKey(), timeZone);
        }
    }

    public static class ServerCpuPredicate implements IPredicate<ServerCpu> {
        private final String cpuName;

        public ServerCpuPredicate(String cpuName) {
            this.cpuName = cpuName;
        }

        @Override
        public boolean match(ServerCpu source) {
            return Objects.equals(source.getCpuName(), cpuName);
        }
    }

    public static class EqualsPredicate implements IPredicate<Object> {
        private final Object object;

        public EqualsPredicate(Object object) {
            this.object = object;
        }

        @Override
        public boolean match(Object source) {
            return source.equals(object);
        }
    }

    public static class DataCenterWithClusterAccordingClusterPredicate implements IPredicate<DataCenterWithCluster> {

        private IdPredicate<Guid> idPredicate;

        public DataCenterWithClusterAccordingClusterPredicate(Guid clusterId) {
            this.idPredicate = new IdPredicate<>(clusterId);
        }

        @Override
        public boolean match(DataCenterWithCluster source) {
            return idPredicate.match(source.getCluster());
        }

    }

    public static class DataCenterWithClusterPredicate implements IPredicate<DataCenterWithCluster> {

        private final Guid dataCenterId;

        private final Guid clusterId;

        public DataCenterWithClusterPredicate(Guid dataCenterId, Guid clusterId) {
            this.dataCenterId = dataCenterId;
            this.clusterId = clusterId;
        }

        @Override
        public boolean match(DataCenterWithCluster source) {
            return source.getDataCenter() != null &&
                    source.getCluster() != null &&
                    source.getDataCenter().getId().equals(dataCenterId) &&
                    source.getCluster().getId().equals(clusterId);

        }

    }

    public static class DataCenterStatusPredicate implements IPredicate<StoragePool> {
        private StoragePoolStatus status = StoragePoolStatus.values()[0];

        public DataCenterStatusPredicate(StoragePoolStatus status) {
            this.status = status;
        }

        @Override
        public boolean match(StoragePool source) {
            return source.getStatus() == status;
        }
    }

    public static class DataCenterNotStatusPredicate implements IPredicate<StoragePool> {

        private DataCenterStatusPredicate predicate;

        public DataCenterNotStatusPredicate(StoragePoolStatus status) {
            this.predicate = new DataCenterStatusPredicate(status);
        }

        @Override
        public boolean match(StoragePool source) {
            return !predicate.match(source);
        }
    }

    public static class ValidateSucceedPredicate implements IPredicate<VdcReturnValueBase> {

        @Override
        public boolean match(VdcReturnValueBase source) {
            return source.isValid();
        }
    }

    public static class HostStatusPredicate implements IPredicate<VDS> {
        private VDSStatus status = VDSStatus.values()[0];

        public HostStatusPredicate(VDSStatus status) {
            this.status = status;
        }

        @Override
        public boolean match(VDS source) {
            return source.getStatus().equals(status);
        }
    }

    public static class TemplateWithVersionPredicate implements IPredicate<TemplateWithVersion> {
        private final Guid id;
        private boolean useLatest;

        public TemplateWithVersionPredicate(Guid id, boolean useLatest) {
            this.id = id;
            this.useLatest = useLatest;
        }

        @Override
        public boolean match(TemplateWithVersion templateWithVersion) {
            if (useLatest) {
                return templateWithVersion.getTemplateVersion() instanceof LatestVmTemplate;
            } else {
                return id.equals(templateWithVersion.getTemplateVersion().getId())
                        && !(templateWithVersion.getTemplateVersion() instanceof LatestVmTemplate);
            }
        }
    }

    public static class IdPredicate<T extends Serializable> implements IPredicate<BusinessEntity<T>> {
        private T id;

        public IdPredicate(T id) {
            this.id = id;
        }

        public boolean match(BusinessEntity<T> entity) {
            return entity != null && Objects.equals(entity.getId(), id);
        }
    }

    public static class IdsPredicate<T extends Serializable> implements IPredicate<BusinessEntity<T>> {

        private Set<T> ids;

        public IdsPredicate(Collection<T> ids) {
            this.ids = new HashSet<>(ids);
        }

        @Override
        public boolean match(BusinessEntity<T> entity) {
            return ids.contains(entity.getId());
        }
    }

    public static class NamePredicate implements IPredicate<Nameable> {

        private final String name;

        public NamePredicate(String name) {
            this.name = name;
        }

        @Override
        public boolean match(Nameable entity) {
            return Objects.equals(name, entity.getName());
        }
    }

    public static class LunPredicate implements IPredicate<LunModel> {
        private final LunModel lun;

        public LunPredicate(LunModel lun) {
            this.lun = lun;
        }

        @Override
        public boolean match(LunModel source) {
            return Objects.equals(source.getLunId(), lun.getLunId());
        }
    }

    public static class TargetPredicate implements IPredicate<SanTargetModel> {
        private final SanTargetModel target;

        public TargetPredicate(SanTargetModel target) {
            this.target = target;
        }

        @Override
        public boolean match(SanTargetModel source) {
            return Objects.equals(source.getName(), target.getName())
                    && Objects.equals(source.getAddress(), target.getAddress())
                    && Objects.equals(source.getPort(), target.getPort());
        }
    }

    public static class DbUserPredicate implements IPredicate<DbUser> {
        private final DbUser target;

        public DbUserPredicate(DbUser target) {
            this.target = target;
        }

        @Override
        public boolean match(DbUser source) {
            String targetName = target.getLoginName();
            if (!StringHelper.isNullOrEmpty(targetName)) {
                targetName = targetName.toLowerCase();
            }
            return Objects.equals(source.getDomain(), target.getDomain())
                    && (StringHelper.isNullOrEmpty(target.getLoginName())
                    || "*".equals(target.getLoginName()) //$NON-NLS-1$
                    || source.getLoginName().toLowerCase().startsWith(targetName));
        }
    }

    public static class NetworkSameProviderPredicate implements IPredicate<Provider> {

        private final Network network;

        public NetworkSameProviderPredicate(Network network) {
            this.network = network;
        }

        @Override
        public boolean match(Provider provider) {
            return network.isExternal() && provider.getId().equals(network.getProvidedBy().getProviderId());
        }

    }

    public interface IPredicate<TSource> {
        boolean match(TSource source);
    }

    private static class TruePredicate<TSource> implements IPredicate<TSource> {
        @Override
        public boolean match(TSource tSource) {
            return true;
        }
    }

    public static final class StorageDomainByPoolNameComparator implements Comparator<StorageDomain>, Serializable {
        private static final long serialVersionUID = 990203400356561666L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(StorageDomain domain1, StorageDomain domain2) {
            return lexoNumeric.compare(domain1.getStoragePoolName(), domain2.getStoragePoolName());
        }
    }

    public static final class NetworkInClusterComparator implements Comparator<Network>, Serializable {

        private static final long serialVersionUID = 990203400356561587L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(Network net1, Network net2) {

            final boolean managementNetwork1 = net1.getCluster().isManagement();
            final boolean managementNetwork2 = net2.getCluster().isManagement();

            if (!managementNetwork1 && !managementNetwork2) {
                return lexoNumeric.compare(net1.getName(), net2.getName());
            } else {
                return managementNetwork1 ? -1 : 1;
            }
        }
    }

    public static final class VnicProfileViewComparator implements Comparator<VnicProfileView>, Serializable {

        private static final long serialVersionUID = 990203400356561587L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(VnicProfileView vnicProfile1, VnicProfileView vnicProfile2) {
            if (vnicProfile1 == VnicProfileView.EMPTY) {
                return vnicProfile2 == VnicProfileView.EMPTY ? 0 : 1;
            } else if (vnicProfile2 == VnicProfileView.EMPTY) {
                return -1;
            }

            int retVal = lexoNumeric.compare(vnicProfile1.getNetworkName(), vnicProfile2.getNetworkName());

            return retVal == 0 ? lexoNumeric.compare(vnicProfile1.getName(), vnicProfile2.getName()) : retVal;
        }
    }

    public static final class ClusterNetworkModelComparator implements Comparator<ClusterNetworkModel>, Serializable {

        private static final long serialVersionUID = -8571840939180248617L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(ClusterNetworkModel model1, ClusterNetworkModel model2) {
            // management first
            if (model1.isManagement()) {
                return -1;
            } else if (model2.isManagement()) {
                return 1;
            }

            return lexoNumeric.compare(model1.getNetworkName(), model2.getNetworkName());
        }
    }

    public static final class ExternalNetworkComparator implements Comparator<ExternalNetwork>, Serializable {
        private static final long serialVersionUID = 4987035011384708563L;
        private final NameableComparator comparator = new NameableComparator();

        @Override
        public int compare(ExternalNetwork net1, ExternalNetwork net2) {
            return comparator.compare(net1.getNetwork(), net2.getNetwork());
        };
    }

    public static <T extends Disk> Collection<T> filterNonSnapableDisks(
            Collection<Disk> source) {
        return (Collection<T>) where(source, new IPredicate<Disk>() {
            @Override
            public boolean match(Disk source) {
                return source.isAllowSnapshot();
            }
        });
    }

    public static <T extends AuditLog> Collection<T> filterAudidLogsByExcludingSeverity(
            Collection<AuditLog> source, final AuditLogSeverity severity) {
        return (Collection<T>) where(source, new IPredicate<AuditLog>() {
            @Override
            public boolean match(AuditLog source) {
                return source.getSeverity() != severity;
            }
        });
    }

    public static <T extends Disk> Collection<T> filterDisksByStorageType(
            Collection<Disk> source, final DiskStorageType diskStorageType) {
        return (Collection<T>) where(source, new IPredicate<Disk>() {
            @Override
            public boolean match(Disk source) {
                return source.getDiskStorageType() == diskStorageType;
            }
        });
    }

    public static Collection<Provider> filterProvidersByProvidedType(Collection<Provider> source,
            final VdcObjectType type) {
        return where(source, new IPredicate<Provider>() {

            @Override
            public boolean match(Provider provider) {
                return provider.getType().getProvidedTypes().contains(type);
            }
        });
    }

    public static final class ProviderTypeComparator implements Comparator<ProviderType>, Serializable {
        private static final long serialVersionUID = -7917198421355959306L;

        @Override
        public int compare(ProviderType type1, ProviderType type2) {
            final EnumTranslator enumTranslator = EnumTranslator.getInstance();
            return LexoNumericComparator.comp(enumTranslator.translate(type1), enumTranslator.translate(type2));
        }
    }

    /**
     * pre-defined cluster policies should be ordered first, then order lexicographically
     */
    public static final class ClusterPolicyComparator implements Comparator<ClusterPolicy>, Serializable {
        private static final long serialVersionUID = -409780241393930906L;
        final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(ClusterPolicy cp1, ClusterPolicy cp2) {
            if (cp1.isLocked() != cp2.isLocked()) {
                return cp1.isLocked() ? -1 : 1;
            }
            return lexoNumeric.compare(cp1.getName(), cp2.getName());
        }
    }

    /**
     * sort policy units by:
     * first is external?
     * second is disabled?
     * third policyUnitType
     * forth name (lexicography)
     */
    public static final class PolicyUnitComparator implements Comparator<PolicyUnit>, Serializable {
        private static final long serialVersionUID = -6155037911174811346L;
        final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(PolicyUnit pu1, PolicyUnit pu2) {
            if (pu1.isInternal() != pu2.isInternal()) {
                return !pu1.isInternal() ? -1 : 1;
            }
            if (pu1.isEnabled() != pu2.isEnabled()) {
                return !pu1.isEnabled() ? -1 : 1;
            }
            if (pu1.getPolicyUnitType() != pu2.getPolicyUnitType()) {
                if (pu1.getPolicyUnitType().equals(PolicyUnitType.FILTER)
                        || pu2.getPolicyUnitType().equals(PolicyUnitType.LOAD_BALANCING)) {
                    return -1;
                }
                if (pu2.getPolicyUnitType().equals(PolicyUnitType.FILTER)
                        || pu1.getPolicyUnitType().equals(PolicyUnitType.LOAD_BALANCING)) {
                    return 1;
                }
            }
            return lexoNumeric.compare(pu1.getName(), pu2.getName());
        }
    }

    public static final class SharedMacPoolComparator implements Comparator<MacPool>, Serializable {

        private static final long serialVersionUID = 3603082617231645079L;
        final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(MacPool o1, MacPool o2) {
            if (o1.isDefaultPool()) {
                return -1;
            } else if (o2.isDefaultPool()) {
                return 1;
            } else {
                return lexoNumeric.compare(o1.getName(), o2.getName());
            }
        }
    }

    public static final class ImportEntityComparator<T> implements Comparator<ImportEntityData<T>>, Serializable {
        private static final long serialVersionUID = 6596945138956015466L;
        final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(ImportEntityData<T> entity1, ImportEntityData<T> entity2) {
            return lexoNumeric.compare(entity1.getName(), entity2.getName());
        }
    }

    public static Collection<StorageDomain> filterStorageDomainsByStorageType(
            Collection<StorageDomain> source, final StorageType storageType) {
        return where(source, new IPredicate<StorageDomain>() {
            @Override
            public boolean match(StorageDomain source) {
                return source.getStorageType() == storageType;
            }
        });
    }

    public static Collection<StorageDomain> filterStorageDomainsByStorageDomainType(
            Collection<StorageDomain> source, final StorageDomainType storageDomainType) {
        return where(source, new IPredicate<StorageDomain>() {
            @Override
            public boolean match(StorageDomain source) {
                return source.getStorageDomainType() == storageDomainType;
            }
        });
    }

    public static Collection<StorageDomain> filterStorageDomainsByStorageStatus(
            Collection<StorageDomain> source, final StorageDomainStatus storageDomainStatus) {
        return where(source, new IPredicate<StorageDomain>() {
            @Override
            public boolean match(StorageDomain source) {
                return source.getStatus() == storageDomainStatus;
            }
        });
    }

    public static Collection<StorageDomain> filterStorageDomainById(
            Collection<StorageDomain> source, final Guid id) {
        return where(source, new IdPredicate<>(id));
    }

    public static VDS findHostByIdFromIdList(Collection<VDS> items, List<Guid> hostIdList) {
        return firstOrNull(items, new IdsPredicate<>(hostIdList));
    }
}
