package org.ovirt.engine.core.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmGuestAgentInterfaceDaoImpl extends BaseDao implements VmGuestAgentInterfaceDao {

    private static final String DELIMITER = ",";

    @Override
    public List<VmGuestAgentInterface> getAllForVm(Guid vmId) {
        return getAllForVm(vmId, null, false);
    }

    @Override
    public List<VmGuestAgentInterface> getAllForVm(Guid vmId, Guid userId, boolean filtered) {
        return getCallsHandler().executeReadList("GetVmGuestAgentInterfacesByVmId",
                vmGuestAgentInterfaceRowMapper,
                getCustomMapSqlParameterSource().addValue("vm_id", vmId)
                        .addValue("user_id", userId)
                        .addValue("filtered", filtered));
    }

    @Override
    public void save(VmGuestAgentInterface vmGuestAgentInterface) {
        getCallsHandler().executeModification("InsertVmGuestAgentInterface",
                createFullParametersMapper(vmGuestAgentInterface));
    }

    @Override
    public void removeAllForVms(Collection<Guid> vmIds) {
        getCallsHandler().executeModification("DeleteVmGuestAgentInterfacesByVmIds",
                getCustomMapSqlParameterSource().addValue("vm_ids", createArrayOfUUIDs(vmIds)));
    }

    protected MapSqlParameterSource createFullParametersMapper(VmGuestAgentInterface entity) {
        return getCustomMapSqlParameterSource()
                .addValue("vm_id", entity.getVmId())
                .addValue("interface_name", entity.getInterfaceName())
                .addValue("mac_address", entity.getMacAddress())
                .addValue("ipv4_addresses", getIpAddressesAsString(entity.getIpv4Addresses()))
                .addValue("ipv6_addresses", getIpAddressesAsString(entity.getIpv6Addresses()));
    }

    private static String getIpAddressesAsString(List<String> ipAddresses) {
        return StringUtils.join(ipAddresses, DELIMITER);
    }

    protected static final RowMapper<VmGuestAgentInterface>  vmGuestAgentInterfaceRowMapper = (rs, rowNum) -> {
        VmGuestAgentInterface vmGuestAgentInterface = new VmGuestAgentInterface();
        vmGuestAgentInterface.setVmId(getGuidDefaultEmpty(rs, "vm_id"));
        vmGuestAgentInterface.setInterfaceName(rs.getString("interface_name"));
        vmGuestAgentInterface.setMacAddress(rs.getString("mac_address"));
        vmGuestAgentInterface.setIpv4Addresses(getListOfIpAddresses(rs.getString("ipv4_addresses")));
        vmGuestAgentInterface.setIpv6Addresses(getListOfIpAddresses(rs.getString("ipv6_addresses")));
        return vmGuestAgentInterface;
    };

    private static List<String> getListOfIpAddresses(String ipAddressesAsString) {
        return ipAddressesAsString == null ? null
                : Arrays.asList(StringUtils.split(ipAddressesAsString, DELIMITER));
    }
}
