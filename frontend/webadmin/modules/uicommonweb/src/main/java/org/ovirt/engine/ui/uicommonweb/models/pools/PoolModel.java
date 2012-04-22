package org.ovirt.engine.ui.uicommonweb.models.pools;

import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

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

    public PoolModel(IVmModelBehavior behavior)
    {
        super(behavior);
        java.util.ArrayList<EntityModel> poolTypeItems = new java.util.ArrayList<EntityModel>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().automaticTitle());
        tempVar.setEntity(VmPoolType.Automatic);
        EntityModel automaticOption = tempVar;
        poolTypeItems.add(automaticOption);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().manualTitle());
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

}
