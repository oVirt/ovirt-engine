package org.ovirt.engine.ui.uicommonweb.models.providers;

import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet;
import org.ovirt.engine.core.common.businessentities.network.ExternalSubnet.IpVersion;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.CidrValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class ExternalSubnetModel extends Model {

    private EntityModel<String> name;
    private EntityModel<String> cidr;
    private ListModel<IpVersion> ipVersion;
    private ExternalSubnet subnet;
    private ProviderNetwork externalNetwork;

    public ExternalSubnetModel() {
        setName(new EntityModel<String>());
        setCidr(new EntityModel<String>());
        setIpVersion(new ListModel<IpVersion>());
        getIpVersion().setItems(Arrays.asList(IpVersion.values()));
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
        getName().setIsChangable(value);
        getCidr().setIsChangable(value);
        getIpVersion().setIsChangable(value);
    }

    public void flush() {
        subnet = new ExternalSubnet();
        subnet.setName(getName().getEntity());
        subnet.setExternalNetwork(getExternalNetwork());
        subnet.setCidr(getCidr().getEntity());
        subnet.setIpVersion(getIpVersion().getSelectedItem());
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });
        getCidr().validateEntity(new IValidation[] { getIpVersion().getSelectedItem() == IpVersion.IPV4
                ? new CidrValidation()
                : new NotEmptyValidation() });
        getIpVersion().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getName().getIsValid() && getCidr().getIsValid() && getIpVersion().getIsValid();
    }
}
