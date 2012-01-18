package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

@SuppressWarnings("unused")
public class PoolModel extends UnitVmModel
{
    @Override
    public boolean getCanDefineVM()
    {
        return getIsNew() || (Integer) getNumOfDesktops().getEntity() == 0;
    }

    @Override
    public boolean getIsNew()
    {
        return super.getIsNew();
    }

    @Override
    public void setIsNew(boolean value)
    {
        setIsAddVMMode(value);
        super.setIsNew(value);
    }

    // public int OriginalNumberOfDesktops { get; set; }

    public PoolModel(IVmModelBehavior behavior)
    {
        super(behavior);
        java.util.ArrayList<EntityModel> poolTypeItems = new java.util.ArrayList<EntityModel>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle("Automatic");
        tempVar.setEntity(VmPoolType.Automatic);
        EntityModel automaticOption = tempVar;
        poolTypeItems.add(automaticOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle("Manual");
        tempVar2.setEntity(VmPoolType.Manual);
        poolTypeItems.add(tempVar2);

        setPoolType(new ListModel());
        getPoolType().setItems(poolTypeItems);

        EntityModel tempVar3 = new EntityModel();
        tempVar3.setEntity(1);
        setNumOfDesktops(tempVar3);

        setIsPoolTabValid(true);

        getPoolType().setSelectedItem(automaticOption);
        getOSType().setSelectedItem(VmOsType.Unassigned);
    }

    // private void DataCenterChanged()
    // {
    // Clusters = DataProvider.GetClusterList(DataCenter.id);
    // if (CanDefineVM)
    // {
    // Cluster = Clusters.FirstOrDefault();
    // }

    // StorageDomains = DataProvider.GetStorageDomainList(DataCenter.id);

    // Templates = DataProvider.GetTemplateListByDataCenter(DataCenter.id)
    // .Where(a => a.vmt_guid != Guid.Empty);
    // if (CanDefineVM)
    // {
    // Template = Templates.FirstOrDefault();
    // }
    // }

    // public void TemplateChanged()
    // {
    // if (Template == null && !CanDefineVM)
    // {
    // return;
    // }

    // OSType = Template.os;
    // NumOfMonitors = Template.num_of_monitors;
    // Domain = Template.domain;
    // MemSize = Template.mem_size_mb;
    // UsbPolicy = Template.usb_policy;
    // IsAutoSuspend = Template.is_auto_suspend;
    // if (TimeZones != null)
    // {
    // TimeZone = TimeZones.FirstOrDefault(a => a.getKey() == (String.IsNullOrEmpty(Template.time_zone)
    // ? DataProvider.GetDefaultTimeZone()
    // : Template.time_zone)
    // );
    // }
    // var storageDomains = DataProvider.GetStorageDomainListByTemplate(Template.vmt_guid);
    // StorageDomains = storageDomains;
    // StorageDomain = storageDomains.FirstOrDefault();
    // }

    // private void OsTypeChanged()
    // {
    // HasDomain = IsWindowsOsType(OSType);
    // HasTimeZone = IsWindowsOsType(OSType);
    // }

}
