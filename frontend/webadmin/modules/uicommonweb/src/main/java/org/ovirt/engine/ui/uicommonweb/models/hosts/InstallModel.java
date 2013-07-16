package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters.AuthenticationMethod;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

@SuppressWarnings("unused")
public class InstallModel extends Model {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private EntityModel privateUserPassword;

    public EntityModel getUserPassword() {
        return privateUserPassword;
    }

    private void setUserPassword(EntityModel value) {
        privateUserPassword = value;
    }

    private ListModel privateOVirtISO;

    public ListModel getOVirtISO() {
        return privateOVirtISO;
    }

    private void setOVirtISO(ListModel value) {
        privateOVirtISO = value;
    }

    private EntityModel privateOverrideIpTables;

    public EntityModel getOverrideIpTables() {
        return privateOverrideIpTables;
    }

    private void setOverrideIpTables(EntityModel value) {
        privateOverrideIpTables = value;
    }

    private EntityModel hostVersion;

    public EntityModel getHostVersion() {
        return hostVersion;
    }

    public void setHostVersion(EntityModel value) {
        hostVersion = value;
    }

    private EntityModel privateUserName;

    public EntityModel getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel value)
    {
        privateUserName = value;
    }

    private EntityModel privatePublicKey;

    public EntityModel getPublicKey()
    {
        return privatePublicKey;
    }

    private void setPublicKey(EntityModel value)
    {
        privatePublicKey = value;
    }

    private AuthenticationMethod hostAuthenticationMethod;

    public void setAuthenticationMethod(AuthenticationMethod value) {
        hostAuthenticationMethod = value;
    }

    public AuthenticationMethod getAuthenticationMethod() {
        return hostAuthenticationMethod;
    }

    public InstallModel() {
        setUserPassword(new EntityModel());
        setOVirtISO(new ListModel());
        setHostVersion(new EntityModel());

        setOverrideIpTables(new EntityModel());
        getOverrideIpTables().setEntity(false);
        setUserName(new EntityModel());
        getUserName().setEntity(constants.defaultUserName());
        // TODO: remove setIsChangable when configured ssh username is enabled
        getUserName().setIsChangable(false);
        setPublicKey(new EntityModel());
        getPublicKey().setEntity(constants.empty());
        fetchPublicKey();
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

        return getUserPassword().getIsValid() && getOVirtISO().getIsValid();
    }

    public void fetchPublicKey() {
        AsyncQuery aQuery = new AsyncQuery();
        aQuery.setModel(this);
        aQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                String pk = (String) result;
                if (pk != null && pk.length() > 0)
                {
                    getPublicKey().setEntity(result);
                }
            }
        };
        AsyncDataProvider.getHostPublicKey(aQuery);
    }
}
