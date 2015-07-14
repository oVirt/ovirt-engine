package org.ovirt.engine.api.restapi.types;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.common.util.StatusUtils;
import org.ovirt.engine.api.model.Bonding;
import org.ovirt.engine.api.model.BootProtocol;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.IP;
import org.ovirt.engine.api.model.MAC;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NicStatus;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.model.Options;
import org.ovirt.engine.api.model.VLAN;
import org.ovirt.engine.api.restapi.utils.CustomPropertiesParser;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;

public class HostNicMapper {
    private static final String OPTIONS_DELIMITER = "\\ ";
    private static final String OPTIONS_EQ = "\\=";
    private static final String[] BONDING_MODS = new String[]{"Active-Backup",
                                                              "Load balance (balance-xor)",
                                                              null,
                                                              "Dynamic link aggregation (802.3ad)",
                                                              "Adaptive transmit load balancing (balance-tlb)"};

    @Mapping(from = HostNIC.class, to = VdsNetworkInterface.class)
    public static VdsNetworkInterface map(HostNIC model, VdsNetworkInterface template) {
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
        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setAddress(model.getIp().getAddress());
            }
            if (model.getIp().isSetGateway()) {
                entity.setGateway(model.getIp().getGateway());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setSubnet(model.getIp().getNetmask());
            }
        }
        if (model.isSetMac() && model.getMac().isSetAddress()) {
            entity.setMacAddress(model.getMac().getAddress());
        }
        if (model.isSetBonding()) {
            entity.setBonded(true);
            if (model.getBonding().isSetOptions()) {
                StringBuffer buf = new StringBuffer();
                for(Option opt : model.getBonding().getOptions().getOptions()){
                    buf.append(opt.getName() + "=" + opt.getValue() + " ");
                }
                entity.setBondOptions(buf.toString().substring(0, buf.length() - 1));
            }
        }
        if(model.isSetBootProtocol()){
            NetworkBootProtocol networkBootProtocol = BootProtocolMapper.map(BootProtocol.fromValue(model.getBootProtocol()), null);
            if(networkBootProtocol != null){
                entity.setBootProtocol(networkBootProtocol);
            }
        }
        if (model.isSetProperties()) {
            entity.setCustomProperties(CustomPropertiesParser.toMap(model.getProperties()));
        }
        return entity;
    }

    @Mapping(from = VdsNetworkInterface.class, to = HostNIC.class)
    public static HostNIC map(VdsNetworkInterface entity, HostNIC template) {
        HostNIC model = template != null ? template : new HostNIC();
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
            model.setVlan(new VLAN());
            model.getVlan().setId(entity.getVlanId());
        }
        if (entity.getAddress() != null || entity.getGateway() != null || entity.getSubnet() != null) {
            model.setIp(new IP());
            if (entity.getAddress() != null) {
                model.getIp().setAddress(entity.getAddress());
            }
            if (entity.getGateway() != null) {
                model.getIp().setGateway(entity.getGateway());
            }
            if (entity.getSubnet() != null) {
                model.getIp().setNetmask(entity.getSubnet());
            }
        }
        if (entity.getMacAddress() != null) {
            model.setMac(new MAC());
            model.getMac().setAddress(entity.getMacAddress());
        }
        if(entity.getStatistics().getStatus()!=InterfaceStatus.NONE){
            NicStatus nicStatus = map(entity.getStatistics().getStatus(), null);
            if(nicStatus!=null){
                model.setStatus(StatusUtils.create(nicStatus));
            }
        }
        if(entity.getSpeed()!=null && entity.getSpeed()>0){
            model.setSpeed(entity.getSpeed() * 1000L * 1000);
        }
        if (!StringUtils.isEmpty(entity.getBondOptions())) {
            if(model.getBonding() == null) model.setBonding(new Bonding());
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
        }

        BootProtocol bootProtocol = BootProtocolMapper.map(entity.getBootProtocol(), null);
        if(bootProtocol!=null){
            model.setBootProtocol(bootProtocol.value());
        }
        model.setMtu(entity.getMtu());
        model.setBridged(entity.isBridged());
        if (entity.getNetworkImplementationDetails() != null) {
            model.setCustomConfiguration(!entity.getNetworkImplementationDetails().isInSync());
        }

        if (entity.hasCustomProperties()) {
            model.setProperties(CustomPropertiesParser.fromMap(entity.getCustomProperties()));
        }

        return model;
    }

    private static String getType(final String[] optionPair) {
        if (!StringUtils.isEmpty(optionPair[0]) && optionPair[0].equals("mode") && !StringUtils.isEmpty(optionPair[1])) {
            Integer mode = tryParse(optionPair[1]);
            if(mode != null && mode > 0 && mode < 6){
                return BONDING_MODS[mode - 1];
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

    @Mapping(from = InterfaceStatus.class, to = NicStatus.class)
    public static NicStatus map(InterfaceStatus interfaceStatus, NicStatus template) {
        if(interfaceStatus!=null){
            switch (interfaceStatus) {
            case UP:
                return NicStatus.UP;
            case DOWN:
                return NicStatus.DOWN;
            default:
                return null;
            }
        }
        return null;
    }
}
