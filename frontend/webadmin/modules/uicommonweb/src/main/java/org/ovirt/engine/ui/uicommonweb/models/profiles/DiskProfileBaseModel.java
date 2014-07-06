package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.DiskProfileParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.queries.QosQueryParameterBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public abstract class DiskProfileBaseModel extends Model {
    private final static StorageQos EMPTY_QOS;

    static {
        EMPTY_QOS = new StorageQos();
        EMPTY_QOS.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
        EMPTY_QOS.setId(Guid.Empty);
    }

    private EntityModel<String> name;
    private EntityModel<String> description;
    private final EntityModel sourceModel;
    private ListModel<StorageDomain> storageDomains;
    private ListModel<StorageQos> qos;
    private DiskProfile diskProfile;
    private final Guid defaultQosId;
    private final VdcActionType vdcActionType;

    public EntityModel<String> getName()
    {
        return name;
    }

    private void setName(EntityModel<String> value)
    {
        name = value;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel<StorageDomain> getStorageDomains() {
        return storageDomains;
    }

    public void setStorageDomains(ListModel<StorageDomain> storageDomains) {
        this.storageDomains = storageDomains;
    }

    public void setDiskProfile(DiskProfile diskProfile) {
        this.diskProfile = diskProfile;
    }

    public DiskProfile getDiskProfile() {
        return diskProfile;
    }

    public ListModel<StorageQos> getQos() {
        return qos;
    }

    public void setQos(ListModel<StorageQos> qos) {
        this.qos = qos;
    }

    public DiskProfileBaseModel(EntityModel sourceModel,
            Guid dcId,
            Guid defaultQosId,
            VdcActionType vdcActionType) {
        this.sourceModel = sourceModel;
        this.defaultQosId = defaultQosId;
        this.vdcActionType = vdcActionType;

        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setStorageDomains(new ListModel<StorageDomain>());
        setQos(new ListModel<StorageQos>());

        initStorageQosList(dcId);
        initCommands();
    }

    protected void initCommands() {
        UICommand okCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    private void onSave() {
        if (getProgress() != null) {
            return;
        }

        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        startProgress(null);

        Frontend.getInstance().runAction(vdcActionType,
                new DiskProfileParameters(diskProfile, diskProfile.getId()),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        stopProgress();

                        if (returnValue != null && returnValue.getSucceeded()) {
                            cancel();
                        }
                    }
                },
                this);
    }

    public void flush() {
        if (diskProfile == null) {
            diskProfile = new DiskProfile();
        }
        diskProfile.setName(getName().getEntity());
        diskProfile.setDescription(getDescription().getEntity());
        StorageDomain storageDomain = getStorageDomains().getSelectedItem();
        diskProfile.setStorageDomainId(storageDomain != null ? storageDomain.getId() : null);
        StorageQos storageQos = getQos().getSelectedItem();
        diskProfile.setQosId(storageQos != null
                && storageQos.getId() != null
                && !storageQos.getId().equals(Guid.Empty)
                ? storageQos.getId() : null);
    }

    private void cancel() {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        else if ("Cancel".equals(command.getName())) {//$NON-NLS-1$
            cancel();
        }
    }

    public void initStorageQosList(Guid dataCenterId) {
        if (dataCenterId == null) {
            return;
        }

        Frontend.getInstance().runQuery(VdcQueryType.GetAllQosByStoragePoolIdAndType,
                new QosQueryParameterBase(dataCenterId, QosType.STORAGE),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        DiskProfileBaseModel.this.initQosList(returnValue == null ? new ArrayList<StorageQos>()
                                : (List<StorageQos>) ((VdcQueryReturnValue) returnValue).getReturnValue());
                    }

                }));
    }

    private void initQosList(List<StorageQos> qosList) {
        qosList.add(0, EMPTY_QOS);
        getQos().setItems(qosList);
        if (defaultQosId != null) {
            for (StorageQos storageQos : qosList) {
                if (defaultQosId.equals(storageQos.getId())) {
                    getQos().setSelectedItem(storageQos);
                    break;
                }
            }
        }
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new SpecialAsciiI18NOrNoneValidation() });

        return getName().getIsValid();
    }

}
