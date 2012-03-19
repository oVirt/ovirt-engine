package org.ovirt.engine.core.dal.dbbroker;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VmNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.roles;
import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.ActionGroupDAO;
import org.ovirt.engine.core.dao.AdGroupDAO;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.ovirt.engine.core.dao.BookmarkDAO;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.DaoFactory;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.EventDAO;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.LunDAO;
import org.ovirt.engine.core.dao.NetworkClusterDAO;
import org.ovirt.engine.core.dao.NetworkDAO;
import org.ovirt.engine.core.dao.PermissionDAO;
import org.ovirt.engine.core.dao.QuotaDAO;
import org.ovirt.engine.core.dao.RepoFileMetaDataDAO;
import org.ovirt.engine.core.dao.RoleDAO;
import org.ovirt.engine.core.dao.RoleGroupMapDAO;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;
import org.ovirt.engine.core.dao.StorageDomainDynamicDAO;
import org.ovirt.engine.core.dao.StorageDomainStaticDAO;
import org.ovirt.engine.core.dao.StoragePoolDAO;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionDAO;
import org.ovirt.engine.core.dao.StorageServerConnectionLunMapDAO;
import org.ovirt.engine.core.dao.TagDAO;
import org.ovirt.engine.core.dao.VdcOptionDAO;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.VdsDynamicDAO;
import org.ovirt.engine.core.dao.VdsGroupDAO;
import org.ovirt.engine.core.dao.VdsSpmIdMapDAO;
import org.ovirt.engine.core.dao.VdsStaticDAO;
import org.ovirt.engine.core.dao.VdsStatisticsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.dao.VmNetworkStatisticsDAO;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

public class DbFacade {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(DbFacade.class);

    @SuppressWarnings("serial")
    private final static Map<Class<?>, Class<?>> mapEntityToDao = new HashMap<Class<?>, Class<?>>()
    {
        {
            put(storage_pool.class, StoragePoolDAO.class);
            put(storage_pool_iso_map.class, StoragePoolIsoMapDAO.class);
            put(storage_domain_static.class, StorageDomainStaticDAO.class);
            put(storage_domain_dynamic.class, StorageDomainDynamicDAO.class);
            put(VdsStatic.class, VdsStaticDAO.class);
            put(VdsDynamic.class, VdsDynamicDAO.class);
            put(VdsStatistics.class, VdsStatisticsDAO.class);
            put(vds_spm_id_map.class, VdsSpmIdMapDAO.class);
            put(roles.class, RoleDAO.class);
            put(VmTemplate.class, VmTemplateDAO.class);
            put(VmDynamic.class, VmDynamicDAO.class);
            put(VmStatic.class, VmStaticDAO.class);
            put(VmStatistics.class, VmStatisticsDAO.class);
            put(Disk.class, DiskDao.class);
            put(DiskImage.class, DiskImageDAO.class);
            put(DiskImageDynamic.class, DiskImageDynamicDAO.class);
            put(VmNetworkInterface.class, VmNetworkInterfaceDAO.class);
            put(VmNetworkStatistics.class, VmNetworkStatisticsDAO.class);
            put(network.class, NetworkDAO.class);
            put(Snapshot.class, SnapshotDao.class);
            put(VmDevice.class, VmDeviceDAO.class);
        }
    };

    private JdbcTemplate jdbcTemplate;

    private DbEngineDialect dbEngineDialect;
    private final SimpleJdbcCallsHandler callsHandler = new SimpleJdbcCallsHandler();

    private int onStartConnectionTimeout;

    private int connectionCheckInterval;


    public void setDbEngineDialect(DbEngineDialect dbEngineDialect) {
        this.dbEngineDialect = dbEngineDialect;
        callsHandler.setDbEngineDialect(dbEngineDialect);
    }

    public DbEngineDialect getDbEngineDialect() {
        return dbEngineDialect;
    }

    public SimpleJdbcCallsHandler getCallsHandler() {
        return callsHandler;
    }

    /**
     * Return the correct DAO for the given {@link BusinessEntity} class.
     * @param <T> The Type of DAO which is returned.
     * @param entityClass The class of the entity.
     * @return The DAO for the entity.
     */
    public <T extends GenericDao<?, ?>> T getDaoForEntity(Class<? extends BusinessEntity<?>> entityClass) {
        @SuppressWarnings("unchecked")
        Class<T> daoType = (Class<T>) mapEntityToDao.get(entityClass);
        return getDAO(daoType);
    }

