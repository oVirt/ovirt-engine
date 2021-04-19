package org.ovirt.engine.api.restapi.types;

import static java.util.stream.Collectors.joining;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.HostNicVirtualFunctionsConfiguration;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.IpVersion;
import org.ovirt.engine.api.model.Mac;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.UpdateHostNicVfsConfigParameters;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.BondMode;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;

public class HostNicMapper {
    private static final String OPTIONS_DELIMITER = "\\ ";
    private static final String OPTIONS_EQ = "\\=";

    @Mapping(from = HostNic.class, to = VdsNetworkInterface.class)
    public static VdsNetworkInterface map(HostNic model, VdsNetworkInterface template) {
        VdsNetworkInterface entity;
        if (template != null) {
            entity = template;
        } else if (model.isSetBonding()) {
            entity = new Bond();
        } else if (model.isSetVlan()) {
            entity = new Vlan();
        } else {
            entity = new Nic();
        }

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }
        if (model.isSetNetwork() && model.getNetwork().isSetName()) {
            entity.setNetworkName(model.getNetwork().getName());
        }
        if (model.isSetName()) {
            entity.setName(model.getName());
        }
        if (model.isSetBaseInterface()) {
            entity.setBaseInterface(model.getBaseInterface());
        }
        mapIpv4FromModel(model, entity);
        mapIpv6FromModel(model, entity);
        if (model.isSetMac() && model.getMac().isSetAddress()) {
            entity.setMacAddress(model.getMac().getAddress());
        }
        if (model.isSetBonding()) {
            entity.setBonded(true);
            if (model.getBonding().isSetOptions()) {

                List<Option> bondingOptions = model.getBonding().getOptions().getOptions();
                String optionsString = bondingOptions.stream()
                        .filter(Option::isSetName)
                        .map(x -> x.getName() + "=" + x.getValue())
                        .collect(joining(" "));

                entity.setBondOptions(optionsString);
            }
        }

