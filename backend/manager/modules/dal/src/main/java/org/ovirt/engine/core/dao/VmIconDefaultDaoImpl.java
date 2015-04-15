package org.ovirt.engine.core.dao;


import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import javax.inject.Named;
import javax.inject.Singleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Named
@Singleton
public class VmIconDefaultDaoImpl extends DefaultGenericDaoDbFacade<VmIconDefault, Guid>
                                   implements VmIconDefaultDao {

    private static final String ID_COLUMN = "id";
    private static final String OS_ID_COLUMN = "os_id";
    private static final String SMALL_ICON_ID_COLUMN = "small_icon_id";
    private static final String LARGE_ICON_ID_COLUMN = "large_icon_id";

    public VmIconDefaultDaoImpl() {
        super("VmIconDefault");
    }

    @Override
    public List<VmIconDefault> getByLargeIconId(Guid largeIconId) {
        return getCallsHandler().executeReadList("GetVmIconDefaultByVmIconDefaultLargeIconId", createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue(LARGE_ICON_ID_COLUMN, largeIconId));
    }

    @Override
    public void removeAll() {
        getCallsHandler().executeModification("DeleteAllFromVmIconDefaults", getCustomMapSqlParameterSource());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmIconDefault entity) {
        return createIdParameterMapper(entity.getId())
                .addValue(OS_ID_COLUMN, entity.getOsId())
                .addValue(SMALL_ICON_ID_COLUMN, entity.getSmallIconId())
                .addValue(LARGE_ICON_ID_COLUMN, entity.getLargeIconId());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue(ID_COLUMN, id);
    }

    @Override
    protected RowMapper<VmIconDefault> createEntityRowMapper() {
        return new RowMapper<VmIconDefault>() {
            @Override
            public VmIconDefault mapRow(ResultSet rs, int rowNum) throws SQLException {
                VmIconDefault iconDefaults = new VmIconDefault();
                iconDefaults.setId(getGuid(rs, ID_COLUMN));
                iconDefaults.setOsId(rs.getInt(OS_ID_COLUMN));
                iconDefaults.setSmallIconId(getGuid(rs, SMALL_ICON_ID_COLUMN));
                iconDefaults.setLargeIconId(getGuid(rs, LARGE_ICON_ID_COLUMN));
                return iconDefaults;
            }
        };
    }
}
