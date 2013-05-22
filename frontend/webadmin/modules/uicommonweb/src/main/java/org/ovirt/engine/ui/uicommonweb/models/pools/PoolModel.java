package org.ovirt.engine.ui.uicommonweb.models.pools;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VmPoolType;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmModelBehaviorBase;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PoolModel extends UnitVmModel {

    public PoolModel(VmModelBehaviorBase behavior) {
        super(behavior);

        ArrayList<EntityModel> poolTypeItems = new ArrayList<EntityModel>();

        EntityModel automaticOption = new EntityModel();
        automaticOption.setTitle(ConstantsManager.getInstance().getConstants().automaticTitle());
        automaticOption.setEntity(VmPoolType.Automatic);
        poolTypeItems.add(automaticOption);

        EntityModel manualOption = new EntityModel();
        manualOption.setTitle(ConstantsManager.getInstance().getConstants().manualTitle());
        manualOption.setEntity(VmPoolType.Manual);
        poolTypeItems.add(manualOption);

        getPoolType().setItems(poolTypeItems);

        getAssignedVms().setIsAvailable(true);
        getNumOfDesktops().setIsAvailable(true);
        getPrestartedVms().setIsAvailable(true);
        getMaxAssignedVmsPerUser().setIsAvailable(true);

        setIsPoolTabValid(true);

        getPoolType().setSelectedItem(automaticOption);
        getOSType().setSelectedItem(OsRepository.DEFAULT_OS);
    }
}
