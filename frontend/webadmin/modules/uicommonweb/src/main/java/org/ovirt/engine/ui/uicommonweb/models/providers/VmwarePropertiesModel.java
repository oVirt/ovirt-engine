package org.ovirt.engine.ui.uicommonweb.models.providers;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmwareVmProviderProperties;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmsModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

public class VmwarePropertiesModel extends ProxyHostPropertiesModel {

    private EntityModel<String> vCenter = new EntityModel<>();
    private EntityModel<String> esx = new EntityModel<>();
    private EntityModel<String> vmwareDatacenter = new EntityModel<>();
    private EntityModel<String> vmwareCluster = new EntityModel<>();
    private EntityModel<Boolean> verifySSL = new EntityModel<>(true);
    private ListModel<VDS> proxyHost = new ListModel<>();

    public VmwarePropertiesModel() {
        getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                vCenter.setIsAvailable(getIsAvailable());
                esx.setIsAvailable(getIsAvailable());
                vmwareDatacenter.setIsAvailable(getIsAvailable());
                vmwareCluster.setIsAvailable(getIsAvailable());
                verifySSL.setIsAvailable(getIsAvailable());
                proxyHost.setIsAvailable(getIsAvailable());
            }
        });
    }

    public boolean validate() {
        if (!getIsAvailable()) {
            setIsValid(true);
        } else {
            getvCenter().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new LengthValidation(255),
                    new HostAddressValidation() });
            getEsx().validateEntity(new IValidation[]{
                    new NotEmptyValidation(),
                    new LengthValidation(255),
                    new HostAddressValidation()});
            vmwareDatacenter.validateEntity(new IValidation[]{
                    new NotEmptyValidation()});
            setIsValid(getvCenter().getIsValid()
                    && getEsx().getIsValid()
                    && getVmwareDatacenter().getIsValid());
        }

        return getIsValid();
    }

    public ListModel<VDS> getProxyHost() {
        return proxyHost;
    }

    public EntityModel<Boolean> getVerifySSL() {
        return verifySSL;
    }

    public EntityModel<String> getvCenter() {
        return vCenter;
    }

    public EntityModel<String> getEsx() {
        return esx;
    }

    public EntityModel<String> getVmwareDatacenter() {
        return vmwareDatacenter;
    }

    public EntityModel<String> getVmwareCluster() {
        return vmwareCluster;
    }

    public void setVmwareCluster(EntityModel<String> vmwareCluster) {
        this.vmwareCluster = vmwareCluster;
    }

    VmwareVmProviderProperties getVmwareVmProviderProperties(Guid dataCenterId) {
        return new VmwareVmProviderProperties(
                getvCenter().getEntity(),
                getEsx().getEntity(),
                ImportVmsModel.mergeDcAndCluster(getVmwareDatacenter().getEntity(), getVmwareCluster().getEntity(), false),
                getVerifySSL().getEntity(),
                dataCenterId,
                getProxyHost().getSelectedItem() != null ? getProxyHost().getSelectedItem().getId() : null);
    }

    public void init(Provider<VmwareVmProviderProperties> provider) {
        VmwareVmProviderProperties properties = provider.getAdditionalProperties();
        getvCenter().setEntity(properties.getvCenter());
        getEsx().setEntity(properties.getEsx());
        Pair<String, String> dcAndCluster = ImportVmsModel.splitToDcAndCluster(properties.getDataCenter());
        getVmwareDatacenter().setEntity(dcAndCluster.getFirst());
        getVmwareCluster().setEntity(dcAndCluster.getSecond());
        getVerifySSL().setEntity(properties.isVerifySSL());
        setLastProxyHostId(properties.getProxyHostId());
        setLastStoragePoolId(properties.getStoragePoolId());
    }

    public String getUrl() {
        return ImportVmsModel.getVmwareUrl(null, getvCenter().getEntity(),
                getVmwareDatacenter().getEntity(), getVmwareCluster().getEntity(),
                getEsx().getEntity(), getVerifySSL().getEntity());
    }
}
