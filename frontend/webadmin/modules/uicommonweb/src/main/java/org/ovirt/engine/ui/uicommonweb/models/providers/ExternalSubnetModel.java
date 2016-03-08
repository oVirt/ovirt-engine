package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.CidrValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.Ipv4AddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class ExternalSubnetModel extends Model {

    private EntityModel<String> name;
    private EntityModel<String> cidr;
    private ListModel<IpVersion> ipVersion;
    private EntityModel<String> gateway;
    private ListModel<EntityModel<String>> dnsServers;

    private ExternalSubnet subnet;
    private ProviderNetwork externalNetwork;

    public ExternalSubnetModel() {
        setName(new EntityModel<String>());
        setCidr(new EntityModel<String>());
        setIpVersion(new ListModel<IpVersion>());
        getIpVersion().setItems(Arrays.asList(IpVersion.values()));
        setGateway(new EntityModel<String>());
        setDnsServers(new ListModel<EntityModel<String>>());
    }

    public EntityModel<String> getName() {
        return name;
    }

    private void setName(EntityModel<String> name) {
        this.name = name;
    }

    public EntityModel<String> getCidr() {
        return cidr;
    }

    private void setCidr(EntityModel<String> cidr) {
        this.cidr = cidr;
    }

    public ListModel<IpVersion> getIpVersion() {
        return ipVersion;
    }

    private void setIpVersion(ListModel<IpVersion> ipVersion) {
        this.ipVersion = ipVersion;
    }

    public EntityModel<String> getGateway() {
        return gateway;
    }

    private void setGateway(EntityModel<String> gateway) {
        this.gateway = gateway;
    }

    public ListModel<EntityModel<String>> getDnsServers() {
        return dnsServers;
    }

    private void setDnsServers(ListModel<EntityModel<String>> dnsServers) {
        this.dnsServers = dnsServers;
    }

    public ExternalSubnet getSubnet() {
        return subnet;
    }

    public ProviderNetwork getExternalNetwork() {
        return externalNetwork;
    }

    public void setExternalNetwork(ProviderNetwork externalNetwork) {
        this.externalNetwork = externalNetwork;
    }

    public void toggleChangeability(boolean value) {
        getName().setIsChangeable(value);
        getCidr().setIsChangeable(value);
        getIpVersion().setIsChangeable(value);
        getGateway().setIsChangeable(value);
        getDnsServers().setIsChangeable(value);
    }

    public void flush() {
        subnet = new ExternalSubnet();
        subnet.setName(getName().getEntity());
        subnet.setExternalNetwork(getExternalNetwork());
        subnet.setCidr(getCidr().getEntity());
        subnet.setIpVersion(getIpVersion().getSelectedItem());
        subnet.setGateway(getGateway().getEntity());

        List<String> dnsServers = new ArrayList<>();
        for (EntityModel<String> dnsServer : getDnsServers().getItems()) {
            if (StringUtils.isNotEmpty(dnsServer.getEntity())) {
                dnsServers.add(dnsServer.getEntity());
            }
        }
        subnet.setDnsServers(dnsServers);
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        boolean ipv4 = getIpVersion().getSelectedItem() == IpVersion.IPV4;
        getCidr().validateEntity(new IValidation[] { ipv4
                ? new CidrValidation()
                : new NotEmptyValidation() });
        getIpVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getGateway().setIsValid(true);
        if (StringUtils.isNotEmpty(getGateway().getEntity()) && ipv4) {
            getGateway().validateEntity(new IValidation[] { new Ipv4AddressValidation() });
        }

        boolean dnsServersValid = true;
        for (EntityModel<String> dnsServer : getDnsServers().getItems()) {
            dnsServer.setIsValid(true);
            if (StringUtils.isNotEmpty(dnsServer.getEntity()) && ipv4) {
                dnsServer.validateEntity(new IValidation[] { new Ipv4AddressValidation() });
            }
            dnsServersValid &= dnsServer.getIsValid();
        }

        return getName().getIsValid()
                && getCidr().getIsValid()
                && getIpVersion().getIsValid()
                && getGateway().getIsValid()
                && dnsServersValid;
    }
}
