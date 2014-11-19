package org.ovirt.engine.ui.uicommonweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Identifiable;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotConfig;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.PolicyUnit;
import org.ovirt.engine.core.common.scheduling.PolicyUnitType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.DataCenterWithCluster;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicommonweb.models.vms.TimeZoneModel;
import org.ovirt.engine.ui.uicompat.DateTimeUtils;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public final class Linq
{
    public static class AuditLogComparer implements Comparator<AuditLog>, Serializable
    {
        private static final long serialVersionUID = 7488030875073130111L;

        @Override
        public int compare(AuditLog x, AuditLog y)
        {
            long xid = x.getAuditLogId();
            long yid = y.getAuditLogId();

            return Long.valueOf(xid).compareTo(yid);
        }
    }

    public final static class IdentifiableComparator<T extends Identifiable> implements Comparator<T>, Serializable {

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
        public int compare(VDS vds1, VDS vds2)
        {
            return (vds1.getVdsSpmPriority() < vds2.getVdsSpmPriority()) ? 1
                    : ((vds1.getVdsSpmPriority() == vds2.getVdsSpmPriority()) ? 0 : -1);

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
                    (device1.getCanCreateBrick() == device2.getCanCreateBrick() ? 0 : (device2.getCanCreateBrick() ? 1
                            : -1));
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

    public static class DiskImageByLastModifiedComparer implements Comparator<DiskImage>, Serializable
    {

        private static final long serialVersionUID = -6085272225112945238L;

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            if (x.getLastModified().before(y.getLastModified()))
            {
                return -1;
            }

            if (x.getLastModified().after(y.getLastModified()))
            {
                return 1;
            }

            return 0;
        }

    }

    public static class DiskImageByLastModifiedTimeOfDayComparer implements Comparator<DiskImage>, Serializable
    {

        private static final long serialVersionUID = 7206189809641328921L;

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            return DateTimeUtils.getTimeOfDay(x.getLastModified())
                    .compareTo(DateTimeUtils.getTimeOfDay(y.getLastModified()));
        }

    }

    public static class DiskImageByActualSizeComparer implements Comparator<DiskImage>, Serializable {
        private static final long serialVersionUID = -7287055507900698918L;

        @Override
        public int compare(DiskImage x, DiskImage y) {
            return x.getActualSize() < y.getActualSize() ? -1 : x.getActualSize() < y.getActualSize() ? 1 : 0;
        }

    }

    public static class StorageDomainModelByNameComparer implements Comparator<StorageDomainModel>, Serializable
    {

        private static final long serialVersionUID = 5142897643241941178L;

        @Override
        public int compare(StorageDomainModel x, StorageDomainModel y)
        {
            return x.getStorageDomain().getStorageName().compareTo(y.getStorageDomain().getStorageName());
        }

    }

    public static class DiskByAliasComparer implements Comparator<Disk>, Serializable
    {

        private static final long serialVersionUID = 7683690514569802083L;

        @Override
        public int compare(Disk x, Disk y)
        {
            String xAlias = x.getDiskAlias();
            String yAlias = y.getDiskAlias();

            if (xAlias == null)
            {
                return 1;
            }

            if (yAlias == null)
            {
                return -1;
            }

            return xAlias.compareTo(yAlias);
        }

    }

    public static class DiskModelByAliasComparer implements Comparator<DiskModel>, Serializable
    {
        private static final long serialVersionUID = -3838651062327707058L;

        @Override
        public int compare(DiskModel x, DiskModel y)
        {
            String xAlias = x.getDisk() != null ?
                    x.getDisk().getDiskAlias() : "";
            String yAlias = y.getDisk() != null ?
                    y.getDisk().getDiskAlias() : "";

            if (xAlias == null)
            {
                return 1;
            }

            if (yAlias == null)
            {
                return -1;
            }

            return xAlias.compareTo(yAlias);
        }
    }

    public static class SanTargetModelComparer implements Comparator<SanTargetModel>, Serializable
    {
        private static final long serialVersionUID = -5674954613952206979L;

        @Override
        public int compare(SanTargetModel x, SanTargetModel y)
        {
            return x.getName().compareTo(y.getName());
        }
    }

    public static class DiskImageByCreationDateComparer implements Comparator<DiskImage>, Serializable
    {

        private static final long serialVersionUID = -5909501177227219287L;

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            if (x.getCreationDate().before(y.getCreationDate()))
            {
                return -1;
            }

            if (x.getCreationDate().after(y.getCreationDate()))
            {
                return 1;
            }

            return 0;
        }

    }

    public static class SnapshotByCreationDateCommparer implements Comparator<Snapshot>, Serializable
    {

        private static final long serialVersionUID = -4063737182979806402L;

        @Override
        public int compare(Snapshot x, Snapshot y)
        {
            return x.getCreationDate().compareTo(y.getCreationDate());
        }

    }

    public static boolean isHostBelongsToAnyOfClusters(ArrayList<VDSGroup> clusters, VDS host)
    {
        for (VDSGroup cluster : clusters)
        {
            if (cluster.getId().equals(host.getVdsGroupId()))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if Any StorageDomain Is Master And Active
     *
     * @param sdl
     * @return
     */
    public static boolean isAnyStorageDomainIsMasterAndActive(List<StorageDomain> sdl)
    {
        for (StorageDomain a : sdl)
        {
            if (a.getStorageDomainType() == StorageDomainType.Master && a.getStatus() != null
                    && a.getStatus() == StorageDomainStatus.Active)
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isDataActiveStorageDomain(StorageDomain storageDomain)
    {
        boolean isData = storageDomain.getStorageDomainType().isDataDomain();

        boolean isActive = isActiveStorageDomain(storageDomain);

        return isData && isActive;
    }

    public static boolean isActiveStorageDomain(StorageDomain storageDomain)
    {
        boolean isActive = storageDomain.getStatus() != null &&
                storageDomain.getStatus() == StorageDomainStatus.Active;

        return isActive;
    }

    /**
     * Finds min Version by clusters list.
     *
     * @param source
     *            IList to look in
     * @return Version MinVersion
     */
    public static Version getMinVersionByClusters(List<VDSGroup> source)
    {
        Version minVersion = source != null && source.size() > 0 ? source.get(0).getCompatibilityVersion() : null;

        if (minVersion != null)
        {
            for (VDSGroup cluster : source)
            {
                minVersion =
                        cluster.getCompatibilityVersion().compareTo(minVersion) < 0 ? (Version) cluster.getCompatibilityVersion()
                                : minVersion;
            }
        }

        return minVersion;
    }

    /**
     * Check if storage_domains item with specified Guid exist in List
     *
     * @param items
     * @param id
     * @return
     */
    public static boolean isSDItemExistInList(ArrayList<StorageDomain> items, Guid id)
    {
        for (StorageDomain b : items)
        {
            if (b.getId().equals(id))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if VDSGroup item with specified id exist in List
     *
     * @param items
     * @param id
     * @return
     */
    public static boolean isClusterItemExistInList(List<VDSGroup> items, Guid id)
    {
        for (VDSGroup a : items)
        {
            if (id.equals(a.getId()))
            {
                return true;
            }
        }
        return false;
    }

    public static NetworkInterface findInterfaceByName(ArrayList<VdsNetworkInterface> items, String name)
    {
        for (NetworkInterface i : items)
        {
            if (ObjectUtils.objectsEqual(i.getName(), name))
            {
                return i;
            }
        }
        return null;
    }

    public static VdsNetworkInterface findInterfaceByNetworkName(ArrayList<VdsNetworkInterface> items, String name)
    {
        for (VdsNetworkInterface i : items)
        {
            if (ObjectUtils.objectsEqual(i.getNetworkName(), name))
            {
                return i;
            }
        }
        return null;
    }

    public static VdsNetworkInterface findInterfaceByIsBond(ArrayList<VdsNetworkInterface> items)
    {
        for (VdsNetworkInterface i : items)
        {
            if (i.getBonded() != null && i.getBonded())
            {
                return i;
            }
        }
        return null;
    }

    public static VdsNetworkInterface findInterfaceNetworkNameNotEmpty(ArrayList<VdsNetworkInterface> items)
    {
        for (VdsNetworkInterface i : items)
        {
            if (!StringHelper.isNullOrEmpty(i.getNetworkName()))
            {
                return i;
            }
        }
        return null;
    }

    public static ArrayList<VdsNetworkInterface> findAllInterfaceNetworkNameNotEmpty(ArrayList<VdsNetworkInterface> items)
    {
        ArrayList<VdsNetworkInterface> ret = new ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : items)
        {
            if (!StringHelper.isNullOrEmpty(i.getNetworkName()))
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static ArrayList<VdsNetworkInterface> findAllInterfaceBondNameIsEmpty(ArrayList<VdsNetworkInterface> items)
    {
        ArrayList<VdsNetworkInterface> ret = new ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : items)
        {
            if (StringHelper.isNullOrEmpty(i.getBondName()))
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static ArrayList<VdsNetworkInterface> findAllInterfaceVlanIdIsEmpty(ArrayList<VdsNetworkInterface> items)
    {
        ArrayList<VdsNetworkInterface> ret = new ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : items)
        {
            if (i.getVlanId() == null)
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static Network findManagementNetwork(List<Network> networks) {
        for (Network network : networks) {
            if (network.getCluster().isManagement()) {
                return network;
            }
        }
        return null;
    }

    public static Network findNetworkByName(ArrayList<Network> items, String name)
    {
        for (Network n : items)
        {
            if (ObjectUtils.objectsEqual(n.getName(), name))
            {
                return n;
            }
        }
        return null;
    }

    public static NetworkQoS findNetworkQosById(Iterable<NetworkQoS> items, Guid qosId) {
        for (NetworkQoS qos : items) {
            if (qos.getId().equals(qosId)) {
                return qos;
            }
        }
        return NetworkQoSModel.EMPTY_QOS;
    }

    public static HostNetworkQos findHostNetworkQosById(Iterable<HostNetworkQos> items, Guid qosId) {
        for (HostNetworkQos qos : items) {
            if (qos.getId().equals(qosId)) {
                return qos;
            }
        }
        return NetworkModel.EMPTY_HOST_NETWORK_QOS;
    }

    public static ArrayList<VDS> findAllVDSByPmEnabled(ArrayList<VDS> items)
    {
        ArrayList<VDS> ret = new ArrayList<VDS>();
        for (VDS i : items)
        {
            if (i.isPmEnabled())
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static SanTargetModel findSanTargetByNotIsConnected(ArrayList<SanTargetModel> items)
    {
        for (SanTargetModel i : items)
        {
            if (!i.getIsLoggedIn())
            {
                return i;
            }
        }
        return null;
    }

    public static ArrayList<StorageDomain> findAllStorageDomainsBySharedStatus(ArrayList<StorageDomain> items,
            StorageDomainSharedStatus status)
    {
        ArrayList<StorageDomain> ret = new ArrayList<StorageDomain>();
        for (StorageDomain i : items)
        {
            if (i.getStorageDomainSharedStatus() == status)
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static VdcReturnValueBase findVdcReturnValueByDescription(ArrayList<VdcReturnValueBase> items,
            String description)
    {
        for (VdcReturnValueBase i : items)
        {
            if (ObjectUtils.objectsEqual(i.getDescription(), description))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * Produces the set difference of two sequences by using the default equality
     */
    // comparer to compare values.
    /**
     * <typeparam name="TSource"></typeparam>
     *
     * @param first
     *            An System.Collections.Generic.IEnumerable<T> whose elements that are not also in second will be
     *            returned.
     * @param second
     *            An System.Collections.Generic.IEnumerable<T> whose elements that also occur in the first sequence will
     *            cause those elements to be removed from the returned sequence.
     * @return A sequence that contains the set difference of the elements of two sequences.
     */
    public static <TSource> ArrayList<TSource> except(ArrayList<TSource> first,
            ArrayList<TSource> second)
    {
        ArrayList<TSource> newIEnumerable = new ArrayList<TSource>();

        if (first != null && second != null)
        {
            for (TSource t : first)
            {
                if (!second.contains(t))
                {
                    newIEnumerable.add(t);
                }
            }
        }

        return second == null ? first : newIEnumerable;
    }

    public static int count(Iterable source)
    {
        int result = 0;
        for (Object item : source)
        {
            result++;
        }

        return result;
    }

    public static <TSource> TSource firstOrDefault(Iterable<TSource> source)
    {
        if (source != null) {
            for (TSource item : source)
            {
                return item;
            }
        }
        return null;
    }

    public static <TSource> boolean all(Iterable<TSource> source, IPredicate<TSource> predicate) {

        for (TSource item : source) {
            if (!predicate.match(item)) {
                return false;
            }
        }

        return true;
    }

    public static <TSource> Collection<TSource> where(Collection<TSource> source, IPredicate<TSource> predicate)
    {
        ArrayList<TSource> list = new ArrayList<TSource>();

        for (TSource item : source)
        {
            if (predicate.match(item))
            {
                list.add(item);
            }
        }

        return list;
    }

    public static Version selectHighestVersion(ArrayList<Version> versions)
    {
        Version retVersion = firstOrDefault(versions);
        for (Version version : versions)
        {
            if (version.compareTo(retVersion) > 0)
            {
                retVersion = version;
            }
        }
        return retVersion;
    }

    public static <TSource> TSource firstOrDefault(Iterable<TSource> source, IPredicate<TSource> predicate)
    {
        for (TSource item : source)
        {
            if (predicate.match(item))
            {
                return item;
            }
        }

        return null;
    }

    /**
     * Returns a new instance of list containing all items of the provided source.
     */
    public static <TSource> ArrayList<TSource> toList(Iterable<TSource> source)
    {
        ArrayList<TSource> list = new ArrayList<TSource>();
        for (TSource item : source)
        {
            list.add(item);
        }

        return list;
    }

    public static <TSource> ArrayList<TSource> distinct(ArrayList<TSource> source,
            IEqualityComparer<TSource> comparer)
    {
        ArrayList<TSource> list = new ArrayList<TSource>();
        for (TSource a : source)
        {
            boolean found = false;
            for (TSource b : list)
            {
                if (comparer.equals(a, b))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
            {
                list.add(a);
            }
        }

        return list;
    }

    public static <TResult> ArrayList<TResult> cast(Iterable source)
    {
        ArrayList<TResult> list = new ArrayList<TResult>();
        for (Object a : source)
        {
            TResult item = (TResult) a;
            list.add(item);
        }

        return list;
    }

    public static List concat(List... lists)
    {
        List result = new ArrayList<Object>();
        for (List list : lists)
        {
            for (Object item : list)
            {
                result.add(item);
            }
        }

        return result;
    }

    public static <T> ArrayList<T> union(ArrayList<ArrayList<T>> lists)
    {
        HashSet<T> set = new HashSet<T>();

        for (ArrayList<T> list : lists)
        {
            set.addAll(list);
        }

        return new ArrayList<T>(set);
    }

    public static <T> ArrayList<T> intersection(ArrayList<ArrayList<T>> lists)
    {
        ArrayList<T> result = new ArrayList<T>();

        if (lists != null && !lists.isEmpty()) {
            result.addAll(lists.get(0));
            for (ArrayList<T> list : lists) {
                result.retainAll(list);
            }
        }

        return result;
    }

    public static StorageDomain getStorageById(Guid storageId, ArrayList<StorageDomain> storageDomains) {
        for (StorageDomain storage : storageDomains) {
            if (storage.getId().equals(storageId)) {
                return storage;
            }
        }
        return null;
    }

    public static ArrayList<StorageDomain> getStorageDomainsByIds(ArrayList<Guid> storageIds,
            ArrayList<StorageDomain> storageDomains) {
        ArrayList<StorageDomain> list = new ArrayList<StorageDomain>();
        for (Guid storageId : storageIds) {
            StorageDomain storageDomain = getStorageById(storageId, storageDomains);
            if (storageDomain != null) {
                list.add(storageDomain);
            }
        }
        return list;
    }

    public static <T> ArrayList<EntityModel<T>> toEntityModelList(ArrayList<T> list)
    {
        ArrayList<EntityModel<T>> entityModelList = new ArrayList<EntityModel<T>>();

        if (list != null) {
            for (T item : list)
            {
                EntityModel<T> model = new EntityModel<T>();
                model.setEntity(item);
                entityModelList.add(model);
            }
        }

        return entityModelList;
    }

    public static ArrayList<DiskModel> filterDisksByType(ArrayList<DiskModel> diskModels, DiskStorageType type)
    {
        ArrayList<DiskModel> filteredList = new ArrayList<DiskModel>();

        if (diskModels != null) {
            for (DiskModel item : diskModels)
            {
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
            return Collections.EMPTY_LIST;
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
            EntityModel<Integer> sizeEntity = new EntityModel<Integer>();
            sizeEntity.setEntity((int) diskImage.getSizeInGigabytes());
            diskModel.setSize(sizeEntity);
            ListModel volumeList = new ListModel();
            volumeList.setItems((diskImage.getVolumeType() == VolumeType.Preallocated ?
                    new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] {VolumeType.Preallocated}))
                    : AsyncDataProvider.getInstance().getVolumeTypeList()));
            volumeList.setSelectedItem(diskImage.getVolumeType());
            diskModel.setVolumeType(volumeList);
        }

        diskModel.setDisk(disk);

        return diskModel;
    }

    public static ArrayList<DiskModel> disksToDiskModelList(List<Disk> disks) {
        ArrayList<DiskModel> diskModels = new ArrayList<DiskModel>();

        for (Disk disk : disks) {
            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModels.add(diskModel);
        }

        return diskModels;
    }

    public static Set<String> getDiskAliases(List<? extends Disk> disks) {
        Set<String> aliases = new HashSet<String>();
        for (Disk disk : disks) {
            aliases.add(disk.getDiskAlias());
        }
        return aliases;
    }

    public static List<DiskImage> imagesSubtract(Iterable<DiskImage> images, Iterable<DiskImage> imagesToSubtract) {
        List<DiskImage> subtract = new ArrayList<DiskImage>();
        for (DiskImage image : images) {
            if (Linq.getDiskImageById(image.getId(), imagesToSubtract) == null) {
                subtract.add(image);
            }
        }
        return subtract;
    }

    private static DiskImage getDiskImageById(Guid id, Iterable<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getId().equals(id)) {
                return diskImage;
            }
        }
        return null;
    }

    public final static class TimeZonePredicate implements IPredicate<TimeZoneModel>
    {
        private final String timeZone;

        public TimeZonePredicate(String timeZone)
        {
            this.timeZone = timeZone;
        }

        @Override
        public boolean match(TimeZoneModel source)
        {
            return ObjectUtils.objectsEqual(source.getTimeZoneKey(), timeZone);
        }
    }

    public final static class ServerCpuPredicate implements IPredicate<ServerCpu>
    {
        private final String cpuName;

        public ServerCpuPredicate(String cpuName)
        {
            this.cpuName = cpuName;
        }

        @Override
        public boolean match(ServerCpu source)
        {
            return ObjectUtils.objectsEqual(source.getCpuName(), cpuName);
        }
    }

    public final static class VersionPredicate implements IPredicate<Version>
    {
        private final Version version;

        public VersionPredicate(Version version)
        {
            this.version = version;
        }

        @Override
        public boolean match(Version source)
        {
            return source.equals(version);
        }
    }

    public final static class DataCenterWithClusterAccordingClusterPredicate implements IPredicate<DataCenterWithCluster> {

        private final Guid clusterId;

        public DataCenterWithClusterAccordingClusterPredicate(Guid clusterId) {
            this.clusterId = clusterId;
        }

        @Override
        public boolean match(DataCenterWithCluster source) {
            if (source.getCluster() == null) {
                return false;
            }

            return source.getCluster().getId().equals(clusterId);
        }

    }

    public final static class DataCenterWithClusterPredicate implements IPredicate<DataCenterWithCluster> {

        private final Guid dataCenterId;

        private final Guid clusterId;

        public DataCenterWithClusterPredicate(Guid dataCenterId, Guid clusterId) {
            this.dataCenterId = dataCenterId;
            this.clusterId = clusterId;
        }

        @Override
        public boolean match(DataCenterWithCluster source) {
            if (source.getDataCenter() == null || source.getCluster() == null) {
                return false;
            }

            return source.getDataCenter().getId().equals(dataCenterId) &&
                    source.getCluster().getId().equals(clusterId);
        }

    }

    public final static class DataCenterPredicate implements IPredicate<StoragePool>
    {
        private Guid id = Guid.Empty;

        public DataCenterPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean match(StoragePool source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class DataCenterStatusPredicate implements IPredicate<StoragePool>
    {
        private StoragePoolStatus status = StoragePoolStatus.values()[0];

        public DataCenterStatusPredicate(StoragePoolStatus status)
        {
            this.status = status;
        }

        @Override
        public boolean match(StoragePool source)
        {
            return source.getStatus() == status;
        }
    }

    public final static class DataCenterNotStatusPredicate implements IPredicate<StoragePool>
    {
        private StoragePoolStatus status = StoragePoolStatus.values()[0];

        public DataCenterNotStatusPredicate(StoragePoolStatus status)
        {
            this.status = status;
        }

        @Override
        public boolean match(StoragePool source)
        {
            return source.getStatus() != status;
        }
    }

    public final static class CanDoActionSucceedPredicate implements IPredicate<VdcReturnValueBase> {

        @Override
        public boolean match(VdcReturnValueBase source) {
            return source.getCanDoAction();
        }
    }

    public final static class ClusterPredicate implements IPredicate<VDSGroup>
    {
        private Guid id = Guid.Empty;

        public ClusterPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean match(VDSGroup source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class HostPredicate implements IPredicate<VDS>
    {
        private Guid id = Guid.Empty;

        public HostPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean match(VDS source)
        {
            return source.getId().equals(id);
        }
    }

    public final static class HostStatusPredicate implements IPredicate<VDS>
    {
        private VDSStatus status = VDSStatus.values()[0];

        public HostStatusPredicate(VDSStatus status)
        {
            this.status = status;
        }

        @Override
        public boolean match(VDS source)
        {
            return source.getStatus().equals(status);
        }
    }

    public final static class TemplatePredicate implements IPredicate<VmTemplate>
    {
        private Guid id = Guid.Empty;

        public TemplatePredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean match(VmTemplate source)
        {
            return source.getId().equals(id);
        }
    }

    public final static class TemplateWithVersionPredicate implements IPredicate<TemplateWithVersion> {
        private final Guid id;

        public TemplateWithVersionPredicate(Guid id) {
            this.id = id;
        }

        @Override
        public boolean match(TemplateWithVersion templateWithVersion) {
            return id.equals(templateWithVersion.getTemplateVersion().getId());
        }
    }

    public final static class StoragePredicate implements IPredicate<StorageDomain>
    {
        private Guid id = Guid.Empty;

        public StoragePredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean match(StorageDomain source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class StorageNamePredicate implements IPredicate<StorageDomain> {

        private final String name;

        public StorageNamePredicate(String name) {
            this.name = name;
        }

        @Override
        public boolean match(StorageDomain source) {
            return name.equals(source.getStorageName());
        }
    }

    public final static class LunPredicate implements IPredicate<LunModel>
    {
        private final LunModel lun;

        public LunPredicate(LunModel lun)
        {
            this.lun = lun;
        }

        @Override
        public boolean match(LunModel source)
        {
            return ObjectUtils.objectsEqual(source.getLunId(), lun.getLunId());
        }
    }

    public final static class TargetPredicate implements IPredicate<SanTargetModel>
    {
        private final SanTargetModel target;

        public TargetPredicate(SanTargetModel target)
        {
            this.target = target;
        }

        @Override
        public boolean match(SanTargetModel source)
        {
            return ObjectUtils.objectsEqual(source.getName(), target.getName())
                    && ObjectUtils.objectsEqual(source.getAddress(), target.getAddress())
                    && ObjectUtils.objectsEqual(source.getPort(), target.getPort());
        }
    }

    public final static class DbUserPredicate implements IPredicate<DbUser>
    {
        private final DbUser target;

        public DbUserPredicate(DbUser target)
        {
            this.target = target;
        }

        @Override
        public boolean match(DbUser source)
        {
            String targetName = target.getLoginName();
            if (!StringHelper.isNullOrEmpty(targetName)) {
                targetName = targetName.toLowerCase();
            }
            return ObjectUtils.objectsEqual(source.getDomain(), target.getDomain())
                    && (StringHelper.isNullOrEmpty(target.getLoginName())
                    || "*".equals(target.getLoginName()) //$NON-NLS-1$
                    || source.getLoginName().toLowerCase().startsWith(targetName));
        }
    }

    public final static class DbGroupPredicate implements IPredicate<DbGroup>
    {
        private final DbGroup target;

        public DbGroupPredicate(DbGroup target)
        {
            this.target = target;
        }

        @Override
        public boolean match(DbGroup source)
        {
            String groupName = source.getName().toLowerCase();
            String targetName = target.getName();
            if (!StringHelper.isNullOrEmpty(targetName)) {
                targetName = targetName.toLowerCase();
            } else if (targetName == null) {
                targetName = "";
            }
            int lastIndex = groupName.lastIndexOf("/"); //$NON-NLS-1$
            if (lastIndex != -1) {
                groupName = groupName.substring(lastIndex+1);
            }
            return ObjectUtils.objectsEqual(source.getDomain(), target.getDomain())
                    && (StringHelper.isNullOrEmpty(target.getName())
                    || "*".equals(target.getName()) //$NON-NLS-1$
                    || groupName.startsWith(targetName))
                    || source.getName().toLowerCase().startsWith(targetName);
        }
    }

    public final static class NetworkSameProviderPredicate implements IPredicate<Provider> {

        private final Network network;

        public NetworkSameProviderPredicate(Network network) {
            this.network = network;
        }

        @Override
        public boolean match(Provider provider) {
            return network.isExternal() ? provider.getId().equals(network.getProvidedBy().getProviderId()) : false;
        }

    }

    public interface IPredicate<TSource>
    {
        boolean match(TSource source);
    }

    public final static class RoleNameComparer implements Comparator<Role>, Serializable {
        private static final long serialVersionUID = -8611835505533367419L;

        @Override
        public int compare(Role left, Role right) {
            return left.getname().compareTo(right.getname());
        }
    }



    public final static class VmInterfaceComparer implements Comparator<VmNetworkInterface>, Serializable {
        LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(VmNetworkInterface nic1, VmNetworkInterface nic2) {
            return lexoNumeric.compare(nic1.getName(), nic2.getName());
        }
    }

    public final static class InterfaceComparator implements Comparator<VdsNetworkInterface>, Serializable {

        private static final long serialVersionUID = -6806871048546270786L;
        LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(VdsNetworkInterface bond1, VdsNetworkInterface bond2) {
            return lexoNumeric.compare(bond1.getName(), bond2.getName());
        }

    }

    public final static class StorageDomainComparator implements Comparator<StorageDomain>, Serializable {
        private static final long serialVersionUID = 990203400356561587L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(StorageDomain domain1, StorageDomain domain2) {
            return lexoNumeric.compare(domain1.getName(), domain2.getName());
        }
    }

    public final static class StorageDomainByPoolNameComparator implements Comparator<StorageDomain>, Serializable {
        private static final long serialVersionUID = 990203400356561666L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(StorageDomain domain1, StorageDomain domain2) {
            return lexoNumeric.compare(domain1.getStoragePoolName(), domain2.getStoragePoolName());
        }
    }

    public final static class VDSGroupComparator implements Comparator<VDSGroup>, Serializable {
        private static final long serialVersionUID = 990203400356561587L;
        private final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(VDSGroup cluster1, VDSGroup cluster2) {
            return lexoNumeric.compare(cluster1.getName(), cluster2.getName());
        }
    }

    public final static class NetworkInClusterComparator implements Comparator<Network>, Serializable {

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

    public final static class VnicProfileViewComparator implements Comparator<VnicProfileView>, Serializable {

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

    public final static class ClusterNetworkModelComparator implements Comparator<ClusterNetworkModel>, Serializable {

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

    public final static class ExternalNetworkComparator implements Comparator<ExternalNetwork>, Serializable {
        private static final long serialVersionUID = 4987035011384708563L;
        private final NameableComparator comparator = new NameableComparator();

        @Override
        public int compare(ExternalNetwork net1, ExternalNetwork net2) {
            return comparator.compare(net1.getNetwork(), net2.getNetwork());
        };
    }

    public final static class ProviderComparator implements Comparator<Provider>, Serializable {
        private static final long serialVersionUID = 627759940118704128L;

        @Override
        public int compare(Provider p1, Provider p2) {
            return LexoNumericComparator.comp(p1.getName(), p2.getName());
        }
    }

    public final static class VmComparator implements Comparator<VM>, Serializable {

        @Override
        public int compare(VM v1, VM v2) {
            return LexoNumericComparator.comp(v1.getName(), v2.getName());
        }
    }

    public final static class VmTemplateComparator implements Comparator<VmTemplate>, Serializable {

        @Override
        public int compare(VmTemplate t1, VmTemplate t2) {
            return LexoNumericComparator.comp(t1.getName(), t2.getName());
        }
    }

    public final static <T extends Disk> Collection<T> filterNonSnapableDisks(
            Collection<Disk> source) {
        return (Collection<T>) where(source, new IPredicate<Disk>() {
            @Override
            public boolean match(Disk source) {
                return source.isAllowSnapshot();
            }
        });
    }

    public final static <T extends Disk> Collection<T> filterDisksByStorageType(
            Collection<Disk> source, final DiskStorageType diskStorageType) {
        return (Collection<T>) where(source, new IPredicate<Disk>() {
            @Override
            public boolean match(Disk source) {
                return source.getDiskStorageType() == diskStorageType;
            }
        });
    }

    public final static Collection<Provider> filterProvidersByProvidedType(Collection<Provider> source,
            final VdcObjectType type) {
        return where(source, new IPredicate<Provider>() {

            @Override
            public boolean match(Provider provider) {
                return provider.getType().getProvidedTypes().contains(type);
            }
        });
    }

    public final static class ProviderTypeComparator implements Comparator<ProviderType>, Serializable {
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
    public final static class ClusterPolicyComparator implements Comparator<ClusterPolicy>, Serializable {
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
    public final static class PolicyUnitComparator implements Comparator<PolicyUnit>, Serializable {
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

    public final static class SharedMacPoolComparator implements Comparator<MacPool>, Serializable {

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

    public final static class ImportEntityComparator<T> implements Comparator<ImportEntityData<T>>, Serializable {
        final LexoNumericComparator lexoNumeric = new LexoNumericComparator();

        @Override
        public int compare(ImportEntityData<T> entity1, ImportEntityData<T> entity2) {
            return lexoNumeric.compare(entity1.getName(), entity2.getName());
        }
    }
}
