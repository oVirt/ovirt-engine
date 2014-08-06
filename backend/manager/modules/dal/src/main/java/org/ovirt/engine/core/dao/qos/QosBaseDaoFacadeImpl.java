package org.ovirt.engine.core.dao.qos;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public abstract class QosBaseDaoFacadeImpl<T extends QosBase> extends DefaultGenericDaoDbFacade<T, Guid> implements QosDao<T> {
    private static final String QOS = "qos";
    private final QosType qosType;

    public QosBaseDaoFacadeImpl(QosType qosType) {
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
        MapSqlParameterSource map = createIdParameterMapper(obj.getId())
                .addValue("qos_type", getQosType())
                .addValue("name", obj.getName())
                .addValue("description", obj.getDescription())
                .addValue("storage_pool_id", obj.getStoragePoolId());
        return map;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource()
                .addValue("id", guid);
    }

    protected static abstract class QosBaseDaoFacadaeImplMapper<M extends QosBase> implements RowMapper<M> {
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
