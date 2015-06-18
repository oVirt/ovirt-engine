package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.providers.HostNetworkProviderModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class InstallModel extends Model {

    private EntityModel<String> privateUserPassword;
    private VDS vds;

    public EntityModel<String> getUserPassword() {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel<String> value) {
        privateUserPassword = value;
    }

    private ListModel<RpmVersion> privateOVirtISO;

    public ListModel<RpmVersion> getOVirtISO() {
        return privateOVirtISO;
    }

    private void setOVirtISO(ListModel<RpmVersion> value) {
        privateOVirtISO = value;
    }

    private EntityModel<Boolean> privateOverrideIpTables;

    public EntityModel<Boolean> getOverrideIpTables() {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel<Boolean> value) {
        privateOverrideIpTables = value;
    }

    private EntityModel<Boolean> activateHostAfterInstall;

    public EntityModel<Boolean> getActivateHostAfterInstall() {
        return activateHostAfterInstall;
    }

    private void setActivateHostAfterInstall(EntityModel<Boolean> value) {
        activateHostAfterInstall = value;
    }

    private EntityModel<String> hostVersion;

    public EntityModel<String> getHostVersion() {
        return hostVersion;
    }

    public void setHostVersion(EntityModel<String> value) {
        hostVersion = value;
    }

    private EntityModel<String> privateUserName;

    public EntityModel<String> getUserName() {
        return privateUserName;
    }

    private void setUserName(EntityModel<String> value) {
        privateUserName = value;
    }

    private EntityModel<String> privatePublicKey;

    public EntityModel<String> getPublicKey() {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel<String> value) {
        privatePublicKey = value;
    }

    private AuthenticationMethod hostAuthenticationMethod;

    public void setAuthenticationMethod(AuthenticationMethod value) {
        hostAuthenticationMethod = value;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return hostAuthenticationMethod;
    }

    private HostNetworkProviderModel networkProviderModel;

    public HostNetworkProviderModel getNetworkProviderModel() {
        return networkProviderModel;
    }

    private void setNetworkProviderModel(HostNetworkProviderModel value) {
        networkProviderModel = value;
    }

    public ListModel<?> getNetworkProviders() {
        return getNetworkProviderModel().getNetworkProviders();
    }

    public EntityModel<?> getInterfaceMappings() {
        return getNetworkProviderModel().getInterfaceMappings();
    }

    public void setVds(VDS value) {
        vds = value;
    }

    public VDS getVds() {
        return vds;
    }

    private EntityModel<Boolean> validationFailed;

    public EntityModel<Boolean> getValidationFailed() {
        return validationFailed;
    }

    public void setValidationFailed(EntityModel<Boolean> value) {
        validationFailed = value;
        onPropertyChanged(new PropertyChangedEventArgs("ValidationFailed")); //$NON-NLS-1$
    }

    private HostedEngineHostModel hostedEngineHostModel;

    public HostedEngineHostModel getHostedEngineHostModel() {
        return hostedEngineHostModel;
    }

    public void setHostedEngineHostModel(HostedEngineHostModel hostedEngineHostModel) {
        this.hostedEngineHostModel = hostedEngineHostModel;
    }

    public InstallModel() {
        setUserPassword(new EntityModel<String>());
        setOVirtISO(new ListModel<RpmVersion>());
        setHostVersion(new EntityModel<String>());

        setOverrideIpTables(new EntityModel<Boolean>());
        setActivateHostAfterInstall(new EntityModel<Boolean>());
        getOverrideIpTables().setEntity(false);
        getActivateHostAfterInstall().setEntity(false);
        setUserName(new EntityModel<String>());
        getUserName().setEntity(HostModel.RootUserName);
        // TODO: remove setIsChangeable when configured ssh username is enabled
        getUserName().setIsChangeable(false);
        setPublicKey(new EntityModel<String>());
        getPublicKey().setEntity(""); //$NON-NLS-1$
        setValidationFailed(new EntityModel<Boolean>());
        fetchEngineSshPublicKey();
        setNetworkProviderModel(new HostNetworkProviderModel());
        setHostedEngineHostModel(new HostedEngineHostModel());
    }

    public boolean validate(boolean isOVirt) {
        getOVirtISO().setIsValid(true);
        getUserPassword().setIsValid(true);

        if (isOVirt) {
            getOVirtISO().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        } else {
            if (getAuthenticationMethod() == AuthenticationMethod.Password) {
                getUserPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
            }
        }

        getNetworkProviderModel().validate();

        return getUserPassword().getIsValid()
                && getOVirtISO().getIsValid()
                && getNetworkProviderModel().getIsValid()
                && getHostedEngineHostModel().getIsValid();
    }

    public void fetchEngineSshPublicKey() {
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result) {
                String pk = (String) result;
                if (pk != null && pk.length() > 0) {
                    getPublicKey().setEntity(pk);
                }
            }
        };
        AsyncDataProvider.getInstance().getEngineSshPublicKey(aQuery);
    }
}
