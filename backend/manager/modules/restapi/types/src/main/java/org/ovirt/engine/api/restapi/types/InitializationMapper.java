package org.ovirt.engine.api.restapi.types;

import static org.ovirt.engine.api.model.CloudInitNetworkProtocol.ENI;
import static org.ovirt.engine.api.model.CloudInitNetworkProtocol.OPENSTACK_METADATA;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.api.model.Initialization;
import org.ovirt.engine.api.model.Ip;
import org.ovirt.engine.api.model.NicConfiguration;
import org.ovirt.engine.api.model.NicConfigurations;
import org.ovirt.engine.api.utils.IntegerParser;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmInitNetwork;
import org.ovirt.engine.core.common.businessentities.network.CloudInitNetworkProtocol;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpInfoFetcher;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpv4InfoFetcher;
import org.ovirt.engine.core.utils.network.vm.VmInitNetworkIpv6InfoFetcher;

public class InitializationMapper {
    @Mapping(from = NicConfiguration.class, to = VmInitNetwork.class)
    public static VmInitNetwork map(NicConfiguration model, VmInitNetwork template) {
        VmInitNetwork entity = template != null ? template : new VmInitNetwork();

        if (model.isSetName()) {
            entity.setName(model.getName());
        }

        if (model.isSetOnBoot()) {
            entity.setStartOnBoot(model.isOnBoot());
        }

        if (model.isSetBootProtocol()) {
            entity.setBootProtocol(Ipv4BootProtocolMapper.map(model.getBootProtocol()));
        }

        if (model.isSetIp()) {
            if (model.getIp().isSetAddress()) {
                entity.setIp(model.getIp().getAddress());
            }
            if (model.getIp().isSetNetmask()) {
                entity.setNetmask(model.getIp().getNetmask());
            }

            if (model.getIp().isSetGateway()) {
                entity.setGateway(model.getIp().getGateway());
            }
        }

        if (model.isSetIpv6BootProtocol()) {
            entity.setIpv6BootProtocol(Ipv6BootProtocolMapper.map(model.getIpv6BootProtocol()));
        }

        if (model.isSetIpv6()) {
            if (model.getIpv6().isSetAddress()) {
                entity.setIpv6Address(model.getIpv6().getAddress());
            }
            if (model.getIpv6().isSetNetmask()) {
                entity.setIpv6Prefix((int) IntegerParser.parseUnsignedInt(model.getIpv6().getNetmask()));
            }

            if (model.getIpv6().isSetGateway()) {
                entity.setIpv6Gateway(model.getIpv6().getGateway());
            }
        }

        return entity;
    }

    @Mapping(from = VmInitNetwork.class, to = NicConfiguration.class)
    public static NicConfiguration map(VmInitNetwork entity, NicConfiguration template) {
        NicConfiguration model = template != null ? template : new NicConfiguration();

        model.setName(entity.getName());
        model.setOnBoot(entity.getStartOnBoot());

        populateModelWithIpv4Details(entity, model);
        populateModelWithIpv6Details(entity, model);

        return model;
    }

    private static void populateModelWithIpv4Details(VmInitNetwork entity, NicConfiguration model) {
        if (entity.getBootProtocol() != null) {
            model.setBootProtocol(Ipv4BootProtocolMapper.map(entity.getBootProtocol()));
        }
        model.setIp(createIpModel(new VmInitNetworkIpv4InfoFetcher(entity)));
    }

    private static void populateModelWithIpv6Details(VmInitNetwork entity, NicConfiguration model) {
        if (entity.getIpv6BootProtocol() != null) {
            model.setIpv6BootProtocol(Ipv6BootProtocolMapper.map(entity.getIpv6BootProtocol()));
        }
        model.setIpv6(createIpModel(new VmInitNetworkIpv6InfoFetcher(entity)));
    }

    private static Ip createIpModel(VmInitNetworkIpInfoFetcher ipInfoFetcher) {
        Ip ip = new Ip();
        ip.setAddress(ipInfoFetcher.fetchIp());
        ip.setNetmask(ipInfoFetcher.fetchNetmask());
        ip.setGateway(ipInfoFetcher.fetchGateway());
        return ip;
    }

