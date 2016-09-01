package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmIconDefault;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmIconDefaultDaoImpl extends DefaultGenericDao<VmIconDefault, Guid>
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

    @Override public VmIconDefault getByOperatingSystemId(int osId) {
        return getCallsHandler().executeRead("GetVmIconDefaultByVmIconDefaultOsId", createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue(OS_ID_COLUMN, osId));
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
        return (rs, rowNum) -> {
            VmIconDefault iconDefaults = new VmIconDefault();
            iconDefaults.setId(getGuid(rs, ID_COLUMN));
            iconDefaults.setOsId(rs.getInt(OS_ID_COLUMN));
            iconDefaults.setSmallIconId(getGuid(rs, SMALL_ICON_ID_COLUMN));
            iconDefaults.setLargeIconId(getGuid(rs, LARGE_ICON_ID_COLUMN));
            return iconDefaults;
        };
    }
}
