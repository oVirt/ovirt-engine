package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class SnapshotModel extends EntityModel
{
    private VM vm;

    public VM getVm()
    {
        return vm;
    }

    public void setVm(VM value)
    {
        if (vm != value)
        {
            vm = value;
            OnPropertyChanged(new PropertyChangedEventArgs("VM")); //$NON-NLS-1$
        }
    }

    private ArrayList<DiskImage> disks;

    public ArrayList<DiskImage> getDisks()
    {
        return disks;
    }

    public void setDisks(ArrayList<DiskImage> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    private List<VmNetworkInterface> nics;

    public List<VmNetworkInterface> getNics()
    {
        return nics;
    }

    public void setNics(List<VmNetworkInterface> value)
    {
        if (nics != value)
        {
            nics = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Nics")); //$NON-NLS-1$
        }
    }

    private List<String> apps;

    public List<String> getApps()
    {
        return apps;
    }

    public void setApps(List<String> value)
    {
        if (apps != value)
        {
            apps = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Apps")); //$NON-NLS-1$
        }
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private EntityModel isPropertiesUpdated;

    public EntityModel getIsPropertiesUpdated()
    {
        return isPropertiesUpdated;
    }

    public void setIsPropertiesUpdated(EntityModel value)
    {
        isPropertiesUpdated = value;
    }

    public SnapshotModel()
    {
        setDescription(new EntityModel());
        setDisks(new ArrayList<DiskImage>());
        setNics(new ArrayList<VmNetworkInterface>());
        setApps(new ArrayList<String>());

        setIsPropertiesUpdated(new EntityModel());
        getIsPropertiesUpdated().setEntity(false);
    }

    public void UpdateVmConfiguration()
    {
        Snapshot snapshot = ((Snapshot) getEntity());

        AsyncDataProvider.GetVmConfigurationBySnapshot(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                SnapshotModel snapshotModel = (SnapshotModel) target;
                Snapshot snapshot = ((Snapshot) snapshotModel.getEntity());
                VM vm = (VM) returnValue;

                if (vm != null && snapshot != null) {
                    snapshotModel.setVm(vm);
                    snapshotModel.setDisks(vm.getDiskList());
                    snapshotModel.setNics(vm.getInterfaces());
                    snapshotModel.setApps(Arrays.asList(snapshot.getAppList() != null ?
                            snapshot.getAppList().split(",") : new String[] {})); //$NON-NLS-1$

                    snapshotModel.getIsPropertiesUpdated().setEntity(true);
                }
            }
        }), snapshot.getId());
    }

    public boolean Validate()
    {
        getDescription().ValidateEntity(new IValidation[] { new NotEmptyValidation(),
                new SpecialAsciiI18NOrNoneValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.GENERAL_MAX_SIZE) });

        return getDescription().getIsValid();
    }

    // send warning message, if a disk which doesn't allow snapshot is detected (e.g. LUN)
    void sendWarningForNonExportableDisks(ArrayList<Disk> disks) {
        // filter non-exportable disks
        final List<String> list = new ArrayList<String>();
        for (Disk disk : disks) {
            if (!disk.isAllowSnapshot()) {
                list.add(disk.getDiskAlias());
            }
        }

        if(!list.isEmpty()) {
            final String s = StringUtils.join(list, ", "); //$NON-NLS-1$
            // append warning message
            setMessage(ConstantsManager.getInstance().getMessages().disksWillNotBePartOfTheExportedVMSnapshot(s));
        }
    }

}
