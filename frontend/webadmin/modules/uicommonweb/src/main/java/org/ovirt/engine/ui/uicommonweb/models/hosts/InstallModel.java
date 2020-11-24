package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Arrays;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.core.common.businessentities.ReplaceHostConfiguration;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.RpmVersion;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
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

    private EntityModel<Boolean> rebootHostAfterInstall;

    public EntityModel<Boolean> getRebootHostAfterInstall() {
        return rebootHostAfterInstall;
    }

    private void setRebootHostAfterInstall(EntityModel<Boolean> value) {
        rebootHostAfterInstall = value;
    }

    private  EntityModel<Boolean> reconfigureGluster;

    private void setReconfigureGluster(EntityModel<Boolean> value) {
        reconfigureGluster = value;
    }

    public EntityModel<Boolean> getReconfigureGluster(){
        return reconfigureGluster;
    }

    private EntityModel<String> hostVersion;

    public EntityModel<String> getHostVersion() {
        return hostVersion;
    }

    public void setHostVersion(EntityModel<String> value) {
        hostVersion = value;
    }

    private  EntityModel<String> fqdnBox;

    public  EntityModel<String> getFqdnBox() {
        return fqdnBox;
    }

    private void setFqdnBox(EntityModel<String> value) {
        fqdnBox = value;
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

    private ListModel<ReplaceHostConfiguration.Action> replaceHostModel;

    public ListModel<ReplaceHostConfiguration.Action> getReplaceHostModel() {
        return replaceHostModel;
    }

    public void setReplaceHostModel(ListModel<ReplaceHostConfiguration.Action> replaceHostModel) {
        this.replaceHostModel = replaceHostModel;

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
        setRebootHostAfterInstall(new EntityModel<Boolean>());
        setReconfigureGluster(new EntityModel<Boolean>());
        getOverrideIpTables().setEntity(false);
        getActivateHostAfterInstall().setEntity(false);
        getRebootHostAfterInstall().setEntity(true);
        getReconfigureGluster().setEntity(true);
        setFqdnBox(new EntityModel<String>());
        getFqdnBox().setIsAvailable(true);
        getFqdnBox().setIsChangeable(true);
        setUserName(new EntityModel<String>());
        getUserName().setEntity(HostModel.RootUserName);
        // TODO: remove setIsChangeable when configured ssh username is enabled
        getUserName().setIsChangeable(false);
        setPublicKey(new EntityModel<String>());
        setValidationFailed(new EntityModel<Boolean>());
        fetchEngineSshPublicKey();
        setHostedEngineHostModel(new HostedEngineHostModel());
        setReplaceHostModel(new ListModel<ReplaceHostConfiguration.Action>());
        replaceHostModel.setItems(Arrays.asList(ReplaceHostConfiguration.Action.values()));
        getReplaceHostModel().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            updateVisibilities();
        });
    }

    public void updateVisibilities() {
        ReplaceHostConfiguration.Action replaceHostOption =
                getReplaceHostModel().getSelectedItem();
        getFqdnBox().setIsChangeable(replaceHostOption == ReplaceHostConfiguration.Action.DIFFERENTFQDN);
        if(replaceHostOption != ReplaceHostConfiguration.Action.DIFFERENTFQDN) {
            getFqdnBox().setEntity(null);
        }
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

        return getUserPassword().getIsValid()
                && getOVirtISO().getIsValid()
                && getHostedEngineHostModel().getIsValid()
                && getReplaceHostModel().getIsValid();
    }

    public void fetchEngineSshPublicKey() {
        AsyncDataProvider.getInstance().getEngineSshPublicKey(new AsyncQuery<>(pk -> {
            if (pk != null && pk.length() > 0) {
                getPublicKey().setEntity(pk);
            }
        }));
    }
}
