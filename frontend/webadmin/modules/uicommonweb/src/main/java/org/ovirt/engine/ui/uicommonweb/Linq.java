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
import java.util.function.Predicate;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

public final class Linq {
    public static final Comparator<AuditLog> AuditLogComparer = Comparator.comparing(AuditLog::getAuditLogId);

    public static final Comparator<? super Identifiable> IdentifiableComparator =
            Comparator.nullsFirst(Comparator.comparing(Identifiable::getValue));

    public static final Comparator<StorageDevice> StorageDeviceComparer =
            Comparator.comparing(StorageDevice::getCanCreateBrick).reversed()
                    .thenComparing(StorageDevice::getName);

    public static final Comparator<Snapshot> SnapshotByCreationDateCommparer =
            Comparator.comparing(Snapshot::getCreationDate);

    public static final Comparator<VnicProfileView> VnicProfileViewComparator =
            Comparator.comparing((VnicProfileView v) -> v == VnicProfileView.EMPTY).reversed()
                    .thenComparing(VnicProfileView::getNetworkName, new LexoNumericComparator())
                    .thenComparing(new LexoNumericNameableComparator<>());

    public static final Comparator<MacPool> SharedMacPoolComparator =
            Comparator.comparing(MacPool::isDefaultPool).reversed()
                    .thenComparing(new LexoNumericNameableComparator<>());

    public static boolean isDataActiveStorageDomain(StorageDomain storageDomain) {
        boolean isData = storageDomain.getStorageDomainType().isDataDomain();

        boolean isActive = storageDomain.getStatus() == StorageDomainStatus.Active;

        return isData && isActive;
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

    public static <TSource> TSource firstOrNull(Collection<TSource> source) {
        return firstOrNull(source, new TruePredicate<TSource>());
    }

    public static <TSource> TSource firstOrNull(Collection<TSource> source, IPredicate<? super TSource> predicate) {
        return firstOrDefault(source, predicate, null);
    }

    public static <TSource> TSource firstOrDefault(Collection<TSource> source, IPredicate<? super TSource> predicate,
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

    public static <TSource> List<TSource> where(Collection<TSource> source, IPredicate<? super TSource> predicate) {
        List<TSource> list = new ArrayList<>();

        for (TSource item : source) {
            if (predicate.match(item)) {
                list.add(item);
            }
        }

        return list;
    }

    public static <T> T retrieveFromSet(Set<T> set, final T object) {
        return firstOrNull(set, new EqualsPredicate(object));
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

    public static <TResult> ArrayList<TResult> cast(Collection<?> source) {
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

    public static List<StorageDomain> getStorageDomainsByIds(List<Guid> storageIds,
            List<StorageDomain> storageDomains) {
        return where(storageDomains, new IdsPredicate<>(storageIds));
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

    public static List<DiskModel> filterDisksByType(List<DiskModel> diskModels, DiskStorageType type) {
        return where(diskModels, m -> m.getDisk().getDiskStorageType() == type);
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

    public static List<DiskImage> imagesSubtract(Collection<DiskImage> images, Collection<DiskImage> imagesToSubtract) {
        List<DiskImage> subtract = new ArrayList<>();
        for (DiskImage image : images) {
            if (Linq.getDiskImageById(image.getId(), imagesToSubtract) == null) {
                subtract.add(image);
            }
        }
        return subtract;
    }

    private static DiskImage getDiskImageById(Guid id, Collection<DiskImage> diskImages) {
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
        protected final Guid id;
        protected boolean useLatest;

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

    public static class TemplateWithVersionPredicateForNewVm extends TemplateWithVersionPredicate {
        public TemplateWithVersionPredicateForNewVm(Guid id, boolean useLatest) {
            super(id, useLatest);
        }

        @Override
        public boolean match(TemplateWithVersion templateWithVersion) {
            if (useLatest) {
                return id.equals(templateWithVersion.getTemplateVersion().getId())
                        && templateWithVersion.getTemplateVersion() instanceof LatestVmTemplate;
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

    public interface IPredicate<TSource> extends Predicate<TSource> {
        @Override
        default boolean test(TSource tSource) {
            return match(tSource);
        }

        boolean match(TSource source);
    }

    public static final class Negative<T> implements IPredicate<T> {
        private final IPredicate<? super T> predicate;

        private Negative(IPredicate<? super T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean match(T t) {
            return !predicate.match(t);
        }

        public static <T> IPredicate<T> create(IPredicate<? super T> predicate) {
            return new Negative<>(predicate);
        }
    }

    private static class TruePredicate<TSource> implements IPredicate<TSource> {
        @Override
        public boolean match(TSource tSource) {
            return true;
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

    public static Collection<StorageDomain> filterStorageDomainById(
            Collection<StorageDomain> source, final Guid id) {
        return where(source, new IdPredicate<>(id));
    }

    public static VDS findHostByIdFromIdList(Collection<VDS> items, List<Guid> hostIdList) {
        return firstOrNull(items, new IdsPredicate<>(hostIdList));
    }
}
