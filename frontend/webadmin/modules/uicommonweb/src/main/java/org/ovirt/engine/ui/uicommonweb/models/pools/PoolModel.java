package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;

@SuppressWarnings("unused")
public class PoolModel extends VmModel
{

    private ListModel privatePoolType;

    public ListModel getPoolType()
    {
        return privatePoolType;
    }

    private void setPoolType(ListModel value)
    {
        privatePoolType = value;
    }

    private EntityModel privateNumOfDesktops;

    public EntityModel getNumOfDesktops()
    {
        return privateNumOfDesktops;
    }

    private void setNumOfDesktops(EntityModel value)
    {
        privateNumOfDesktops = value;
    }

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

    private int assignedVms;

    public int getAssignedVms()
    {
        return assignedVms;
    }

    public void setAssignedVms(int value)
    {
        if (assignedVms != value)
        {
            assignedVms = value;
            OnPropertyChanged(new PropertyChangedEventArgs("AssignedVms"));
        }
    }

    private boolean isPoolTabValid;

    public boolean getIsPoolTabValid()
    {
        return isPoolTabValid;
    }

    public void setIsPoolTabValid(boolean value)
    {
        if (isPoolTabValid != value)
        {
            isPoolTabValid = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsPoolTabValid"));
        }
    }

    private boolean isAddVMMode;

    public boolean getIsAddVMMode()
    {
        return isAddVMMode;
    }

    public void setIsAddVMMode(boolean value)
    {
        if (isAddVMMode != value)
        {
            isAddVMMode = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsAddVMMode"));
        }
    }

    public PoolModel()
    {
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

    @Override
    protected void FillTemplateList(Guid DataCenterId)
    {
        // var templates = DataProvider.GetTemplateListByDataCenter(DataCenterId).Where(a => (Guid)a.vmt_guid !=
        // Guid.Empty);
        java.util.ArrayList<VmTemplate> templates = new java.util.ArrayList<VmTemplate>();
        for (VmTemplate template : DataProvider.GetTemplateList(DataCenterId))
        {
            if (!template.getId().equals(NGuid.Empty))
            {
                templates.add(template);
            }
        }
        getTemplate().setItems(templates);
        // Template.Value = templates.FirstOrDefault();
        getTemplate().setSelectedItem(Linq.FirstOrDefault(templates));
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

    @Override
    public boolean Validate()
    {
        boolean baseValidation = super.Validate();

        setIsPoolTabValid(true);
        // Revalidate name field.
        // TODO: Make maximum characters value depend on number of desktops in pool.
        VmOsType os = (VmOsType) getOSType().getSelectedItem();

        int maxAlowedVms = DataProvider.GetMaxVmsInPool();
        int maxNumOfCharsVmSerialNumber = String.valueOf(maxAlowedVms).length() + 1;
        int maxAllowedChars_windows = WINDOWS_VM_NAME_MAX_LIMIT - maxNumOfCharsVmSerialNumber;
        int maxAllowedChars_nonWindows = NON_WINDOWS_VM_NAME_MAX_LIMIT - maxNumOfCharsVmSerialNumber;

        String nameExpr = StringFormat.format("^[0-9a-zA-Z-_]{1,%1$s}$", maxAllowedChars_windows);
        String nameMsg =
                StringFormat.format("Name must contain only alphanumeric characters. Maximum length: %1$s.",
                        maxAllowedChars_windows);

        if (!DataProvider.IsWindowsOsType(os))
        {
            nameExpr = StringFormat.format("^[-\\w]{1,%1$s}$", maxAllowedChars_nonWindows);
            nameMsg =
                    StringFormat.format("Name cannot contain special characters. Maximum length: %1$s.",
                            maxAllowedChars_nonWindows);
        }

        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(64);
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setExpression(nameExpr);
        tempVar2.setMessage(nameMsg);
        getName().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(255);
        getDescription().ValidateEntity(new IValidation[] { tempVar3 });

        LengthValidation tempVar4 = new LengthValidation();
        tempVar4.setMaxLength(4);
        IntegerValidation tempVar5 = new IntegerValidation();
        tempVar5.setMinimum(1);
        tempVar5.setMaximum(getIsNew() ? maxAlowedVms : maxAlowedVms - getAssignedVms());
        getNumOfDesktops().ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4, tempVar5 });

        setIsGeneralTabValid(getIsGeneralTabValid() && getName().getIsValid() && getNumOfDesktops().getIsValid());

        setIsPoolTabValid(true);

        return baseValidation && getName().getIsValid() && getNumOfDesktops().getIsValid();
    }
}
