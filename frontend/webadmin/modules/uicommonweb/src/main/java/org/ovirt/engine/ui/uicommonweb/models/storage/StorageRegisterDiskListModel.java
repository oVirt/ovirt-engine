package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.RegisterCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

public class StorageRegisterDiskListModel extends SearchableListModel<StorageDomain, Disk> {
    private UICommand registerCommand;

    public UICommand getRegisterCommand() {
        return registerCommand;
    }

    private void setRegisterCommand(UICommand value) {
        registerCommand = value;
    }

    public StorageRegisterDiskListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().registerDisksTitle());
        setRegisterCommand(new UICommand("Register", this)); //$NON-NLS-1$
        setIsTimerDisabled(true);
        updateActionAvailability();
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        getSearchCommand().execute();
    }

    public void setEntity(StorageDomain value) {
        super.setEntity(value);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() == null) {
            setItems(null);
            return;
        }
        super.search();
    }

    public void cancel() {
        setWindow(null);
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }
        IdQueryParameters parameters = new IdQueryParameters(getEntity().getId());
        parameters.setRefresh(getIsQueryFirstTime());
        Frontend.getInstance().runQuery(VdcQueryType.GetUnregisteredCinderDisksByStorageDomainId, parameters,
                new AsyncQuery(this, new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object ReturnValue) {
                        List<Disk> newItems = ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                        Collections.sort(newItems, new DiskByDiskAliasComparator());
                        setItems(newItems);
                    }
                }));
    }

    private void updateActionAvailability() {
        ArrayList<CinderDisk> disks = getSelectedItems() != null ?
                Linq.<CinderDisk> cast(getSelectedItems()) : new ArrayList<CinderDisk>();

        getRegisterCommand().setIsExecutionAllowed(disks.size() > 0);
    }

    private void register() {
        if (getWindow() != null) {
            return;
        }

        ArrayList<VdcActionParametersBase> parametersList = new ArrayList<>();
        for (Object item : getSelectedItems()) {
            CinderDisk disk = (CinderDisk) item;
            RegisterCinderDiskParameters parameters = new RegisterCinderDiskParameters(disk, getEntity().getId());
            parametersList.add(parameters);
        }

        Frontend.getInstance().runMultipleAction(VdcActionType.RegisterCinderDisk, parametersList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void executed(FrontendMultipleActionAsyncResult result) {
                    }
                },
                this);
    }

    private void onRegister() {
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getRegisterCommand()) {
            register();
        }
        else if ("OnRegister".equals(command.getName())) { //$NON-NLS-1$
            onRegister();
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    @Override
    protected String getListName() {
        return "StorageRegisterDiskListModel"; //$NON-NLS-1$
    }
}
