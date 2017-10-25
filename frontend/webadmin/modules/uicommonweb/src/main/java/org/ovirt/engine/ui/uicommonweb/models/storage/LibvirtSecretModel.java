package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LibvirtSecretParameters;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.validation.GuidValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class LibvirtSecretModel extends EntityModel<LibvirtSecret> {

    private EntityModel<String> uuid;
    private EntityModel<String> value;
    private ListModel<LibvirtSecretUsageType> usageType;
    private EntityModel<String> description;
    private EntityModel<String> providerId;

    public LibvirtSecretModel() {
        setUuid(new EntityModel<>());
        setValue(new EntityModel<>());
        setUsageType(new ListModel<>());
        setDescription(new EntityModel<>());
        setProviderId(new EntityModel<>());

        ArrayList<LibvirtSecretUsageType> libvirtSecretUsageTypeList =
                AsyncDataProvider.getInstance().getLibvirtSecretUsageTypeList();
        getUsageType().setItems(libvirtSecretUsageTypeList, Linq.firstOrNull(libvirtSecretUsageTypeList));
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
    }

    private void onSave() {
        if (!validate()) {
            return;
        }

        ActionType actionType = isNew() ?
                ActionType.AddLibvirtSecret : ActionType.UpdateLibvirtSecret;
        flush();
        Frontend.getInstance().runAction(actionType, new LibvirtSecretParameters(getEntity()),
                result -> {
                    ActionReturnValue res = result.getReturnValue();
                    if (res.getSucceeded()) {
                        getCancelCommand().execute();
                    }
                }, this);
    }

    public boolean validate() {
        getUsageType().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getUuid().validateEntity(new IValidation[] { new NotEmptyValidation(), new GuidValidation() });

        ArrayList<IValidation> valueValidations = new ArrayList<>(
                Collections.singletonList(new RegexValidation(ValidationUtils.BASE_64_PATTERN,
                        ConstantsManager.getInstance().getConstants().secretValueMustBeInBase64())));
        if (isNew()) {
            valueValidations.add(new NotEmptyValidation());
            getValue().validateEntity(valueValidations.toArray(new IValidation[valueValidations.size()]));
        }

        return getUsageType().getIsValid() && getUuid().getIsValid() && getValue().getIsValid();
    }

    private void flush() {
        LibvirtSecret secret = isNew() ? new LibvirtSecret() : getEntity();
        secret.setUsageType(getUsageType().getSelectedItem());
        secret.setDescription(getDescription().getEntity());
        secret.setProviderId(Guid.createGuidFromString(getProviderId().getEntity()));
        secret.setId(Guid.createGuidFromString(uuid.getEntity()));
        if (StringHelper.isNotNullOrEmpty(getValue().getEntity())) {
            secret.setValue(getValue().getEntity());
        }
        setEntity(secret);
    }

    public void edit(LibvirtSecret secret) {
        setEntity(secret);
        getUsageType().setSelectedItem(secret.getUsageType());
        getUuid().setEntity(secret.getId().toString());
        getDescription().setEntity(secret.getDescription());
    }

    private boolean isNew() {
        return getEntity() == null;
    }

    public EntityModel<String> getUuid() {
        return uuid;
    }

    public void setUuid(EntityModel<String> uuid) {
        this.uuid = uuid;
    }

    public EntityModel<String> getValue() {
        return value;
    }

    public void setValue(EntityModel<String> value) {
        this.value = value;
    }

    public ListModel<LibvirtSecretUsageType> getUsageType() {
        return usageType;
    }

    public void setUsageType(ListModel<LibvirtSecretUsageType> usageType) {
        this.usageType = usageType;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public EntityModel<String> getProviderId() {
        return providerId;
    }

    public void setProviderId(EntityModel<String> providerId) {
        this.providerId = providerId;
    }
}