        if (model.isSetQos()) {
            entity.setQos((HostNetworkQos) QosMapper.map(model.getQos(), null));
        }
        return entity;
    }

    private static void mapIpv4FromModel(HostNic model, VdsNetworkInterface entity) {
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setIpv4Address(model.getIp().getAddress());
            }
            if (model.getIp().isSetGateway()) {
                entity.setIpv4Gateway(model.getIp().getGateway());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setIpv4Subnet(model.getIp().getNetmask());
            }
        }
        if (model.isSetBootProtocol()) {
            Ipv4BootProtocol ipv4BootProtocol = Ipv4BootProtocolMapper.map(model.getBootProtocol());
            if (ipv4BootProtocol != null) {
                entity.setIpv4BootProtocol(ipv4BootProtocol);
            }
        }
    }

    private static void mapIpv6FromModel(HostNic model, VdsNetworkInterface entity) {
        if (model.isSetIpv6()) {
            if (model.getIpv6().isSetAddress()) {
                entity.setIpv6Address(model.getIpv6().getAddress());
            }
            if (model.getIpv6().isSetGateway()) {
                entity.setIpv6Gateway(model.getIpv6().getGateway());
            }
            if (model.getIpv6().isSetNetmask()) {
                try {
                    final Integer ipv6Prefix = Integer.valueOf(model.getIpv6().getNetmask());
                    entity.setIpv6Prefix(ipv6Prefix);
                } catch (NumberFormatException ignore) {
                }
            }
        }
        if (model.isSetIpv6BootProtocol()) {
            Ipv6BootProtocol ipv6BootProtocol = Ipv6BootProtocolMapper.map(model.getIpv6BootProtocol());
            if (ipv6BootProtocol != null) {
                entity.setIpv6BootProtocol(ipv6BootProtocol);
            }
        }
    }

    @Mapping(from = HostNic.class, to = Bond.class)
    public static Bond map(HostNic model, Bond template) {
        Bond entity = template == null ? new Bond() : template;

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }

        if (model.isSetName()) {
            entity.setName(model.getName());
        }

        if (model.isSetBonding()) {
            entity.setBonded(true);
            if (model.getBonding().isSetSlaves()) {
                entity.getSlaves().clear();
                for (HostNic slave : model.getBonding().getSlaves().getHostNics()) {
                    if (slave.isSetName()) {
                        entity.getSlaves().add(slave.getName());
                    }
                }
            }

            if (model.getBonding().isSetOptions()) {
                entity.setBondOptions(calculateBondingOptionsString(model));
            }
        }

        if (model.isSetQos()) {
            entity.setQos((HostNetworkQos) QosMapper.map(model.getQos(), null));
        }

        return entity;
    }

    private static String calculateBondingOptionsString(HostNic model) {
        List<Option> bondingOptions = model.getBonding().getOptions().getOptions();

        if (bondingOptions.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        appendBondingOption(builder, bondingOptions.get(0));

        for(int i = 1; i < bondingOptions.size(); i++) {
            builder.append(" ");
            appendBondingOption(builder, bondingOptions.get(i));
        }

        return builder.toString();
    }

    private static StringBuilder appendBondingOption(StringBuilder builder, Option opt) {
        return builder.append(opt.getName())
                .append("=")
                .append(opt.getValue());
    }

    @Mapping(from = VdsNetworkInterface.class, to = HostNic.class)
    public static HostNic map(VdsNetworkInterface entity, HostNic template) {
        HostNic model = template != null ? template : new HostNic();
        if (entity.getId() != null) {
            model.setId(entity.getId().toString());
        }
        if (entity.getNetworkName() != null) {
            model.setNetwork(new Network());
            model.getNetwork().setName(entity.getNetworkName());
        }
        if (entity.getName() != null) {
            model.setName(entity.getName());
        }
        if (entity.getBaseInterface() != null) {
            model.setBaseInterface(entity.getBaseInterface());
        }
        if (entity.getVlanId() != null) {
            model.setVlan(new org.ovirt.engine.api.model.Vlan());
            model.getVlan().setId(entity.getVlanId());
        }

        mapIpv4ToModel(entity, model);
        mapIpv6ToModel(entity, model);

        if (entity.getMacAddress() != null) {
            model.setMac(new Mac());
            model.getMac().setAddress(entity.getMacAddress());
        }
        if (entity.getStatistics().getStatus()!=InterfaceStatus.NONE) {
            NicStatus nicStatus = mapNicStatus(entity.getStatistics().getStatus());
            if (nicStatus != null) {
                model.setStatus(nicStatus);
            }
        }
        if(entity.getSpeed()!=null && entity.getSpeed()>0){
            model.setSpeed(entity.getSpeed() * 1000L * 1000);
        }
        if (!StringUtils.isEmpty(entity.getBondOptions())) {
            if(model.getBonding() == null) {
                model.setBonding(new Bonding());
            }
            model.getBonding().setOptions(new Options());
            for(String opt : entity.getBondOptions().split(OPTIONS_DELIMITER)){
                String[] option_pair = opt.split(OPTIONS_EQ);
                if(option_pair.length == 2){
                    Option option = new Option();
                    option.setName(option_pair[0]);
                    option.setValue(option_pair[1]);
                    option.setType(getType(option_pair));
                    model.getBonding().getOptions().getOptions().add(option);
                }
            }
            if (entity.getAdPartnerMac() != null) {
                model.getBonding().setAdPartnerMac(new Mac());
                model.getBonding().getAdPartnerMac().setAddress(entity.getAdPartnerMac());
            }
        }

        model.setMtu(entity.getMtu());
        model.setBridged(entity.isBridged());
        if (entity.getNetworkImplementationDetails() != null) {
            model.setCustomConfiguration(!entity.getNetworkImplementationDetails().isInSync());
        }

        HostNetworkQos qos = entity.getQos();
        if (qos != null) {
            model.setQos(QosMapper.map(qos, null));
        }

        if (entity.getAdAggregatorId() != null) {
            model.setAdAggregatorId(entity.getAdAggregatorId());
        }
        return model;
    }

    private static void mapIpv4ToModel(VdsNetworkInterface entity, HostNic model) {
        BootProtocol ipv4BootProtocol = Ipv4BootProtocolMapper.map(entity.getIpv4BootProtocol());
        if(ipv4BootProtocol!=null){
            model.setBootProtocol(ipv4BootProtocol);
        }

        if (entity.getIpv4Address() != null || entity.getIpv4Gateway() != null || entity.getIpv4Subnet() != null) {
            final Ip ipv4 = new Ip();
            ipv4.setVersion(IpVersion.V4);
            if (entity.getIpv4Address() != null) {
                ipv4.setAddress(entity.getIpv4Address());
            }
            if (entity.getIpv4Gateway() != null) {
                ipv4.setGateway(entity.getIpv4Gateway());
            }
            if (entity.getIpv4Subnet() != null) {
                ipv4.setNetmask(entity.getIpv4Subnet());
            }
            model.setIp(ipv4);
        }
    }

    private static void mapIpv6ToModel(VdsNetworkInterface entity, HostNic model) {
        BootProtocol ipv6BootProtocol = Ipv6BootProtocolMapper.map(entity.getIpv6BootProtocol());
        if(ipv6BootProtocol!=null){
            model.setIpv6BootProtocol(ipv6BootProtocol);
        }

        if (entity.getIpv6Address() != null || entity.getIpv6Gateway() != null || entity.getIpv6Prefix() != null) {
            final Ip ipv6 = new Ip();
            ipv6.setVersion(IpVersion.V6);
            if (entity.getIpv6Address() != null) {
                ipv6.setAddress(entity.getIpv6Address());
            }
            if (entity.getIpv6Gateway() != null) {
                ipv6.setGateway(entity.getIpv6Gateway());
            }
            if (entity.getIpv6Prefix() != null) {
                ipv6.setNetmask(entity.getIpv6Prefix().toString());
            }
            model.setIpv6(ipv6);
        }
    }

    private static String getType(final String[] optionPair) {
        if (!StringUtils.isEmpty(optionPair[0]) && optionPair[0].equals("mode") && !StringUtils.isEmpty(optionPair[1])) {
            Integer mode = tryParse(optionPair[1]);
            if(mode != null){
                /*
                 *  Return the description of this bond-mode, as it appears in BondMode enum
                 *  in the Engine (truncating the unnecessary "(Mode x") prefix from it).
                 *
                 *  This solution is temporary, the final solution will include creating
                 *  a parallel enum in the API layer and using its values here.
                 */
                return BondMode.values()[mode].getDescription().substring(9);
            }
        }
        return null;
    }

    public static Integer tryParse(String text) {
        try {
          return Integer.valueOf(text);
        } catch (NumberFormatException e) {
          return null;
        }
      }

    public static NicStatus mapNicStatus(InterfaceStatus status) {
        if (status == null) {
            return null;
        }
        switch (status) {
        case UP:
            return NicStatus.UP;
        case DOWN:
            return NicStatus.DOWN;
        default:
            return null;
        }
    }

    @Mapping(from = HostNicVfsConfig.class, to = HostNicVirtualFunctionsConfiguration.class)
    public static HostNicVirtualFunctionsConfiguration map(HostNicVfsConfig entity,
            HostNicVirtualFunctionsConfiguration apiModel) {
        apiModel.setAllNetworksAllowed(entity.isAllNetworksAllowed());
        apiModel.setMaxNumberOfVirtualFunctions(entity.getMaxNumOfVfs());
        apiModel.setNumberOfVirtualFunctions(entity.getNumOfVfs());
        return apiModel;
    }

    @Mapping(from = HostNicVirtualFunctionsConfiguration.class, to = UpdateHostNicVfsConfigParameters.class)
    public static UpdateHostNicVfsConfigParameters map(HostNicVirtualFunctionsConfiguration apiModel,
            UpdateHostNicVfsConfigParameters params) {
        if (apiModel.isSetAllNetworksAllowed()) {
            params.setAllNetworksAllowed(apiModel.isAllNetworksAllowed());
        }
        if (apiModel.isSetNumberOfVirtualFunctions()) {
            params.setNumOfVfs(apiModel.getNumberOfVirtualFunctions());
        }
        return params;
    }

    @Mapping(from = HostNicVfsConfig.class, to = UpdateHostNicVfsConfigParameters.class)
    public static UpdateHostNicVfsConfigParameters map(HostNicVfsConfig entity,
            UpdateHostNicVfsConfigParameters params) {
        params.setNicId(entity.getNicId());
        params.setAllNetworksAllowed(entity.isAllNetworksAllowed());
        params.setNumOfVfs(entity.getNumOfVfs());
        return params;
    }
}
