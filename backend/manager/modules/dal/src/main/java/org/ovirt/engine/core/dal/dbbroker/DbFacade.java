package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.ExternalVariable;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.CpuStatistics;
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
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
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
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ActionGroupDao;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.AuditLogDao;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.BookmarkDao;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDao;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.ClusterFeatureDao;
import org.ovirt.engine.core.dao.CommandEntityDao;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.DbGroupDao;
import org.ovirt.engine.core.dao.DbUserDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.EngineBackupLogDao;
import org.ovirt.engine.core.dao.EngineSessionDao;
import org.ovirt.engine.core.dao.EventDao;
import org.ovirt.engine.core.dao.ExternalVariableDao;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.ImageTransferDao;
import org.ovirt.engine.core.dao.IscsiBondDao;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.LibvirtSecretDao;
import org.ovirt.engine.core.dao.LunDao;
import org.ovirt.engine.core.dao.MacPoolDao;
import org.ovirt.engine.core.dao.PermissionDao;
import org.ovirt.engine.core.dao.QuotaDao;
import org.ovirt.engine.core.dao.RepoFileMetaDataDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.RoleGroupMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainOvfInfoDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.StorageServerConnectionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDao;
import org.ovirt.engine.core.dao.SupportedHostFeatureDao;
import org.ovirt.engine.core.dao.TagDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.UserProfileDao;
import org.ovirt.engine.core.dao.VdcOptionDao;
import org.ovirt.engine.core.dao.VdsCpuStatisticsDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VdsDynamicDao;
import org.ovirt.engine.core.dao.VdsKdumpStatusDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VdsSpmIdMapDao;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.VdsStatisticsDao;
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;
import org.ovirt.engine.core.dao.VmIconDao;
import org.ovirt.engine.core.dao.VmIconDefaultDao;
import org.ovirt.engine.core.dao.VmInitDao;
import org.ovirt.engine.core.dao.VmJobDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.VmPoolDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmStatisticsDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.dao.dwh.DwhHistoryTimekeepingDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterClusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterHooksDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerDao;
import org.ovirt.engine.core.dao.gluster.GlusterServerServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterServiceDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotConfigDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeSnapshotScheduleDao;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.dao.network.NetworkViewDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
import org.ovirt.engine.core.dao.network.VmNicDao;
import org.ovirt.engine.core.dao.network.VnicProfileDao;
import org.ovirt.engine.core.dao.network.VnicProfileViewDao;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;
import org.ovirt.engine.core.dao.provider.ProviderDao;
import org.ovirt.engine.core.dao.qos.CpuQosDao;
import org.ovirt.engine.core.dao.qos.QosBaseDao;
import org.ovirt.engine.core.dao.qos.StorageQosDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.dao.scheduling.ClusterPolicyDao;
import org.ovirt.engine.core.dao.scheduling.PolicyUnitDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Singleton
public class DbFacade {

