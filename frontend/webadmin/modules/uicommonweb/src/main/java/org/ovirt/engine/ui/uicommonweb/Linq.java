package org.ovirt.engine.ui.uicommonweb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDomainModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicompat.DateTimeUtils;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public final class Linq
{
    public static class AuditLogComparer implements Comparator<AuditLog>
    {
        @Override
        public int compare(AuditLog x, AuditLog y)
        {
            long xid = x.getaudit_log_id();
            long yid = y.getaudit_log_id();

            return (new Long(xid)).compareTo(yid);
        }
    }

    public static class VmAndPoolByNameComparer implements Comparator
    {
        @Override
        public int compare(Object x, Object y)
        {
            return GetValue(x).compareTo(GetValue(y));
        }

        private String GetValue(Object obj)
        {
            if (obj instanceof VM)
            {
                return ((VM) obj).getVmName();
            }
            if (obj instanceof VmPool)
            {
                return ((VmPool) obj).getVmPoolName();
            }

            throw new NotImplementedException();
        }
    }

    /**
     * Checks if host belongs to any of clusters from list.
     *
     * @param clusters
     * @param host
     * @return
     */
    public static class VdsGroupByNameComparer implements Comparator<VDSGroup>
    {

        @Override
        public int compare(VDSGroup x, VDSGroup y)
        {
            return x.getname().compareTo(y.getname());
        }

    }

    public static class VmTemplateByNameComparer implements Comparator<VmTemplate>
    {

        @Override
        public int compare(VmTemplate x, VmTemplate y)
        {
            return x.getname().compareTo(y.getname());
        }

    }

    public static class DiskImageByLastModifiedComparer implements Comparator<DiskImage>
    {

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            if (x.getlastModified().before(y.getlastModified()))
            {
                return -1;
            }

            if (x.getlastModified().after(y.getlastModified()))
            {
                return 1;
            }

            return 0;
        }

    }

    public static class DiskImageByLastModifiedTimeOfDayComparer implements Comparator<DiskImage>
    {

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            return DateTimeUtils.getTimeOfDay(x.getlastModified())
                    .compareTo(DateTimeUtils.getTimeOfDay(y.getlastModified()));
        }

    }

    public static class StorageDomainByNameComparer implements Comparator<StorageDomain>
    {

        @Override
        public int compare(StorageDomain x, StorageDomain y)
        {
            return x.getStorageName().compareTo(y.getStorageName());
        }

    }

    public static class StorageDomainModelByNameComparer implements Comparator<StorageDomainModel>
    {

        @Override
        public int compare(StorageDomainModel x, StorageDomainModel y)
        {
            return x.getStorageDomain().getStorageName().compareTo(y.getStorageDomain().getStorageName());
        }

    }

    public static class DiskByAliasComparer implements Comparator<Disk>
    {

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

    public static class DiskModelByNameComparer implements Comparator<DiskModel>
    {
        @Override
        public int compare(DiskModel x, DiskModel y)
        {
            return x.getName().compareTo(y.getName());
        }
    }

    public static class DiskModelByAliasComparer implements Comparator<DiskModel>
    {
        @Override
        public int compare(DiskModel x, DiskModel y)
        {
            String xAlias = x.getDisk() != null ?
                    x.getDisk().getDiskAlias() : x.getDisk().getDiskAlias();
            String yAlias = y.getDisk() != null ?
                    y.getDisk().getDiskAlias() : y.getDisk().getDiskAlias();

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

    public static class NetworkByNameComparer implements Comparator<Network>
    {
        @Override
        public int compare(Network x, Network y)
        {
            return x.getName().compareTo(y.getName());
        }
    }

    public static class SanTargetModelComparer implements Comparator<SanTargetModel>
    {
        @Override
        public int compare(SanTargetModel x, SanTargetModel y)
        {
            return x.getName().compareTo(y.getName());
        }
    }

    public static class CaseInsensitiveComparer implements Comparator<String>
    {

        @Override
        public int compare(String str1, String str2)
        {
            return str1.toLowerCase().compareTo(str2.toLowerCase());
        }

    }

    public static class DiskImageByCreationDateComparer implements Comparator<DiskImage>
    {

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            if (x.getcreation_date().before(y.getcreation_date()))
            {
                return -1;
            }

            if (x.getcreation_date().after(y.getcreation_date()))
            {
                return 1;
            }

            return 0;
        }

    }

    public static class RpmVersionComparer implements Comparator<RpmVersion> {

        @Override
        public int compare(RpmVersion x, RpmVersion y) {
            return x.compareTo(y);
        }
    }

    public static class SnapshotByCreationDateCommparer implements Comparator<Snapshot>
    {

        @Override
        public int compare(Snapshot x, Snapshot y)
        {
            return x.getCreationDate().compareTo(y.getCreationDate());
        }

    }

    public static boolean IsHostBelongsToAnyOfClusters(ArrayList<VDSGroup> clusters, VDS host)
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
     * Checks if Any StorageDomain Is Matser And Active
     *
     * @param sdl
     * @return
     */
    public static boolean IsAnyStorageDomainIsMatserAndActive(List<StorageDomain> sdl)
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

    public static boolean IsDataActiveStorageDomain(StorageDomain storageDomain)
    {
        boolean isData = storageDomain.getStorageDomainType() == StorageDomainType.Data ||
                storageDomain.getStorageDomainType() == StorageDomainType.Master;

        boolean isActive = IsActiveStorageDomain(storageDomain);

        return isData && isActive;
    }

    public static boolean IsActiveStorageDomain(StorageDomain storageDomain)
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
    public static Version GetMinVersionByClusters(List<VDSGroup> source)
    {
        Version minVersion = source != null && source.size() > 0 ? source.get(0).getcompatibility_version() : null;

        if (minVersion != null)
        {
            for (VDSGroup cluster : source)
            {
                minVersion =
                        cluster.getcompatibility_version().compareTo(minVersion) < 0 ? (Version) cluster.getcompatibility_version()
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
    public static boolean IsSDItemExistInList(ArrayList<StorageDomain> items, Guid id)
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
    public static boolean IsClusterItemExistInList(List<VDSGroup> items, Guid id)
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

    public static NetworkInterface FindInterfaceByName(ArrayList<NetworkInterface> items, String name)
    {
        for (NetworkInterface i : items)
        {
            if (StringHelper.stringsEqual(i.getName(), name))
            {
                return i;
            }
        }
        return null;
    }

    public static ArrayList<NetworkInterface> VdsNetworkInterfaceListToBase(ArrayList<VdsNetworkInterface> items)
    {
        ArrayList<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        for (VdsNetworkInterface item : items)
        {
            networkInterfaces.add(item);
        }

        return networkInterfaces;
    }

    public static ArrayList<NetworkInterface> VmNetworkInterfaceListToBase(ArrayList<VmNetworkInterface> items)
    {
        ArrayList<NetworkInterface> networkInterfaces = new ArrayList<NetworkInterface>();
        for (VmNetworkInterface item : items)
        {
            networkInterfaces.add(item);
        }

        return networkInterfaces;
    }

    public static NetworkInterface FindInterfaceByNetworkName(ArrayList<NetworkInterface> items, String name)
    {
        for (NetworkInterface i : items)
        {
            if (StringHelper.stringsEqual(i.getNetworkName(), name))
            {
                return i;
            }
        }
        return null;
    }

    public static VdsNetworkInterface FindInterfaceByIsBond(ArrayList<VdsNetworkInterface> items)
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

    public static NetworkInterface FindInterfaceNetworkNameNotEmpty(ArrayList<NetworkInterface> items)
    {
        for (NetworkInterface i : items)
        {
            if (!StringHelper.isNullOrEmpty(i.getNetworkName()))
            {
                return i;
            }
        }
        return null;
    }

    public static ArrayList<NetworkInterface> FindAllInterfaceNetworkNameNotEmpty(ArrayList<NetworkInterface> items)
    {
        ArrayList<NetworkInterface> ret = new ArrayList<NetworkInterface>();
        for (NetworkInterface i : items)
        {
            if (!StringHelper.isNullOrEmpty(i.getNetworkName()))
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static ArrayList<VdsNetworkInterface> FindAllInterfaceBondNameIsEmpty(ArrayList<VdsNetworkInterface> items)
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

    public static ArrayList<VdsNetworkInterface> FindAllInterfaceVlanIdIsEmpty(ArrayList<VdsNetworkInterface> items)
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

    public static Network FindNetworkByName(ArrayList<Network> items, String name)
    {
        for (Network n : items)
        {
            if (StringHelper.stringsEqual(n.getName(), name))
            {
                return n;
            }
        }
        return null;
    }

    public static ArrayList<VDS> FindAllVDSByPmEnabled(ArrayList<VDS> items)
    {
        ArrayList<VDS> ret = new ArrayList<VDS>();
        for (VDS i : items)
        {
            if (i.getpm_enabled())
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static SanTargetModel FindSanTargetByNotIsConnected(ArrayList<SanTargetModel> items)
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

    public static ArrayList<StorageDomain> FindAllStorageDomainsBySharedStatus(ArrayList<StorageDomain> items,
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

    public static VdcReturnValueBase FindVdcReturnValueByDescription(ArrayList<VdcReturnValueBase> items,
            String description)
    {
        for (VdcReturnValueBase i : items)
        {
            if (StringHelper.stringsEqual(i.getDescription(), description))
            {
                return i;
            }
        }
        return null;
    }

    /**
     * Determines if all elements of source satisfy a condition.
     */
    // public static bool All<TSource>(IEnumerable<TSource> source, Func<TSource, bool> predicate)
    // {
    // foreach (TSource item in source)
    // if (!predicate(item))
    // return false;

    // return true;
    // }

    /**
     * Find min TSource by TKey.
     *
     * <typeparam name="TSource">Source type.</typeparam> <typeparam name="TKey">Min TSource to be found according to
     * this type.</typeparam>
     *
     * @param source
     *            IEnumerable to iterate for min TSource.
     * @param selector
     *            Param by which to search for min TSource.
     * @return min(IEnumerable<TSource>)
     */
    // public static TSource Min<TSource, TKey>(IEnumerable<TSource> source, Func<TSource, TKey> selector)
    // {
    // using (IEnumerator<TSource> sIterator = source.GetEnumerator())
    // {
    // if (!sIterator.MoveNext()) return default(TSource);

    // IComparer<TKey> comparer = Comparer<TKey>.Default;
    // TKey minKey = selector(sIterator.Current);
    // TSource minVal = sIterator.Current;

    // while (sIterator.MoveNext())
    // {
    // TSource minCandidate = sIterator.Current;
    // TKey minCandidateKeyValue = selector(minCandidate);
    // if (comparer.compare(minCandidateKeyValue, minKey) < 0)
    // {
    // minKey = minCandidateKeyValue;
    // minVal = minCandidate;
    // }
    // }
    // return minVal;
    // }
    // }

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
    public static <TSource> ArrayList<TSource> Except(ArrayList<TSource> first,
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

    /**
     * OrderBy
     *
     * <typeparam name="TSource">TSource of IEnumerable to sort.</typeparam> <typeparam name="TKey">TKey of IEnumerable
     * to sort by.</typeparam>
     *
     * @param source
     *            IEnumerable to sort.
     * @param keySelector
     *            Key of IEnumerable to sort by.
     * @return
     */
    // public static IEnumerable<TSource> OrderBy<TSource, TKey>(IEnumerable<TSource> source, Func<TSource, TKey>
    // keySelector)
    // {
    // SortedList<TKey, TSource> sl = new SortedList<TKey, TSource>();

    // foreach (TSource item in source)
    // {
    // sl.Add(keySelector(item), item);
    // }

    // return Cast<TSource>(sl.Values);
    // }

    public static int Count(Iterable source)
    {
        int result = 0;
        for (Object item : source)
        {
            result++;
        }

        return result;
    }

    public static <TSource> TSource FirstOrDefault(Iterable<TSource> source)
    {
        for (TSource item : source)
        {
            return item;
        }

        return null;
    }

    public static <TSource> boolean All(Iterable<TSource> source, IPredicate<TSource> predicate) {

        for (TSource item : source) {
            if (!predicate.Match(item)) {
                return false;
            }
        }

        return true;
    }

    public static <TSource> Iterable<TSource> Where(Iterable<TSource> source, IPredicate<TSource> predicate)
    {
        ArrayList<TSource> list = new ArrayList<TSource>();

        for (TSource item : source)
        {
            if (predicate.Match(item))
            {
                list.add(item);
            }
        }

        return list;
    }

    public static Version SelectHighestVersion(ArrayList<Version> versions)
    {
        Version retVersion = FirstOrDefault(versions);
        for (Version version : versions)
        {
            if (version.compareTo(retVersion) > 0)
            {
                retVersion = version;
            }
        }
        return retVersion;
    }

    public static <TSource> TSource FirstOrDefault(Iterable<TSource> source, IPredicate<TSource> predicate)
    {
        for (TSource item : source)
        {
            if (predicate.Match(item))
            {
                return item;
            }
        }

        return null;
    }

    /**
     * Returns a new instance of list containing all items of the provided source.
     */
    public static <TSource> ArrayList<TSource> ToList(Iterable<TSource> source)
    {
        ArrayList<TSource> list = new ArrayList<TSource>();
        for (TSource item : source)
        {
            list.add(item);
        }

        return list;
    }

    // public static TSource First<TSource>(IEnumerable<TSource> source, Func<TSource, bool> predicate)
    // {
    // foreach (TSource item in source)
    // {
    // if (predicate(item))
    // {
    // return item;
    // }
    // }

    // throw new InvalidOperationException();
    // }

    // public static bool Any<TSource>(IEnumerable<TSource> source, Func<TSource, bool> predicate)
    // {
    // foreach (TSource item in source)
    // {
    // if (predicate(item))
    // {
    // return true;
    // }
    // }

    // return false;
    // }

    public static <TSource> ArrayList<TSource> Distinct(ArrayList<TSource> source,
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

    // public static IDictionary<TKey, IList<TSource>> GroupBy<TSource, TKey>(IEnumerable<TSource> source, Func<TSource,
    // TKey> keySelector)
    // {
    // IDictionary<TKey, IList<TSource>> bag = new Dictionary<TKey, IList<TSource>>();
    // foreach (TSource item in source)
    // {
    // TKey key = keySelector(item);
    // if (!bag.ContainsKey(key))
    // {
    // bag.Add(key, new List<TSource>());
    // }

    // IList<TSource> list = bag[key];
    // list.Add(item);
    // }

    // return bag;
    // }

    public static <TResult> ArrayList<TResult> Cast(Iterable source)
    {
        ArrayList<TResult> list = new ArrayList<TResult>();
        for (Object a : source)
        {
            TResult item = (TResult) a;
            list.add(item);
        }

        return list;
    }

    public static <T extends Comparable<T>> ArrayList<T> OrderByDescending(List<T> source)
    {
        ArrayList<T> list = new ArrayList<T>();

        ArrayList<T> sorted = new ArrayList<T>(source);
        Collections.sort(sorted);

        for (int i = sorted.size(); i > 0; i--)
        {
            list.add(sorted.get(i - 1));
        }

        return list;
    }

    public static <T> ArrayList<T> OrderByDescending(List<T> source, Comparator<T> comparer)
    {
        ArrayList<T> list = new ArrayList<T>();

        ArrayList<T> sorted = new ArrayList<T>(source);
        Collections.sort(sorted, comparer);

        for (int i = sorted.size(); i > 0; i--)
        {
            list.add(sorted.get(i - 1));
        }

        return list;
    }

    /**
     * Sorts a not typed list. Allows to do a sort on a list containing elements of different types.
     */
    public static void Sort(List source, Comparator comparer) {
        Collections.sort(source, comparer);
    }

    public static List Concat(List... lists)
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

    public static <T> ArrayList<T> Union(ArrayList<ArrayList<T>> lists)
    {
        HashSet<T> set = new HashSet<T>();

        for (ArrayList<T> list : lists)
        {
            set.addAll(list);
        }

        return new ArrayList<T>(set);
    }

    public static <T> ArrayList<T> Intersection(ArrayList<ArrayList<T>> lists)
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
                list.add(getStorageById(storageId, storageDomains));
            }
        }
        return list;
    }

    public static ArrayList<StorageDomain> getStorageDomainsDisjoint(ArrayList<DiskModel> disks,
            ArrayList<StorageDomain> storageDomains) {
        ArrayList<ArrayList<StorageDomain>> storageDomainslists = new ArrayList<ArrayList<StorageDomain>>();
        for (DiskModel diskModel : disks) {
            ArrayList<StorageDomain> list =
                    getStorageDomainsByIds(((DiskImage) diskModel.getDisk()).getstorage_ids(), storageDomains);

            storageDomainslists.add(list);
        }

        return Intersection(storageDomainslists);
    }

    public static <T> ArrayList<EntityModel> ToEntityModelList(ArrayList<T> list)
    {
        ArrayList<EntityModel> entityModelList = new ArrayList<EntityModel>();

        if (list != null) {
            for (Object item : list)
            {
                EntityModel model = new EntityModel();
                model.setEntity(item);
                entityModelList.add(model);
            }
        }

        return entityModelList;
    }

    public static ArrayList<DiskModel> FilterDisksByType(ArrayList<DiskModel> diskModels, DiskStorageType type)
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

    public static DiskModel DiskToModel(Disk disk) {
        DiskModel diskModel = new DiskModel();
        diskModel.setIsNew(true);
        diskModel.getAlias().setEntity(disk.getDiskAlias());

        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) disk;
            EntityModel sizeEntity = new EntityModel();
            sizeEntity.setEntity(diskImage.getSizeInGigabytes());
            diskModel.setSize(sizeEntity);
            ListModel volumeList = new ListModel();
            volumeList.setItems((diskImage.getvolume_type() == VolumeType.Preallocated ?
                    new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                    : AsyncDataProvider.GetVolumeTypeList()));
            volumeList.setSelectedItem(diskImage.getvolume_type());
            diskModel.setVolumeType(volumeList);
        }

        diskModel.setDisk(disk);

        return diskModel;
    }

    public static ArrayList<DiskModel> DisksToDiskModelList(ArrayList<Disk> disks) {
        ArrayList<DiskModel> diskModels = new ArrayList<DiskModel>();

        for (Disk disk : disks) {
            DiskModel diskModel = new DiskModel();
            diskModel.setDisk(disk);
            diskModels.add(diskModel);
        }

        return diskModels;
    }

    public final static class TimeZonePredicate implements IPredicate<Map.Entry<String, String>>
    {
        private final String timeZone;

        public TimeZonePredicate(String timeZone)
        {
            this.timeZone = timeZone;
        }

        @Override
        public boolean Match(Map.Entry<String, String> source)
        {
            return StringHelper.stringsEqual(source.getKey(), timeZone);
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
        public boolean Match(ServerCpu source)
        {
            return StringHelper.stringsEqual(source.getCpuName(), cpuName);
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
        public boolean Match(Version source)
        {
            return source.equals(version);
        }
    }

    public final static class DataCenterPredicate implements IPredicate<storage_pool>
    {
        private Guid id = new Guid();

        public DataCenterPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(storage_pool source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class DataCenterStatusPredicate implements IPredicate<storage_pool>
    {
        private StoragePoolStatus status = StoragePoolStatus.values()[0];

        public DataCenterStatusPredicate(StoragePoolStatus status)
        {
            this.status = status;
        }

        @Override
        public boolean Match(storage_pool source)
        {
            return source.getstatus() == status;
        }
    }

    public final static class DataCenterNotStatusPredicate implements IPredicate<storage_pool>
    {
        private StoragePoolStatus status = StoragePoolStatus.values()[0];

        public DataCenterNotStatusPredicate(StoragePoolStatus status)
        {
            this.status = status;
        }

        @Override
        public boolean Match(storage_pool source)
        {
            return source.getstatus() != status;
        }
    }


    public final static class CanDoActionSucceedPredicate implements IPredicate<VdcReturnValueBase> {

        @Override
        public boolean Match(VdcReturnValueBase source) {
            return source.getCanDoAction();
        }
    }

    public final static class ClusterPredicate implements IPredicate<VDSGroup>
    {
        private Guid id = new Guid();

        public ClusterPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(VDSGroup source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class HostPredicate implements IPredicate<VDS>
    {
        private Guid id = new Guid();

        public HostPredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(VDS source)
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
        public boolean Match(VDS source)
        {
            return source.getStatus().equals(status);
        }
    }

    public final static class TemplatePredicate implements IPredicate<VmTemplate>
    {
        private Guid id = new Guid();

        public TemplatePredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(VmTemplate source)
        {
            return source.getId().equals(id);
        }
    }

    public final static class StoragePredicate implements IPredicate<StorageDomain>
    {
        private Guid id = new Guid();

        public StoragePredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(StorageDomain source)
        {
            return id.equals(source.getId());
        }
    }

    public final static class StorageNamePredicate implements IPredicate<StorageDomain> {

        private String name;

        public StorageNamePredicate(String name) {
            this.name = name;
        }

        @Override
        public boolean Match(StorageDomain source) {
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
        public boolean Match(LunModel source)
        {
            return StringHelper.stringsEqual(source.getLunId(), lun.getLunId());
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
        public boolean Match(SanTargetModel source)
        {
            return StringHelper.stringsEqual(source.getName(), target.getName())
                    && StringHelper.stringsEqual(source.getAddress(), target.getAddress())
                    && StringHelper.stringsEqual(source.getPort(), target.getPort());
        }
    }

    public interface IPredicate<TSource>
    {
        boolean Match(TSource source);
    }
}
