package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class QosBaseDaoImpl<T extends QosBase> extends DefaultGenericDao<T, Guid> implements QosDao<T> {
    private static final String QOS = "qos";
    private final QosType qosType;

    public QosBaseDaoImpl(QosType qosType) {
        super(QOS);
        this.qosType = qosType;
        setProcedureNameForSave(MessageFormat.format(DEFAULT_SAVE_PROCEDURE_FORMAT, qosType.name() + QOS));
        setProcedureNameForUpdate(MessageFormat.format(DEFAULT_UPDATE_PROCEDURE_FORMAT, qosType.name() + QOS));
    }

    /**
     * @return qos type for derived qos dao
     */
    protected QosType getQosType() {
        return qosType;
    }

    @Override
    public List<T> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<T> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        if (userID != null) {
            parameterSource = parameterSource.addValue("user_id", userID.getUuid());
        }
        if (isFiltered) {
            switch (getQosType()) {
                case  NETWORK:
                    return getCallsHandler().executeReadList("GetAllNetworkQos",
                        createEntityRowMapper(),
                        parameterSource);
                case CPU:
                    return getCallsHandler().executeReadList("GetAllCpuQos",
                        createEntityRowMapper(),
                        parameterSource);
                case STORAGE:
                    return getCallsHandler().executeReadList("GetAllStorageQos",
                        createEntityRowMapper(),
                        parameterSource);
                case HOSTNETWORK:
                    return getCallsHandler().executeReadList("GetAllHostNetworkQos",
                        createEntityRowMapper(),
                        parameterSource);
            }
        }
        parameterSource = parameterSource.addValue("qos_type", getQosType());
        return getCallsHandler().executeReadList("GetAllQosByQosType",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public List<T> getAllForStoragePoolId(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId.getUuid())
                .addValue("qos_type", getQosType());
        return getCallsHandler().executeReadList("GetAllQosForStoragePoolByQosType",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(T obj) {
        return createIdParameterMapper(obj.getId())
                .addValue("qos_type", getQosType())
                .addValue("name", obj.getName())
                .addValue("description", obj.getDescription())
                .addValue("storage_pool_id", obj.getStoragePoolId());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource()
                .addValue("id", guid);
    }

    protected abstract static class QosBaseDaoFacadaeImplMapper<M extends QosBase> implements RowMapper<M> {
        @Override
        public M mapRow(ResultSet rs, int rowNum) throws SQLException {
            M entity = createQosEntity(rs);
            entity.setId(getGuid(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setDescription(rs.getString("description"));
            return entity;
        }

        protected abstract M createQosEntity(ResultSet rs) throws SQLException;
    }

}
