package org.ovirt.engine.ui.uicommonweb;

import java.util.Collections;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.NetworkInterface;
import org.ovirt.engine.core.common.businessentities.ServerCpu;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanTargetModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotModel;
import org.ovirt.engine.ui.uicompat.DateTimeUtils;
import org.ovirt.engine.ui.uicompat.IEqualityComparer;

@SuppressWarnings("unused")
public final class Linq
{
    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class AuditLogComparer implements java.util.Comparator<AuditLog>
    {
        @Override
        public int compare(AuditLog x, AuditLog y)
        {
            long xid = x.getaudit_log_id();
            long yid = y.getaudit_log_id();

            return (new Long(xid)).compareTo(yid);
        }
    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class VmAndPoolByNameComparer implements java.util.Comparator
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
                return ((VM) obj).getvm_name();
            }
            if (obj instanceof vm_pools)
            {
                return ((vm_pools) obj).getvm_pool_name();
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
    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class VdsGroupByNameComparer implements java.util.Comparator<VDSGroup>
    {

        @Override
        public int compare(VDSGroup x, VDSGroup y)
        {
            return x.getname().compareTo(y.getname());
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class VmTemplateByNameComparer implements java.util.Comparator<VmTemplate>
    {

        @Override
        public int compare(VmTemplate x, VmTemplate y)
        {
            return x.getname().compareTo(y.getname());
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class DiskImageByLastModifiedComparer implements java.util.Comparator<DiskImage>
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

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class DiskImageByLastModifiedTimeOfDayComparer implements java.util.Comparator<DiskImage>
    {

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            return DateTimeUtils.getTimeOfDay(x.getlastModified())
                    .compareTo(DateTimeUtils.getTimeOfDay(y.getlastModified()));
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class SnapshotModelDateComparer implements java.util.Comparator<SnapshotModel>
    {

        @Override
        public int compare(SnapshotModel x, SnapshotModel y)
        {
            if (x.getDate() == null)
            {
                return 1;
            }
            if (y.getDate() == null)
            {
                return -1;
            }
            return x.getDate().compareTo(y.getDate());
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class StorageDomainByNameComparer implements java.util.Comparator<storage_domains>
    {

        @Override
        public int compare(storage_domains x, storage_domains y)
        {
            return x.getstorage_name().compareTo(y.getstorage_name());
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class DiskByInternalDriveMappingComparer implements java.util.Comparator<DiskImage>
    {

        @Override
        public int compare(DiskImage x, DiskImage y)
        {
            return x.getinternal_drive_mapping().compareTo(y.getinternal_drive_mapping());
        }

    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class NetworkByNameComparer implements java.util.Comparator<network>
    {
        @Override
        public int compare(network x, network y)
        {
            return x.getname().compareTo(y.getname());
        }
    }

    public static class SanTargetModelComparer implements java.util.Comparator<SanTargetModel>
    {
        @Override
        public int compare(SanTargetModel x, SanTargetModel y)
        {
            return x.getName().compareTo(y.getName());
        }
    }

    // C# TO JAVA CONVERTER TODO TASK: The interface type was changed to the closest equivalent Java type, but the
    // methods implemented will need adjustment:
    public static class CaseInsensitiveComparer implements java.util.Comparator<String>
    {

        @Override
        public int compare(String str1, String str2)
        {
            return str1.toLowerCase().compareTo(str2.toLowerCase());
        }

    }

    public static boolean IsHostBelongsToAnyOfClusters(java.util.ArrayList<VDSGroup> clusters, VDS host)
    {
        for (VDSGroup cluster : clusters)
        {
            if (cluster.getID().equals(host.getvds_group_id()))
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
    public static boolean IsAnyStorageDomainIsMatserAndActive(java.util.List<storage_domains> sdl)
    {
        for (storage_domains a : sdl)
        {
            if (a.getstorage_domain_type() == StorageDomainType.Master && a.getstatus() != null
                    && a.getstatus() == StorageDomainStatus.Active)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds min Version by clusters list.
     *
     * @param source
     *            IList to look in
     * @return Version MinVersion
     */
    public static Version GetMinVersionByClusters(java.util.List<VDSGroup> source)
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
    public static boolean IsSDItemExistInList(java.util.ArrayList<storage_domains> items, Guid id)
    {
        for (storage_domains b : items)
        {
            if (b.getid().equals(id))
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
    public static boolean IsClusterItemExistInList(java.util.List<VDSGroup> items, Guid id)
    {
        for (VDSGroup a : items)
        {
            if (id.equals(a.getID()))
            {
                return true;
            }
        }
        return false;
    }

    public static NetworkInterface FindInterfaceByName(java.util.ArrayList<NetworkInterface> items, String name)
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

    public static java.util.ArrayList<NetworkInterface> VdsNetworkInterfaceListToBase(java.util.ArrayList<VdsNetworkInterface> items)
    {
        java.util.ArrayList<NetworkInterface> networkInterfaces = new java.util.ArrayList<NetworkInterface>();
        for (VdsNetworkInterface item : items)
        {
            networkInterfaces.add(item);
        }

        return networkInterfaces;
    }

    public static java.util.ArrayList<NetworkInterface> VmNetworkInterfaceListToBase(java.util.ArrayList<VmNetworkInterface> items)
    {
        java.util.ArrayList<NetworkInterface> networkInterfaces = new java.util.ArrayList<NetworkInterface>();
        for (VmNetworkInterface item : items)
        {
            networkInterfaces.add(item);
        }

        return networkInterfaces;
    }

    public static NetworkInterface FindInterfaceByNetworkName(java.util.ArrayList<NetworkInterface> items, String name)
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

    public static VdsNetworkInterface FindInterfaceByIsBond(java.util.ArrayList<VdsNetworkInterface> items)
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

    public static NetworkInterface FindInterfaceNetworkNameNotEmpty(java.util.ArrayList<NetworkInterface> items)
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

    public static java.util.ArrayList<NetworkInterface> FindAllInterfaceNetworkNameNotEmpty(java.util.ArrayList<NetworkInterface> items)
    {
        java.util.ArrayList<NetworkInterface> ret = new java.util.ArrayList<NetworkInterface>();
        for (NetworkInterface i : items)
        {
            if (!StringHelper.isNullOrEmpty(i.getNetworkName()))
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static java.util.ArrayList<VdsNetworkInterface> FindAllInterfaceBondNameIsEmpty(java.util.ArrayList<VdsNetworkInterface> items)
    {
        java.util.ArrayList<VdsNetworkInterface> ret = new java.util.ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : items)
        {
            if (StringHelper.isNullOrEmpty(i.getBondName()))
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static java.util.ArrayList<VdsNetworkInterface> FindAllInterfaceVlanIdIsEmpty(java.util.ArrayList<VdsNetworkInterface> items)
    {
        java.util.ArrayList<VdsNetworkInterface> ret = new java.util.ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface i : items)
        {
            if (i.getVlanId() == null)
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static network FindNetworkByName(java.util.ArrayList<network> items, String name)
    {
        for (network n : items)
        {
            if (StringHelper.stringsEqual(n.getname(), name))
            {
                return n;
            }
        }
        return null;
    }

    public static java.util.ArrayList<VDS> FindAllVDSByPmEnabled(java.util.ArrayList<VDS> items)
    {
        java.util.ArrayList<VDS> ret = new java.util.ArrayList<VDS>();
        for (VDS i : items)
        {
            if (i.getpm_enabled())
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static SanTargetModel FindSanTargetByNotIsConnected(java.util.ArrayList<SanTargetModel> items)
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

    public static java.util.ArrayList<storage_domains> FindAllStorageDomainsBySharedStatus(java.util.ArrayList<storage_domains> items,
            StorageDomainSharedStatus status)
    {
        java.util.ArrayList<storage_domains> ret = new java.util.ArrayList<storage_domains>();
        for (storage_domains i : items)
        {
            if (i.getstorage_domain_shared_status() == status)
            {
                ret.add(i);
            }
        }
        return ret;
    }

    public static VdcReturnValueBase FindVdcReturnValueByDescription(java.util.ArrayList<VdcReturnValueBase> items,
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
    public static <TSource> java.util.ArrayList<TSource> Except(java.util.ArrayList<TSource> first,
            java.util.ArrayList<TSource> second)
    {
        java.util.ArrayList<TSource> newIEnumerable = new java.util.ArrayList<TSource>();

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

    public static <TSource> Iterable<TSource> Where(Iterable<TSource> source, IPredicate<TSource> predicate)
    {
        java.util.ArrayList<TSource> list = new java.util.ArrayList<TSource>();

        for (TSource item : source)
        {
            if (predicate.Match(item))
            {
                list.add(item);
            }
        }

        return list;
    }

    public static Version SelectHighestVersion(java.util.ArrayList<Version> versions)
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
    public static <TSource> java.util.ArrayList<TSource> ToList(Iterable<TSource> source)
    {
        java.util.ArrayList<TSource> list = new java.util.ArrayList<TSource>();
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

    public static <TSource> java.util.ArrayList<TSource> Distinct(java.util.ArrayList<TSource> source,
            IEqualityComparer<TSource> comparer)
    {
        java.util.ArrayList<TSource> list = new java.util.ArrayList<TSource>();
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

    public static <TResult> java.util.ArrayList<TResult> Cast(Iterable source)
    {
        java.util.ArrayList<TResult> list = new java.util.ArrayList<TResult>();
        for (Object a : source)
        {
            TResult item = (TResult) a;
            list.add(item);
        }

        return list;
    }

    public static <T extends Comparable<T>> java.util.ArrayList<T> OrderByDescending(java.util.List<T> source)
    {
        java.util.ArrayList<T> list = new java.util.ArrayList<T>();

        java.util.ArrayList<T> sorted = new java.util.ArrayList<T>(source);
        Collections.sort(sorted);

        for (int i = sorted.size(); i > 0; i--)
        {
            list.add(sorted.get(i - 1));
        }

        return list;
    }

    public static <T> java.util.ArrayList<T> OrderByDescending(java.util.List<T> source,
            java.util.Comparator<T> comparer)
    {
        java.util.ArrayList<T> list = new java.util.ArrayList<T>();

        java.util.ArrayList<T> sorted = new java.util.ArrayList<T>(source);
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
    public static void Sort(java.util.List source, java.util.Comparator comparer)
    {
        int pos = 0;
        while (pos < source.size())
        {
            if (pos == 0 || comparer.compare(source.get(pos), source.get(pos - 1)) >= 1)
            {
                pos++;
            }
            else
            {
                Object temp = source.get(pos);
                source.set(pos, source.get(pos - 1));
                source.set(pos - 1, temp);
                pos--;
            }
        }
    }

    public static java.util.List Concat(java.util.List... lists)
    {
        java.util.List result = new java.util.ArrayList<Object>();
        for (java.util.List list : lists)
        {
            for (Object item : list)
            {
                result.add(item);
            }
        }

        return result;
    }

    public final static class TimeZonePredicate implements IPredicate<java.util.Map.Entry<String, String>>
    {
        private final String timeZone;

        public TimeZonePredicate(String timeZone)
        {
            this.timeZone = timeZone;
        }

        @Override
        public boolean Match(java.util.Map.Entry<String, String> source)
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
            return id.equals(source.getID());
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
            return source.getvds_id().equals(id);
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
            return source.getstatus().equals(status);
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

    public final static class StoragePredicate implements IPredicate<storage_domains>
    {
        private Guid id = new Guid();

        public StoragePredicate(Guid id)
        {
            this.id = id;
        }

        @Override
        public boolean Match(storage_domains source)
        {
            return id.equals(source.getid());
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