    private static final Logger log = LoggerFactory.getLogger(DbFacade.class);

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
            put(CpuStatistics.class, VdsCpuStatisticsDao.class);
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
        }
    };

    private static DbFacade instance;

    private final DbFacadeLocator dbFacadeLocator;
    private final JdbcTemplate jdbcTemplate;
    private final DbEngineDialect dbEngineDialect;
    private final SimpleJdbcCallsHandler callsHandler;

    @Inject
    private Instance<Dao> daos;


    private int onStartConnectionTimeout;

    private int connectionCheckInterval;

    @Inject
    public DbFacade(JdbcTemplate jdbcTemplate,
            DbEngineDialect dbEngineDialect,
            SimpleJdbcCallsHandler callsHandler,
            DbFacadeLocator dbFacadeLocator) {

        Objects.requireNonNull(jdbcTemplate, "jdbcTemplate cannot be null");
        Objects.requireNonNull(dbEngineDialect, "dbEngineDialect cannot be null");
        Objects.requireNonNull(callsHandler, "callsHandler cannot be null");
        Objects.requireNonNull(dbFacadeLocator, "dbFacadeLocator cannot be null");

        this.dbFacadeLocator = dbFacadeLocator;
        this.jdbcTemplate = jdbcTemplate;
        this.dbEngineDialect = dbEngineDialect;
        this.callsHandler = callsHandler;

        init();
    }

    private void init() {
        log.info("Initializing the DbFacade");
        dbFacadeLocator.configure(this);

        instance = this;
    }

    public DbEngineDialect getDbEngineDialect() {
        return dbEngineDialect;
    }

    public SimpleJdbcCallsHandler getCallsHandler() {
        return callsHandler;
    }

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

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
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

    /**
     * just convenience so we don't refactor old code
     */
    public static DbFacade getInstance() {
        return instance;
    }

    /**
     * Visible for testing
     */
    public static void setInstance(DbFacade dbFacade){
        instance = dbFacade;
    }

    private CustomMapSqlParameterSource getCustomMapSqlParameterSource() {
        return new CustomMapSqlParameterSource(dbEngineDialect);
    }

    public String getEntityNameByIdAndType(Guid objectId, VdcObjectType vdcObjectType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("entity_id", objectId)
                .addValue("object_type", vdcObjectType.getValue());

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withFunctionName("fn_get_entity_name").execute(
                        parameterSource);

        String resultKey = dbEngineDialect.getFunctionReturnKey();
        return dbResults.get(resultKey) != null ? dbResults.get(resultKey).toString() : null;
    }

    public Integer getSystemStatisticsValue(String entity) {
        return getSystemStatisticsValue(entity, "");
    }

    public Integer getSystemStatisticsValue(String entity, String status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("entity", entity).addValue(
                "status", status);

        RowMapper<Integer> mapper = new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("val");
            }
        };

        Map<String, Object> dbResults =
                dbEngineDialect.createJdbcCallForQuery(jdbcTemplate).withProcedureName("Getsystem_statistics")
                        .returningResultSet("RETURN_VALUE", mapper).execute(parameterSource);

        return (Integer) DbFacadeUtils.asSingleResult((List<?>) dbResults.get("RETURN_VALUE"));
    }

    /**
     * User presentation in GUI have a distinction between ADMIN/USER user. The distinction is determined by their
     * permissions or their group's permissions. when Permission with the role type Admin is found, set the DbUser
     * isAdmin flag to ADMIN Type or to USER otherwise. Make the change only if the value is different to what it is
     * saved to db
     */
    public void updateLastAdminCheckStatus(Guid... userIds) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("userIds", StringUtils.join(userIds, ","));

        new SimpleJdbcCall(jdbcTemplate).withProcedureName("UpdateLastAdminCheckStatus")
                .execute(parameterSource);
    }

    /***
     * CheckDBConnection calls a simple "select 1" SP to verify DB is up & running.
     *
     * @return True if DB is up & running.
     */
    public boolean checkDBConnection() {
        return new SimpleJdbcCall(jdbcTemplate).withProcedureName("CheckDBConnection").execute() != null;
    }

    /**
     * Returns a singleton instance of {@link BookmarkDao}.
     *
     * @return the dao
     */
    public BookmarkDao getBookmarkDao() {
        return getDao(BookmarkDao.class);

    }

    /**
     * Retrieves the singleton instance of {@link CommandEntityDao}.
     *
     * @return the dao
     */
    public CommandEntityDao getCommandEntityDao() {
        return getDao(CommandEntityDao.class);
    }

    /**
     * Returns the singleton instance of {@link DbUserDao}.
     *
     * @return the dao
     */
    public DbUserDao getDbUserDao() {
        return getDao(DbUserDao.class);
    }

    /**
     * Returns the singleton instance of {@link UserProfileDao}.
     *
     * @return the dao
     */
    public UserProfileDao getUserProfileDao() {
        return getDao(UserProfileDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDao}.
     *
     * @return the dao
     */
    public VdsDao getVdsDao() {
        return getDao(VdsDao.class);
    }

    /**
     * Returns the singleton instance of {@link UnregisteredOVFDataDao}.
     *
     * @return the dao
     */
    public UnregisteredOVFDataDao getUnregisteredOVFDataDao() {
        return getDao(UnregisteredOVFDataDao.class);
    }

    /**
     * Returns the singleton instance of {@link UnregisteredDisksDao}.
     *
     * @return the dao
     */
    public UnregisteredDisksDao getUnregisteredDisksDao() {
        return getDao(UnregisteredDisksDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmAndTemplatesGenerationsDao}.
     *
     * @return the dao
     */
    public VmAndTemplatesGenerationsDao getVmAndTemplatesGenerationsDao() {
        return getDao(VmAndTemplatesGenerationsDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStaticDao}.
     *
     * @return the dao
     */
    public VdsStaticDao getVdsStaticDao() {
        return getDao(VdsStaticDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDynamicDao}.
     *
     * @return the dao
     */
    public VdsDynamicDao getVdsDynamicDao() {
        return getDao(VdsDynamicDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStatisticsDao}.
     *
     * @return the dao
     */
    public VdsStatisticsDao getVdsStatisticsDao() {
        return getDao(VdsStatisticsDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsSpmIdMapDao}.
     *
     * @return the dao
     */
    public VdsSpmIdMapDao getVdsSpmIdMapDao() {
        return getDao(VdsSpmIdMapDao.class);
    }

    /**
     * Returns the singleton instance of {@link org.ovirt.engine.core.dao.ClusterDao}.
     *
     * @return the dao
     */
    public ClusterDao getClusterDao() {
        return getDao(ClusterDao.class);
    }

    /**
     * Returns the single instance of {@link AuditLogDao}.
     *
     * @return the dao
     */
    public AuditLogDao getAuditLogDao() {
        return getDao(AuditLogDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link LunDao}.
     *
     * @return the dao
     */
    public LunDao getLunDao() {
        return getDao(LunDao.class);
    }

    /**
     * Returns the singleton instance of {@link InterfaceDao}.
     *
     * @return the dao
     */
    public InterfaceDao getInterfaceDao() {
        return getDao(InterfaceDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmNetworkInterfaceDao}.
     *
     * @return the dao
     */
    public VmNetworkInterfaceDao getVmNetworkInterfaceDao() {
        return getDao(VmNetworkInterfaceDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmNetworkInterfaceDao}.
     *
     * @return the dao
     */
    public VmNetworkStatisticsDao getVmNetworkStatisticsDao() {
        return getDao(VmNetworkStatisticsDao.class);
    }

    /**
     * Returns the singleton instance of {@link RoleGroupMapDao}.
     *
     * @return the dao
     */
    public RoleGroupMapDao getRoleGroupMapDao() {
        return getDao(RoleGroupMapDao.class);
    }

    /**
     * * Returns the singleton instance of {@link VmTemplateDao}.
     *
     * @return the dao
     */
    public VmTemplateDao getVmTemplateDao() {
        return getDao(VmTemplateDao.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDao}.
     *
     * @return the dao
     */
    public VmDao getVmDao() {
        return getDao(VmDao.class);
    }

    /**
     * * Returns the singleton instance if {@link org.ovirt.engine.core.dao.VmIconDao}
     *
     * @return the dao
     */
    public VmIconDao getVmIconDao() {
        return getDao(VmIconDao.class);
    }

    public VmIconDefaultDao getVmIconsDefaultDao() {
        return getDao(VmIconDefaultDao.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDynamicDao}.
     *
     * @return the dao
     */
    public VmDynamicDao getVmDynamicDao() {
        return getDao(VmDynamicDao.class);
    }

    /**
     * Returns the singleton instance of {@link TagDao}.
     *
     * @return the dao
     */
    public TagDao getTagDao() {
        return getDao(TagDao.class);
    }

    /**
     * Returns the singleton instance of {@link BaseDiskDao}.
     *
     * @return the dao
     */
    public BaseDiskDao getBaseDiskDao() {
        return getDao(BaseDiskDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskVmElementDao}.
     *
     * @return the dao
     */
    public DiskVmElementDao getDiskVmElementDao() {
        return getDao(DiskVmElementDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskDao}.
     *
     * @return the dao
     */
    public DiskDao getDiskDao() {
        return getDao(DiskDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskLunMapDao}.
     *
     * @return the dao
     */
    public DiskLunMapDao getDiskLunMapDao() {
        return getDao(DiskLunMapDao.class);
    }

    /**
     * Returns the singleton instance of {@link ImageDao}.
     *
     * @return the dao
     */
    public ImageDao getImageDao() {
        return getDao(ImageDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskImageDao}.
     *
     * @return the dao
     */
    public DiskImageDao getDiskImageDao() {
        return getDao(DiskImageDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskImageDynamicDao}.
     *
     * @return the dao
     */
    public DiskImageDynamicDao getDiskImageDynamicDao() {
        return getDao(DiskImageDynamicDao.class);
    }

    /**
     * Returns the singleton instance of {@link EventDao}.
     *
     * @return the dao
     */
    public EventDao getEventDao() {
        return getDao(EventDao.class);
    }

    /**
     * Returns the singleton instance of {@link ActionGroupDao}.
     *
     * @return the dao
     */
    public ActionGroupDao getActionGroupDao() {
        return getDao(ActionGroupDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link RoleDao}.
     *
     * @return the dao
     */
    public RoleDao getRoleDao() {
        return getDao(RoleDao.class);
    }

    /**
     * Returns the singleton instance of {@link AsyncTaskDao}.
     *
     * @return the dao
     */
    public AsyncTaskDao getAsyncTaskDao() {
        return getDao(AsyncTaskDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link DbGroupDao}.
     *
     * @return the dao
     */
    public DbGroupDao getDbGroupDao() {
        return getDao(DbGroupDao.class);
    }

    /**
     * Returns the singleton instance of {@link EngineSessionDao}.
     *
     * @return the dao
     */
    public EngineSessionDao getEngineSessionDao() {
        return getDao(EngineSessionDao.class);
    }

    /**
     * Returns the singleton instance of {@link ProviderDao}.
     *
     * @return the dao
     */
    public ProviderDao getProviderDao() {
        return getDao(ProviderDao.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkDao}.
     *
     * @return the dao
     */
    public NetworkDao getNetworkDao() {
        return getDao(NetworkDao.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkViewDao}.
     *
     * @return the dao
     */
    public NetworkViewDao getNetworkViewDao() {
        return getDao(NetworkViewDao.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkClusterDao}.
     *
     * @return the dao
     */
    public NetworkClusterDao getNetworkClusterDao() {
        return getDao(NetworkClusterDao.class);
    }

    /**
     * Returns the singleton instance of {@link PermissionDao}.
     *
     * @return the dao
     */
    public PermissionDao getPermissionDao() {
        return getDao(PermissionDao.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDao}.
     *
     * @return the dao
     */
    public StorageDomainDao getStorageDomainDao() {
        return getDao(StorageDomainDao.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainOvfInfoDao}.
     *
     * @return the dao
     */
    public StorageDomainOvfInfoDao getStorageDomainOvfInfoDao() {
        return getDao(StorageDomainOvfInfoDao.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDao}.
     *
     * @return the dao
     */
    public StorageDomainStaticDao getStorageDomainStaticDao() {
        return getDao(StorageDomainStaticDao.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDao}.
     *
     * @return the dao
     */
    public StorageDomainDynamicDao getStorageDomainDynamicDao() {
        return getDao(StorageDomainDynamicDao.class);
    }

    /**
     * Returns the singleton instance of {@link RepoFileMetaDataDao}.
     *
     * @return Repository file meta data dao.
     */
    public RepoFileMetaDataDao getRepoFileMetaDataDao() {
        return getDao(RepoFileMetaDataDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link SnapshotDao}.
     *
     * @return the dao
     */
    public SnapshotDao getSnapshotDao() {
        return getDao(SnapshotDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link ImageStorageDomainMapDao}.
     *
     * @return the dao
     */
    public ImageStorageDomainMapDao getImageStorageDomainMapDao() {
        return getDao(ImageStorageDomainMapDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link StoragePoolDao}.
     *
     * @return the dao
     */
    public StoragePoolDao getStoragePoolDao() {
        return getDao(StoragePoolDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link MacPoolDao}.
     *
     * @return the dao
     */
    public MacPoolDao getMacPoolDao() {
        return getDao(MacPoolDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link StoragePoolIsoMapDao}.
     *
     * @return the dao
     */
    public StoragePoolIsoMapDao getStoragePoolIsoMapDao() {
        return getDao(StoragePoolIsoMapDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionDao}.
     *
     * @return the dao
     */
    public StorageServerConnectionDao getStorageServerConnectionDao() {
        return getDao(StorageServerConnectionDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionLunMapDao}.
     *
     * @return the dao
     */
    public StorageServerConnectionLunMapDao getStorageServerConnectionLunMapDao() {
        return getDao(StorageServerConnectionLunMapDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdcOptionDao}.
     *
     * @return the dao
     */
    public VdcOptionDao getVdcOptionDao() {
        return getDao(VdcOptionDao.class);
    }

    /**
     * Returns the singleton instance of {@link BusinessEntitySnapshotDao}.
     *
     * @return the dao
     */
    public BusinessEntitySnapshotDao getBusinessEntitySnapshotDao() {
        return getDao(BusinessEntitySnapshotDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmPoolDao}.
     *
     * @return the dao
     */
    public VmPoolDao getVmPoolDao() {
        return getDao(VmPoolDao.class);
    }

    public VmStaticDao getVmStaticDao() {
        return getDao(VmStaticDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmStatisticsDao}.
     *
     * @return the dao
     */
    public VmStatisticsDao getVmStatisticsDao() {
        return getDao(VmStatisticsDao.class);
    }

    /**
     * Returns the singleton instance of {@link QuotaDao}.
     *
     * @return the dao
     */
    public QuotaDao getQuotaDao() {
        return getDao(QuotaDao.class);
    }

    public VmDeviceDao getVmDeviceDao() {
        return getDao(VmDeviceDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmJobDao}.
     *
     * @return the dao
     */
    public VmJobDao getVmJobDao() {
        return getDao(VmJobDao.class);
    }

    /**
     *
     * Returns the singleton instance of {@link JobDao}.
     *
     * @return the dao
     */
    public JobDao getJobDao() {
        return getDao(JobDao.class);
    }

    /**
     * Returns the singleton instance of {@link JobSubjectEntityDao}.
     *
     * @return the dao
     */
    public JobSubjectEntityDao getJobSubjectEntityDao() {
        return getDao(JobSubjectEntityDao.class);
    }

    /**
     * Returns the singleton instance of {@link StepDao}.
     *
     * @return the dao
     */
    public StepDao getStepDao() {
        return getDao(StepDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterVolumeSnapshotDao}
     *
     * @return the dao
     */
    public GlusterVolumeSnapshotDao getGlusterVolumeSnapshotDao() {
        return getDao(GlusterVolumeSnapshotDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterVolumeSnapshotConfigDao}
     *
     * @return the dao
     */
    public GlusterVolumeSnapshotConfigDao getGlusterVolumeSnapshotConfigDao() {
        return getDao(GlusterVolumeSnapshotConfigDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link org.ovirt.engine.core.dao.StorageServerConnectionExtensionDao}.
     *
     * @return the dao
     */
    public StorageServerConnectionExtensionDao getStorageServerConnectionExtensionDao() {
        return getDao(StorageServerConnectionExtensionDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterVolumeSnapshotScheduleDao}
     *
     * @return the dao
     */
    public GlusterVolumeSnapshotScheduleDao getGlusterVolumeSnapshotScheduleDao() {
        return getDao(GlusterVolumeSnapshotScheduleDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterVolumeDao}.
     *
     * @return the dao
     */
    public GlusterVolumeDao getGlusterVolumeDao() {
        return getDao(GlusterVolumeDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterBrickDao}.
     *
     * @return the dao
     */
    public GlusterBrickDao getGlusterBrickDao() {
        return getDao(GlusterBrickDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterOptionDao}.
     *
     * @return the dao
     */
    public GlusterOptionDao getGlusterOptionDao() {
        return getDao(GlusterOptionDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterServiceDao}.
     *
     * @return the dao
     */
    public GlusterServiceDao getGlusterServiceDao() {
        return getDao(GlusterServiceDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterServerServiceDao}.
     *
     * @return the dao
     */
    public GlusterServerServiceDao getGlusterServerServiceDao() {
        return getDao(GlusterServerServiceDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterClusterServiceDao}.
     *
     * @return the dao
     */
    public GlusterClusterServiceDao getGlusterClusterServiceDao() {
        return getDao(GlusterClusterServiceDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterHooksDao}.
     *
     * @return the dao
     */
    public GlusterHooksDao getGlusterHooksDao() {
        return getDao(GlusterHooksDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterGeoRepDao}.
     *
     * @return the dao
     */
    public GlusterGeoRepDao getGlusterGeoRepDao() {
        return getDao(GlusterGeoRepDao.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDeviceDao}.
     *
     * @return the dao
     */
    public StorageDeviceDao getStorageDeviceDao() {
        return getDao(StorageDeviceDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterServerDao}.
     *
     * @return the dao
     */
    public GlusterServerDao getGlusterServerDao() {
        return getDao(GlusterServerDao.class);
    }

    public void setOnStartConnectionTimeout(int onStartConnectionTimeout) {
        this.onStartConnectionTimeout = onStartConnectionTimeout;
    }

    public int getOnStartConnectionTimeout() {
        return onStartConnectionTimeout;
    }

    public void setConnectionCheckInterval(int connectionCheckInterval) {
        this.connectionCheckInterval = connectionCheckInterval;
    }

    public int getConnectionCheckInterval() {
        return connectionCheckInterval;
    }

    /**
     * Returns the singleton instance of {@link VmGuestAgentInterfaceDao}.
     *
     * @return the dao
     */
    public VmGuestAgentInterfaceDao getVmGuestAgentInterfaceDao() {
        return getDao(VmGuestAgentInterfaceDao.class);
    }

    public NetworkQoSDao getNetworkQosDao() {
        return getDao(NetworkQoSDao.class);
    }

    public HostNetworkQosDao getHostNetworkQosDao() {
        return getDao(HostNetworkQosDao.class);
    }

    public StorageQosDao getStorageQosDao() {
        return getDao(StorageQosDao.class);
    }

    public CpuQosDao getCpuQosDao() {
        return getDao(CpuQosDao.class);
    }

    public QosBaseDao getQosBaseDao() {
        return getDao(QosBaseDao.class);
    }
    /**
     * Returns the singleton instance of {@link PolicyUnitDao}.
     *
     * @return the dao
     */
    public PolicyUnitDao getPolicyUnitDao() {
        return getDao(PolicyUnitDao.class);
    }

    /**
     * Returns the singleton instance of {@link ClusterPolicyDao}.
     *
     * @return the dao
     */
    public ClusterPolicyDao getClusterPolicyDao() {
        return getDao(ClusterPolicyDao.class);
    }

    /**
     * Returns the singleton instance of {@link VnicProfileDao}.
     *
     * @return the dao
     */
    public VnicProfileDao getVnicProfileDao() {
        return getDao(VnicProfileDao.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkFilterDao}.
     *
     * @return the dao
     */
    public NetworkFilterDao getNetworkFilterDao() {
        return getDao(NetworkFilterDao.class);
    }

    /**
     * Returns the singleton instance of {@link VnicProfileViewDao}.
     *
     * @return the dao
     */
    public VnicProfileViewDao getVnicProfileViewDao() {
        return getDao(VnicProfileViewDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmNicDao}.
     *
     * @return the dao
     */
    public VmNicDao getVmNicDao() {
        return getDao(VmNicDao.class);
    }

    public VmInitDao getVmInitDao() {
        return getDao(VmInitDao.class);
    }

    /**
     * Returns the singleton instance of {@link DwhHistoryTimekeepingDao}.
     *
     * @return the dao instance
     */
    public DwhHistoryTimekeepingDao getDwhHistoryTimekeepingDao() {
        return getDao(DwhHistoryTimekeepingDao.class);
    }

    /**
     * Returns the singleton instance of {@link AffinityGroupDao}.
     *
     * @return the dao instance
     */
    public AffinityGroupDao getAffinityGroupDao() {
        return getDao(AffinityGroupDao.class);
    }

    /**
     * Returns the singleton instance of {@link ExternalVariableDao}.
     *
     * @return the dao instance
     */
    public ExternalVariableDao getExternalVariableDao() {
        return getDao(ExternalVariableDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsKdumpStatusDao}.
     *
     * @return the dao instance
     */
    public VdsKdumpStatusDao getVdsKdumpStatusDao() {
        return getDao(VdsKdumpStatusDao.class);
    }

    /**
     * This call will populate a translation table of OS Ids to they're name
     * The translation table shall be in use by DWH
     *
     * @param osIdToName
     *            OS id to OS Name map
     */
    public void populateDwhOsInfo(Map<Integer, String> osIdToName) {
        // first clear the table
        new SimpleJdbcCall(jdbcTemplate).withProcedureName("clear_osinfo").execute();
        // batch populate
        List<MapSqlParameterSource> executions = new ArrayList<>();
        for (Map.Entry<Integer, String> e : osIdToName.entrySet()) {
            executions.add(getCustomMapSqlParameterSource()
                    .addValue("os_id", e.getKey())
                    .addValue("os_name", e.getValue()));
        }
        getCallsHandler().executeStoredProcAsBatch("insert_osinfo", executions);
    }

    public IscsiBondDao getIscsiBondDao() {
        return getDao(IscsiBondDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsCpuStatisticsDao}.
     *
     * @return the dao instance
     */
    public VdsCpuStatisticsDao getVdsCpuStatisticsDao() {
        return getDao(VdsCpuStatisticsDao.class);
    }

    /**
     * Returns the singleton instance of {@link VdsNumaNodeDao}.
     *
     * @return the dao instance
     */
    public VdsNumaNodeDao getVdsNumaNodeDao() {
        return getDao(VdsNumaNodeDao.class);
    }

    /**
     * Returns the singleton instance of {@link VmNumaNodeDao}.
     *
     * @return the dao instance
     */
    public VmNumaNodeDao getVmNumaNodeDao() {
        return getDao(VmNumaNodeDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskProfileDao}.
     *
     * @return the dao instance
     */
    public DiskProfileDao getDiskProfileDao() {
        return getDao(DiskProfileDao.class);
    }

    /**
     * Returns the singleton instance of {@link CpuProfileDao}.
     *
     * @return the dao instance
     */
    public CpuProfileDao getCpuProfileDao() {
        return getDao(CpuProfileDao.class);
    }

    public FenceAgentDao getFenceAgentDao() {
        return getDao(FenceAgentDao.class);
    }

    /**
     * Returns the singleton instance of {@link HostDeviceDao}.
     *
     * @return the dao instance
     */
    public HostDeviceDao getHostDeviceDao() {
        return getDao(HostDeviceDao.class);
    }

    /**
     * Returns the singleton instance of {@link org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig}.
     *
     * @return the dao
     */
    public HostNicVfsConfigDao getHostNicVfsConfigDao() {
        return getDao(HostNicVfsConfigDao.class);
    }

    public EngineBackupLogDao getEngineBackupLogDao() {
        return getDao(EngineBackupLogDao.class);
    }

    public ClusterFeatureDao getClusterFeatureDao() {
        return getDao(ClusterFeatureDao.class);
    }

    public SupportedHostFeatureDao getSupportedHostFeatureDao() {
        return getDao(SupportedHostFeatureDao.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkAttachmentDao}.
     *
     * @return the dao
     */
    public NetworkAttachmentDao getNetworkAttachmentDao() {
        return getDao(NetworkAttachmentDao.class);
    }

    public LibvirtSecretDao getLibvirtSecretDao() {
        return getDao(LibvirtSecretDao.class);
    }

    public ImageTransferDao getImageTransferDao() {
        return getDao(ImageTransferDao.class);
    }

}
