package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
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
            OnPropertyChanged(new PropertyChangedEventArgs("VM"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Disks"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Nics"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Apps"));
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
                            snapshot.getAppList().split(",") : new String[] {}));

                    snapshotModel.getIsPropertiesUpdated().setEntity(true);
                }
            }
        }), snapshot.getId());
    }

    public boolean Validate()
    {
        getDescription().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

        return getDescription().getIsValid();
    }
}