    @Mapping(from = Initialization.class, to = VmInit.class)
    public static VmInit map(Initialization model, VmInit template) {
        VmInit entity = template != null ? template : new VmInit();
        boolean someSubTagSet = false;

        if (model.isSetHostName()) {
            someSubTagSet = true;
            entity.setHostname(model.getHostName());
        }

        if (model.isSetDomain()) {
            someSubTagSet = true;
            entity.setDomain(model.getDomain());
        }

        if (model.isSetTimezone()) {
            someSubTagSet = true;
            entity.setTimeZone(model.getTimezone());
        }

        if (model.isSetAuthorizedSshKeys()) {
            someSubTagSet = true;
            entity.setAuthorizedKeys(model.getAuthorizedSshKeys());
        }

        if (model.isSetRegenerateSshKeys()) {
            someSubTagSet = true;
            entity.setRegenerateKeys(model.isRegenerateSshKeys());
        }

        if (model.isSetDnsServers()) {
            someSubTagSet = true;
            entity.setDnsServers(model.getDnsServers());
        }

        if (model.isSetDnsSearch()) {
            someSubTagSet = true;
            entity.setDnsSearch(model.getDnsSearch());
        }

        if (model.isSetWindowsLicenseKey()) {
            someSubTagSet = true;
            entity.setWinKey(model.getWindowsLicenseKey());
        }

        if (model.isSetRootPassword()) {
            someSubTagSet = true;
            entity.setRootPassword(model.getRootPassword());
        }

        if (model.isSetCustomScript()) {
            someSubTagSet = true;
            entity.setCustomScript(model.getCustomScript());
        }

        if (model.isSetNicConfigurations()) {
            someSubTagSet = true;
            List<VmInitNetwork> networks = new ArrayList<>();
            for (NicConfiguration nic : model.getNicConfigurations().getNicConfigurations()) {
                networks.add(map(nic, null));
            }
            entity.setNetworks(networks);
        }

        if (model.isSetInputLocale()) {
            someSubTagSet = true;
            entity.setInputLocale(model.getInputLocale());
        }

        if (model.isSetUiLanguage()) {
            someSubTagSet = true;
            entity.setUiLanguage(model.getUiLanguage());
        }

        if (model.isSetSystemLocale()) {
            someSubTagSet = true;
            entity.setSystemLocale(model.getSystemLocale());
        }

        if (model.isSetUserLocale()) {
            someSubTagSet = true;
            entity.setUserLocale(model.getUserLocale());
        }

        if (model.isSetUserName()) {
            someSubTagSet = true;
            entity.setUserName(model.getUserName());
        }

        if (model.isSetActiveDirectoryOu()) {
            someSubTagSet = true;
            entity.setActiveDirectoryOU(model.getActiveDirectoryOu());
        }

        if (model.isSetOrgName()) {
            someSubTagSet = true;
            entity.setOrgName(model.getOrgName());
        }

        if (model.isSetCloudInit()) {
            someSubTagSet = true;
            VmMapper.map(model.getCloudInit(), entity);
        }

        if (model.isSetCloudInitNetworkProtocol()) {
            someSubTagSet = true;
            entity.setCloudInitNetworkProtocol(map(model.getCloudInitNetworkProtocol()));
        }

        if (!someSubTagSet) {
            return null;
        }

        return entity;
    }

    @Mapping(from = org.ovirt.engine.api.model.CloudInitNetworkProtocol.class, to = CloudInitNetworkProtocol.class)
    public static CloudInitNetworkProtocol map(org.ovirt.engine.api.model.CloudInitNetworkProtocol protocol) {
        switch (protocol) {
        case ENI:
            return CloudInitNetworkProtocol.ENI;
        case OPENSTACK_METADATA:
            return CloudInitNetworkProtocol.OPENSTACK_METADATA;
        default:
            return null;
        }
    }

    @Mapping(from = CloudInitNetworkProtocol.class, to = org.ovirt.engine.api.model.CloudInitNetworkProtocol.class)
    public static org.ovirt.engine.api.model.CloudInitNetworkProtocol map(CloudInitNetworkProtocol protocol) {
        switch (protocol) {
        case ENI:
            return ENI;
        case OPENSTACK_METADATA:
            return OPENSTACK_METADATA;
        default:
            return null;
        }
    }

    @Mapping(from = VmInit.class, to = Initialization.class)
    public static Initialization map(VmInit entity, Initialization template) {
        Initialization model = template != null ? template :
            new Initialization();

        if (entity.getHostname() != null) {
            model.setHostName(entity.getHostname());
        }
        if (StringUtils.isNotBlank(entity.getDomain())) {
            model.setDomain(entity.getDomain());
        }
        if (entity.getTimeZone() != null) {
            model.setTimezone(entity.getTimeZone());
        }
        if (entity.getAuthorizedKeys() != null) {
            model.setAuthorizedSshKeys(entity.getAuthorizedKeys());
        }
        if (entity.getRegenerateKeys() != null) {
            model.setRegenerateSshKeys(entity.getRegenerateKeys());
        }
        if (entity.getDnsServers() != null) {
            model.setDnsServers(entity.getDnsServers());
        }
        if (entity.getDnsSearch() != null) {
            model.setDnsSearch(entity.getDnsSearch());
        }
        if (entity.getWinKey() != null) {
            model.setWindowsLicenseKey(entity.getWinKey());
        }
        if (entity.getRootPassword() != null || entity.isPasswordAlreadyStored()) {
            model.setRootPassword("******");
        }
        if (entity.getCustomScript() != null) {
            model.setCustomScript(entity.getCustomScript());
        }
        if (entity.getNetworks() != null) {
            model.setNicConfigurations(new NicConfigurations());
            for (VmInitNetwork network : entity.getNetworks()) {
                model.getNicConfigurations().getNicConfigurations().add(map(network, null));
            }
        }
        if (entity.getInputLocale() != null) {
            model.setInputLocale(entity.getInputLocale());
        }
        if (entity.getUiLanguage() != null) {
            model.setUiLanguage(entity.getUiLanguage());
        }
        if (entity.getSystemLocale() != null) {
            model.setSystemLocale(entity.getSystemLocale());
        }
        if (entity.getUserLocale() != null) {
            model.setUserLocale(entity.getUserLocale());
        }
        if (entity.getUserName() != null) {
            model.setUserName(entity.getUserName());
        }
        if (entity.getActiveDirectoryOU() != null) {
            model.setActiveDirectoryOu(entity.getActiveDirectoryOU());
        }
        if (entity.getOrgName() != null) {
            model.setOrgName(entity.getOrgName());
        }
        if (entity.getCloudInitNetworkProtocol() != null) {
            model.setCloudInitNetworkProtocol(map(entity.getCloudInitNetworkProtocol()));
        }
        return model;
    }
}
