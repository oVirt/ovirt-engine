package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.KdumpFlowStatus;
import org.ovirt.engine.core.common.businessentities.VdsKdumpStatus;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class VdsKdumpStatusDaoImpl  extends BaseDao implements VdsKdumpStatusDao {
    private static RowMapper<VdsKdumpStatus> vdsKdumpStatusMapper = (rs, rowNum) -> {
        VdsKdumpStatus entity = new VdsKdumpStatus();
        entity.setVdsId(Guid.createGuidFromStringDefaultEmpty(rs.getString("vds_id")));
        entity.setStatus(KdumpFlowStatus.forString(rs.getString("status")));
        entity.setAddress(rs.getString("address"));
        return entity;
    };

    @Override
    public void update(VdsKdumpStatus vdsKdumpStatus){
        getCallsHandler().executeModification(
                "UpsertKdumpStatus",
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", vdsKdumpStatus.getVdsId())
                        .addValue("status", vdsKdumpStatus.getStatus().getAsString())
                        .addValue("address", vdsKdumpStatus.getAddress())
        );
    }

    /**
     * Updates kdump status record for specified VDS
     *
     * @param ip
     *            IP address of host to update status for
     * @param vdsKdumpStatus
     *            updated kdump status
     */
    public void updateForIp(String ip, VdsKdumpStatus vdsKdumpStatus){
        getCallsHandler().executeModification(
                "UpsertKdumpStatusForIp",
                getCustomMapSqlParameterSource()
                        .addValue("ip", ip)
                        .addValue("status", vdsKdumpStatus.getStatus().getAsString())
                        .addValue("address", vdsKdumpStatus.getAddress())
        );
    }



    @Override
    public void remove(Guid vdsId) {
        getCallsHandler().executeModification(
                "RemoveFinishedKdumpStatusForVds",
                getCustomMapSqlParameterSource().addValue("vds_id", vdsId)
        );
    }

    @Override
    public VdsKdumpStatus get(Guid vdsId) {
        return getCallsHandler().executeRead(
                "GetKdumpStatusForVds",
                vdsKdumpStatusMapper,
                getCustomMapSqlParameterSource().addValue("vds_id", vdsId)
        );
    }

    @Override
    public List<VdsKdumpStatus> getAllUnfinishedVdsKdumpStatus(){
        return getCallsHandler().executeReadList(
                "GetAllUnfinishedVdsKdumpStatus",
                vdsKdumpStatusMapper,
                getCustomMapSqlParameterSource()
        );
    }
}
