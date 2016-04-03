package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PoolModel extends UnitVmModel {

    public PoolModel(VmModelBehaviorBase behavior) {
        super(behavior, null);

        ArrayList<EntityModel<VmPoolType>> poolTypeItems = new ArrayList<>();

        EntityModel automaticOption = new EntityModel();
        automaticOption.setTitle(ConstantsManager.getInstance().getConstants().automaticTitle());
        automaticOption.setEntity(VmPoolType.AUTOMATIC);
        poolTypeItems.add(automaticOption);

        EntityModel manualOption = new EntityModel();
        manualOption.setTitle(ConstantsManager.getInstance().getConstants().manualTitle());
        manualOption.setEntity(VmPoolType.MANUAL);
        poolTypeItems.add(manualOption);

        getPoolType().setItems(poolTypeItems);

        getPoolStateful().setIsAvailable(true);
        getAssignedVms().setIsAvailable(true);
        getNumOfDesktops().setIsAvailable(true);
        getPrestartedVms().setIsAvailable(true);
        getMaxAssignedVmsPerUser().setIsAvailable(true);

        setValidTab(TabName.POOL_TAB, true);

        getPoolType().setSelectedItem(automaticOption);
    }
}