    protected <T extends DAO> T getDAO(Class<T> daoType) {
        T dao = DaoFactory.get(daoType);
        if (dao instanceof BaseDAODbFacade) {
            BaseDAODbFacade dbFacadeDAO = (BaseDAODbFacade)dao;
            dbFacadeDAO.setTemplate(jdbcTemplate);
            dbFacadeDAO.setDialect(dbEngineDialect);
            dbFacadeDAO.setDbFacade(this);
        } else {
            //Set here session factory after merge
        }
        return dao;
    }

    public DbFacade() {
    }

    public void setTemplate(JdbcTemplate template) {
        this.jdbcTemplate = template;
        callsHandler.setJdbcTemplate(template);
    }

    /**
     * just convenience so we don't refactor old code
     */
    public static DbFacade getInstance() {
        return DbFacadeLocator.getDbFacade();
    }

    private CustomMapSqlParameterSource getCustomMapSqlParameterSource() {
        return new CustomMapSqlParameterSource(dbEngineDialect);
    }

    public void SaveIsInitialized(Guid vmid, boolean isInitialized) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", vmid).addValue(
                "is_initialized", isInitialized);

        new SimpleJdbcCall(jdbcTemplate).withProcedureName("UpdateIsInitialized")
                .execute(parameterSource);
    }

    // tags_vm_map
    public NGuid getEntityPermissions(Guid adElementId, ActionGroup actionGroup, Guid objectId,
            VdcObjectType vdcObjectType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("user_id", adElementId)
                .addValue("action_group_id", actionGroup.getId()).addValue("object_id", objectId).addValue(
                        "object_type_id", vdcObjectType.getValue());

        String resultKey = "permission_id";
        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withProcedureName("get_entity_permissions")
                        .declareParameters(new SqlOutParameter(resultKey, Types.VARCHAR))
                        .execute(parameterSource);

        return dbResults.get(resultKey) != null ? new NGuid(dbResults.get(resultKey).toString()) : null;
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

    /**
     * Get the column size as defined in database for char/varchar colmuns
     * @param table table name
     * @param column column name
     * @return the column size (number of characters allowed)
     */
    public int getColumnSize(String table, String column) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("table", table)
                .addValue("column", column);

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withFunctionName("fn_get_column_size").execute(
                        parameterSource);

        String resultKey = dbEngineDialect.getFunctionReturnKey();
        return dbResults.get(resultKey) != null ? ((Integer)dbResults.get(resultKey)).intValue() : -1;
    }

    public boolean IsStoragePoolMasterUp(Guid storagePoolId) {
        List<storage_domains> domains = getStorageDomainDAO().getAllForStoragePool(storagePoolId);
        storage_domains master = LinqUtils.firstOrNull(domains, new Predicate<storage_domains>() {
            @Override
            public boolean eval(storage_domains storage_domains) {
                return storage_domains.getstorage_domain_type() == StorageDomainType.Master;
            }
        });
        return master != null
                && (master.getstatus() == StorageDomainStatus.Active || master.getstatus() == StorageDomainStatus.Unknown);
    }

    public Integer GetSystemStatisticsValue(String entity, String status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("entity", entity).addValue(
                "status", status);

        ParameterizedRowMapper<Integer> mapper = new ParameterizedRowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("val");
            }
        };

        Map<String, Object> dbResults = dbEngineDialect.createJdbcCallForQuery(jdbcTemplate).withProcedureName("Getsystem_statistics")
                .returningResultSet("RETURN_VALUE", mapper).execute(parameterSource);

        return (Integer) DbFacadeUtils.asSingleResult((List<?>) (dbResults.get("RETURN_VALUE")));
    }

    @SuppressWarnings("unchecked")
    public List<Guid> getOrderedVmGuidsForRunMultipleActions(List<Guid> guids) {
        ParameterizedRowMapper<Guid> mapper = new ParameterizedRowMapper<Guid>() {
            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Guid.createGuidFromString(rs.getString("vm_guid"));
            }
        };

        // Constructing an IN clause of SQL that contains a list of GUIDs
        // The in clause looks like ('guid1','guid2','guid3')
        StringBuilder guidsSb = new StringBuilder();
        guidsSb.append("'").append(StringUtils.join(guids, "','")).append("'");

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guids", guidsSb
                .toString());

        Map<String, Object> dbResults = dbEngineDialect.createJdbcCallForQuery(jdbcTemplate).withProcedureName(
                "GetOrderedVmGuidsForRunMultipleActions").returningResultSet("RETURN_VALUE", mapper).execute(
                parameterSource);

        return (ArrayList<Guid>) dbResults.get("RETURN_VALUE");

    }

    /**
     * User presentation in GUI have a distinction between ADMIN/USER user. The distinction is determined by their
     * permissions or their group's permissions. when Permission with the role type Admin is found, set
     * the DbUser isAdmin flag to ADMIN Type or to USER otherwise. Make the change only if the value is different to
     * what it is saved to db
     *
     * @param userIds
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
    public boolean CheckDBConnection() {
        return (new SimpleJdbcCall(jdbcTemplate).withProcedureName("CheckDBConnection").execute() != null);
    }

    /**
     * Returns a singleton instance of {@link BookmarkDAO}.
     *
     * @return the dao
     */
    public BookmarkDAO getBookmarkDAO() {
        return getDAO(BookmarkDAO.class);

    }

    /**
     * Returns the singleton instance of {@link DbuserDAO}.
     *
     * @return the dao
     */
    public DbUserDAO getDbUserDAO() {
       return getDAO(DbUserDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDAO}.
     *
     * @return the dao
     */
    public VdsDAO getVdsDAO() {
        return getDAO(VdsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStaticDAO}.
     *
     * @return the dao
     */
    public VdsStaticDAO getVdsStaticDAO() {
        return getDAO(VdsStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDynamicDAO}.
     *
     * @return the dao
     */
    public VdsDynamicDAO getVdsDynamicDAO() {
        return getDAO(VdsDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStatisticsDAO}.
     *
     * @return the dao
     */
    public VdsStatisticsDAO getVdsStatisticsDAO() {
        return getDAO(VdsStatisticsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsSpmIdMapDAO}.
     *
     * @return the dao
     */
    public VdsSpmIdMapDAO getVdsSpmIdMapDAO() {
        return getDAO(VdsSpmIdMapDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsGroupDAO}.
     *
     * @return the dao
     */
    public VdsGroupDAO getVdsGroupDAO() {
        return getDAO(VdsGroupDAO.class);
    }

    /**
     * Returns the single instance of {@link AuditLogDAO}.
     *
     * @return the dao
     */
    public AuditLogDAO getAuditLogDAO() {
        return getDAO(AuditLogDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link LunDAO}.
     *
     * @return the dao
     */
    public LunDAO getLunDAO() {
        return getDAO(LunDAO.class);
    }

    /**
     * Returns the singleton instance of {@link InterfaceDAO}.
     *
     * @return the dao
     */
    public InterfaceDAO getInterfaceDAO() {
        return getDAO(InterfaceDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VmNetworkInterfaceDAO}.
     *
     * @return the dao
     */
    public VmNetworkInterfaceDAO getVmNetworkInterfaceDAO() {
        return getDAO(VmNetworkInterfaceDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VmNetworkInterfaceDAO}.
     *
     * @return the dao
     */
    public VmNetworkStatisticsDAO getVmNetworkStatisticsDAO() {
        return getDAO(VmNetworkStatisticsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link RoleGroupMapDAO}.
     *
     * @return the dao
     */
    public RoleGroupMapDAO getRoleGroupMapDAO() {
        return getDAO(RoleGroupMapDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmTemplateDAO}.
     *
     * @return the dao
     */
    public VmTemplateDAO getVmTemplateDAO() {
        return getDAO(VmTemplateDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDAO}.
     *
     * @return the dao
     */
    public VmDAO getVmDAO() {
        return getDAO(VmDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDynamicDAO}.
     *
     * @return the dao
     */
    public VmDynamicDAO getVmDynamicDAO() {
        return getDAO(VmDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link TagDAO}.
     *
     * @return the dao
     */
    public TagDAO getTagDAO() {
        return getDAO(TagDAO.class);
    }

    /**
     * Returns the singleton instance of {@link DiskDao}.
     *
     * @return the dao
     */
    public DiskDao getDiskDao() {
        return getDAO(DiskDao.class);
    }

    /**
     * Returns the singleton instance of {@link DiskImageDAO}.
     *
     * @return the dao
     */
    public DiskImageDAO getDiskImageDAO() {
        return getDAO(DiskImageDAO.class);
    }

    /**
     * Returns the singleton instance of {@link DiskImageDynamicDAO}.
     *
     * @return the dao
     */
    public DiskImageDynamicDAO getDiskImageDynamicDAO() {
        return getDAO(DiskImageDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link EventSubscriberDAO}.
     *
     * @return the dao
     */
    public EventDAO getEventDAO() {
        return getDAO(EventDAO.class);
    }

    /**
     * Returns the singleton instance of {@link ActionGroupDAO}.
     *
     * @return the dao
     */
    public ActionGroupDAO getActionGroupDAO() {
        return getDAO(ActionGroupDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link RoleDAO}.
     *
     * @return the dao
     */
    public RoleDAO getRoleDAO() {
        return getDAO(RoleDAO.class);
    }

    /**
     * Returns the singleton instance of {@link AsyncTaskDAO}.
     *
     * @return the dao
     */
    public AsyncTaskDAO getAsyncTaskDAO() {
        return getDAO(AsyncTaskDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link AdGroupDAO}.
     *
     * @return the dao
     */
    public AdGroupDAO getAdGroupDAO() {
        return getDAO(AdGroupDAO.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkDAO}.
     *
     * @return the dao
     */
    public NetworkDAO getNetworkDAO() {
        return getDAO(NetworkDAO.class);
    }

    /**
     * Returns the singleton instance of {@link NetworkClusterDAO}.
     *
     * @return the dao
     */
    public NetworkClusterDAO getNetworkClusterDAO() {
        return getDAO(NetworkClusterDAO.class);
    }

    /**
     * Returns the singleton instance of {@link PermissionDAO}.
     *
     * @return the dao
     */
    public PermissionDAO getPermissionDAO() {
        return getDAO(PermissionDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainDAO getStorageDomainDAO() {
        return getDAO(StorageDomainDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainStaticDAO getStorageDomainStaticDAO() {
        return getDAO(StorageDomainStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainDynamicDAO getStorageDomainDynamicDAO() {
        return getDAO(StorageDomainDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link RepoFileMetaDataDAO}.
     *
     * @return Repository file meta data dao.
     */
    public RepoFileMetaDataDAO getRepoFileMetaDataDao() {
        return getDAO(RepoFileMetaDataDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link SnapshotDao}.
     *
     * @return the dao
     */
    public SnapshotDao getSnapshotDao() {
        return getDAO(SnapshotDao.class);
    }

    /**
     * Retrieves the singleton instance of {@link StoragePoolDAO}.
     *
     * @return the dao
     */
    public StoragePoolDAO getStoragePoolDAO() {
        return getDAO(StoragePoolDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StoragePoolIsoMapDAO}.
     *
     * @return the dao
     */
    public StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDAO(StoragePoolIsoMapDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionDAO}.
     *
     * @return the dao
     */
    public StorageServerConnectionDAO getStorageServerConnectionDAO() {
        return getDAO(StorageServerConnectionDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionLunMapDAO}.
     *
     * @return the dao
     */
    public StorageServerConnectionLunMapDAO getStorageServerConnectionLunMapDAO() {
        return getDAO(StorageServerConnectionLunMapDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdcOptionDAO}.
     *
     * @return the dao
     */
    public VdcOptionDAO getVdcOptionDAO() {
        return getDAO(VdcOptionDAO.class);
    }

    /**
     * Returns the singleton instance of {@link BusinessEntitySnapshotDAO}.
     * @return
     */
    public BusinessEntitySnapshotDAO getBusinessEntitySnapshotDAO() {
        return getDAO(BusinessEntitySnapshotDAO.class);
    }


    /**
     * Returns the singleton instance of {@link VmPoolDAO}.
     *
     * @return the dao
     */
    public VmPoolDAO getVmPoolDAO() {
        return getDAO(VmPoolDAO.class);
    }

    public VmStaticDAO getVmStaticDAO() {
        return getDAO(VmStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VmStatisticsDAO}.
     *
     * @return the dao
     */
    public VmStatisticsDAO getVmStatisticsDAO() {
        return getDAO(VmStatisticsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link QuotaDAO}.
     *
     * @return the dao
     */
    public QuotaDAO getQuotaDAO() {
        return getDAO(QuotaDAO.class);
    }

    public VmDeviceDAO getVmDeviceDAO() {
        return getDAO(VmDeviceDAO.class);
    }

    /**
     *
     * Returns the singleton instance of {@link JobDao}.
     *
     * @return the dao
     */
    public JobDao getJobDao() {
        return getDAO(JobDao.class);
    }

    /**
     * Returns the singleton instance of {@link getJobSubjectEntityDao}.
     *
     * @return the dao
     */
    public JobSubjectEntityDao getJobSubjectEntityDao() {
        return getDAO(JobSubjectEntityDao.class);
    }

    /**
     * Returns the singleton instance of {@link StepDao}.
     *
     * @return the dao
     */
    public StepDao getStepDao() {
        return getDAO(StepDao.class);
    }

    /**
     * Returns the singleton instance of {@link GlusterVolumeDao}.
     *
     * @return the dao
     */
    public GlusterVolumeDao getGlusterVolumeDao() {
        return getDAO(GlusterVolumeDao.class);
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
}
