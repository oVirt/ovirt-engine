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
import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.VdsDynamic;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.VdsStatistics;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.ActionGroupDAO;
import org.ovirt.engine.core.dao.AdGroupDAO;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.dao.AuditLogDAO;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.BookmarkDAO;
import org.ovirt.engine.core.dao.BusinessEntitySnapshotDAO;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.DaoFactory;
import org.ovirt.engine.core.dao.DbUserDAO;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.DiskImageDynamicDAO;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.EventDAO;
import org.ovirt.engine.core.dao.GenericDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.JobDao;
import org.ovirt.engine.core.dao.JobSubjectEntityDao;
import org.ovirt.engine.core.dao.LunDAO;
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
import org.ovirt.engine.core.dao.VmAndTemplatesGenerationsDAO;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmDynamicDAO;
import org.ovirt.engine.core.dao.VmGuestAgentInterfaceDao;
import org.ovirt.engine.core.dao.VmPoolDAO;
import org.ovirt.engine.core.dao.VmStaticDAO;
import org.ovirt.engine.core.dao.VmStatisticsDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterOptionDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.dao.network.NetworkViewDao;
import org.ovirt.engine.core.dao.network.VmNetworkInterfaceDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDao;
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
            put(StoragePoolIsoMap.class, StoragePoolIsoMapDAO.class);
            put(StorageDomainStatic.class, StorageDomainStaticDAO.class);
            put(StorageDomainDynamic.class, StorageDomainDynamicDAO.class);
            put(VdsStatic.class, VdsStaticDAO.class);
            put(VdsDynamic.class, VdsDynamicDAO.class);
            put(VdsStatistics.class, VdsStatisticsDAO.class);
            put(vds_spm_id_map.class, VdsSpmIdMapDAO.class);
            put(Role.class, RoleDAO.class);
            put(VmTemplate.class, VmTemplateDAO.class);
            put(VmDynamic.class, VmDynamicDAO.class);
            put(VmStatic.class, VmStaticDAO.class);
            put(VmStatistics.class, VmStatisticsDAO.class);
            put(BaseDisk.class, BaseDiskDao.class);
            put(DiskImage.class, BaseDiskDao.class);
            put(DiskImageDynamic.class, DiskImageDynamicDAO.class);
            put(VmNetworkInterface.class, VmNetworkInterfaceDao.class);
            put(VmNetworkStatistics.class, VmNetworkStatisticsDao.class);
            put(Network.class, NetworkDao.class);
            put(Snapshot.class, SnapshotDao.class);
            put(VmDevice.class, VmDeviceDAO.class);
            put(image_storage_domain_map.class, ImageStorageDomainMapDao.class);
            put(permissions.class, PermissionDAO.class);
            put(Image.class, ImageDao.class);
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
        return getDao(daoType);
    }

    protected <T extends DAO> T getDao(Class<T> daoType) {
        T dao = DaoFactory.get(daoType);
        if (dao instanceof BaseDAODbFacade) {
            BaseDAODbFacade dbFacadeDAO = (BaseDAODbFacade)dao;
            dbFacadeDAO.setTemplate(jdbcTemplate);
            dbFacadeDAO.setDialect(dbEngineDialect);
            dbFacadeDAO.setDbFacade(this);
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

    public NGuid getEntityPermissionsForUserAndGroups(Guid userId, String groupIds, ActionGroup actionGroup, Guid objectId,
            VdcObjectType vdcObjectType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("user_id", userId)
                .addValue("group_ids", groupIds).addValue("action_group_id", actionGroup.getId()).addValue("object_id", objectId).addValue(
                        "object_type_id", vdcObjectType.getValue());

        String resultKey = "permission_id";
        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withProcedureName("get_entity_permissions_for_user_and_groups")
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

    public boolean isStoragePoolMasterUp(Guid storagePoolId) {
        List<StorageDomain> domains = getStorageDomainDao().getAllForStoragePool(storagePoolId);
        StorageDomain master = LinqUtils.firstOrNull(domains, new Predicate<StorageDomain>() {
            @Override
            public boolean eval(StorageDomain storage_domains) {
                return storage_domains.getStorageDomainType() == StorageDomainType.Master;
            }
        });
        return master != null
                && (master.getStatus() == StorageDomainStatus.Active || master.getStatus() == StorageDomainStatus.Unknown);
    }

    public Integer getSystemStatisticsValue(String entity) {
        return getSystemStatisticsValue(entity, "");
    }

    public Integer getSystemStatisticsValue(String entity, String status) {
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
    public boolean checkDBConnection() {
        return (new SimpleJdbcCall(jdbcTemplate).withProcedureName("CheckDBConnection").execute() != null);
    }

    /**
     * Returns a singleton instance of {@link BookmarkDAO}.
     *
     * @return the dao
     */
    public BookmarkDAO getBookmarkDao() {
        return getDao(BookmarkDAO.class);

    }

    /**
     * Returns the singleton instance of {@link DbuserDAO}.
     *
     * @return the dao
     */
    public DbUserDAO getDbUserDao() {
       return getDao(DbUserDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDAO}.
     *
     * @return the dao
     */
    public VdsDAO getVdsDao() {
        return getDao(VdsDAO.class);
    }


    /**
     * Returns the singleton instance of {@link VmAndTemplatesGenerationsDAO}.
     *
     * @return the dao
     */
    public VmAndTemplatesGenerationsDAO getVmAndTemplatesGenerationsDao() {
        return getDao(VmAndTemplatesGenerationsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStaticDAO}.
     *
     * @return the dao
     */
    public VdsStaticDAO getVdsStaticDao() {
        return getDao(VdsStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsDynamicDAO}.
     *
     * @return the dao
     */
    public VdsDynamicDAO getVdsDynamicDao() {
        return getDao(VdsDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsStatisticsDAO}.
     *
     * @return the dao
     */
    public VdsStatisticsDAO getVdsStatisticsDao() {
        return getDao(VdsStatisticsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsSpmIdMapDAO}.
     *
     * @return the dao
     */
    public VdsSpmIdMapDAO getVdsSpmIdMapDao() {
        return getDao(VdsSpmIdMapDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdsGroupDAO}.
     *
     * @return the dao
     */
    public VdsGroupDAO getVdsGroupDao() {
        return getDao(VdsGroupDAO.class);
    }

    /**
     * Returns the single instance of {@link AuditLogDAO}.
     *
     * @return the dao
     */
    public AuditLogDAO getAuditLogDao() {
        return getDao(AuditLogDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link LunDAO}.
     *
     * @return the dao
     */
    public LunDAO getLunDao() {
        return getDao(LunDAO.class);
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
     * Returns the singleton instance of {@link RoleGroupMapDAO}.
     *
     * @return the dao
     */
    public RoleGroupMapDAO getRoleGroupMapDao() {
        return getDao(RoleGroupMapDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmTemplateDAO}.
     *
     * @return the dao
     */
    public VmTemplateDAO getVmTemplateDao() {
        return getDao(VmTemplateDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDAO}.
     *
     * @return the dao
     */
    public VmDAO getVmDao() {
        return getDao(VmDAO.class);
    }

    /**
     * * Returns the singleton instance of {@link VmDynamicDAO}.
     *
     * @return the dao
     */
    public VmDynamicDAO getVmDynamicDao() {
        return getDao(VmDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link TagDAO}.
     *
     * @return the dao
     */
    public TagDAO getTagDao() {
        return getDao(TagDAO.class);
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
     * Returns the singleton instance of {@link DiskImageDAO}.
     *
     * @return the dao
     */
    public DiskImageDAO getDiskImageDao() {
        return getDao(DiskImageDAO.class);
    }

    /**
     * Returns the singleton instance of {@link DiskImageDynamicDAO}.
     *
     * @return the dao
     */
    public DiskImageDynamicDAO getDiskImageDynamicDao() {
        return getDao(DiskImageDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link EventSubscriberDAO}.
     *
     * @return the dao
     */
    public EventDAO getEventDao() {
        return getDao(EventDAO.class);
    }

    /**
     * Returns the singleton instance of {@link ActionGroupDAO}.
     *
     * @return the dao
     */
    public ActionGroupDAO getActionGroupDao() {
        return getDao(ActionGroupDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link RoleDAO}.
     *
     * @return the dao
     */
    public RoleDAO getRoleDao() {
        return getDao(RoleDAO.class);
    }

    /**
     * Returns the singleton instance of {@link AsyncTaskDAO}.
     *
     * @return the dao
     */
    public AsyncTaskDAO getAsyncTaskDao() {
        return getDao(AsyncTaskDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link AdGroupDAO}.
     *
     * @return the dao
     */
    public AdGroupDAO getAdGroupDao() {
        return getDao(AdGroupDAO.class);
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
     * Returns the singleton instance of {@link PermissionDAO}.
     *
     * @return the dao
     */
    public PermissionDAO getPermissionDao() {
        return getDao(PermissionDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainDAO getStorageDomainDao() {
        return getDao(StorageDomainDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainStaticDAO getStorageDomainStaticDao() {
        return getDao(StorageDomainStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link StorageDomainDAO}.
     *
     * @return the dao
     */
    public StorageDomainDynamicDAO getStorageDomainDynamicDao() {
        return getDao(StorageDomainDynamicDAO.class);
    }

    /**
     * Returns the singleton instance of {@link RepoFileMetaDataDAO}.
     *
     * @return Repository file meta data dao.
     */
    public RepoFileMetaDataDAO getRepoFileMetaDataDao() {
        return getDao(RepoFileMetaDataDAO.class);
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
     * Retrieves the singleton instance of {@link StoragePoolDAO}.
     *
     * @return the dao
     */
    public StoragePoolDAO getStoragePoolDao() {
        return getDao(StoragePoolDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StoragePoolIsoMapDAO}.
     *
     * @return the dao
     */
    public StoragePoolIsoMapDAO getStoragePoolIsoMapDao() {
        return getDao(StoragePoolIsoMapDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionDAO}.
     *
     * @return the dao
     */
    public StorageServerConnectionDAO getStorageServerConnectionDao() {
        return getDao(StorageServerConnectionDAO.class);
    }

    /**
     * Retrieves the singleton instance of {@link StorageServerConnectionLunMapDAO}.
     *
     * @return the dao
     */
    public StorageServerConnectionLunMapDAO getStorageServerConnectionLunMapDao() {
        return getDao(StorageServerConnectionLunMapDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VdcOptionDAO}.
     *
     * @return the dao
     */
    public VdcOptionDAO getVdcOptionDao() {
        return getDao(VdcOptionDAO.class);
    }

    /**
     * Returns the singleton instance of {@link BusinessEntitySnapshotDAO}.
     * @return
     */
    public BusinessEntitySnapshotDAO getBusinessEntitySnapshotDao() {
        return getDao(BusinessEntitySnapshotDAO.class);
    }


    /**
     * Returns the singleton instance of {@link VmPoolDAO}.
     *
     * @return the dao
     */
    public VmPoolDAO getVmPoolDao() {
        return getDao(VmPoolDAO.class);
    }

    public VmStaticDAO getVmStaticDao() {
        return getDao(VmStaticDAO.class);
    }

    /**
     * Returns the singleton instance of {@link VmStatisticsDAO}.
     *
     * @return the dao
     */
    public VmStatisticsDAO getVmStatisticsDao() {
        return getDao(VmStatisticsDAO.class);
    }

    /**
     * Returns the singleton instance of {@link QuotaDAO}.
     *
     * @return the dao
     */
    public QuotaDAO getQuotaDao() {
        return getDao(QuotaDAO.class);
    }

    public VmDeviceDAO getVmDeviceDao() {
        return getDao(VmDeviceDAO.class);
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
     * Returns the singleton instance of {@link getJobSubjectEntityDao}.
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
}
