package org.ovirt.engine.core.dal.dbbroker;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.ExternalVariable;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.DwhHistoryTimekeeping;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.SupportedAdditionalClusterFeature;
import org.ovirt.engine.core.common.businessentities.UserProfile;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsKdumpStatus;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.CommandEntityDao;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.dao.ExternalVariableDao;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.UserProfileDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsKdumpStatusDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.VmIconDefaultDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmJobDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.dwh.DwhHistoryTimekeepingDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VmNicFilterParameterDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;

@Singleton
public class DbFacade {
    @SuppressWarnings("serial")
    private static final Map<Class<?>, Class<?>> mapEntityToDao = new HashMap<Class<?>, Class<?>>() {
        {
            put(StoragePool.class, StoragePoolDao.class);
            put(StoragePoolIsoMap.class, StoragePoolIsoMapDao.class);
            put(StorageDomainStatic.class, StorageDomainStaticDao.class);
            put(StorageDomainDynamic.class, StorageDomainDynamicDao.class);
            put(VdsStatic.class, VdsStaticDao.class);
            put(VdsDynamic.class, VdsDynamicDao.class);
            put(VdsStatistics.class, VdsStatisticsDao.class);
            put(VdsSpmIdMap.class, VdsSpmIdMapDao.class);
            put(Role.class, RoleDao.class);
            put(VmTemplate.class, VmTemplateDao.class);
            put(VmDynamic.class, VmDynamicDao.class);
            put(VmStatic.class, VmStaticDao.class);
            put(VmStatistics.class, VmStatisticsDao.class);
            put(BaseDisk.class, BaseDiskDao.class);
            put(DiskVmElement.class, DiskVmElementDao.class);
            put(DiskImage.class, BaseDiskDao.class);
            put(CinderDisk.class, BaseDiskDao.class);
            put(ManagedBlockStorageDisk.class, BaseDiskDao.class);
            put(DiskImageDynamic.class, DiskImageDynamicDao.class);
            put(VmNic.class, VmNicDao.class);
            put(VmNetworkInterface.class, VmNicDao.class);
            put(VmNetworkStatistics.class, VmNetworkStatisticsDao.class);
            put(Network.class, NetworkDao.class);
            put(Provider.class, ProviderDao.class);
            put(Snapshot.class, SnapshotDao.class);
            put(VmDevice.class, VmDeviceDao.class);
            put(ImageStorageDomainMap.class, ImageStorageDomainMapDao.class);
            put(Permission.class, PermissionDao.class);
            put(Image.class, ImageDao.class);
            put(Job.class, JobDao.class);
            put(Step.class, StepDao.class);
            put(VnicProfile.class, VnicProfileDao.class);
            put(VnicProfileView.class, VnicProfileDao.class);
            put(DwhHistoryTimekeeping.class, DwhHistoryTimekeepingDao.class);
            put(IscsiBond.class, IscsiBondDao.class);
            put(VmInit.class, VmInitDao.class);
            put(VdsNumaNode.class, VdsNumaNodeDao.class);
            put(VmNumaNode.class, VmNumaNodeDao.class);
            put(CommandEntity.class, CommandEntityDao.class);
            put(ExternalVariable.class, ExternalVariableDao.class);
            put(VdsKdumpStatus.class, VdsKdumpStatusDao.class);
            put(VmJob.class, VmJobDao.class);
            put(MacPool.class, MacPoolDao.class);
            put(DiskProfile.class, DiskProfileDao.class);
            put(FenceAgent.class, FenceAgentDao.class);
            put(EngineSession.class, EngineSessionDao.class);
            put(HostDevice.class, HostDeviceDao.class);
            put(UserProfile.class, UserProfileDao.class);
            put(VmIcon.class, VmIconDao.class);
            put(VmIconDefault.class, VmIconDefaultDao.class);
            put(LibvirtSecret.class, LibvirtSecretDao.class);
            put(StorageServerConnections.class, StorageServerConnectionDao.class);
            put(ImageTransfer.class, ImageTransferDao.class);
            put(LUNs.class, LunDao.class);
            put(SupportedAdditionalClusterFeature.class, ClusterFeatureDao.class);
            put(Cluster.class, ClusterDao.class);
            put(NetworkCluster.class, NetworkClusterDao.class);
            put(VmNicFilterParameter.class, VmNicFilterParameterDao.class);
        }
    };

    @Inject
    private Instance<Dao> daos;

    /**
     * Return the correct Dao for the given {@link BusinessEntity} class.
     *
     * @param <T>
     *            The Type of Dao which is returned.
     * @param entityClass
     *            The class of the entity.
     * @return The Dao for the entity.
     */
    public <T extends GenericDao<?, ?>> T getDaoForEntity(Class<? extends BusinessEntity<?>> entityClass) {
        @SuppressWarnings("unchecked")
        Class<T> daoType = (Class<T>) mapEntityToDao.get(entityClass);
        return getDao(daoType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Dao> T getDao(Class<T> daoType) {
        for (Dao dao : daos) {
            if (daoType.isAssignableFrom(dao.getClass())) {
                return (T) dao;
            }
        }
        throw new IllegalArgumentException("There is no Dao registered for dao type " + daoType.getName());
    }
}
