package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmsSelectionModel extends KeyModel {
    public VmsSelectionModel() {
        super(ConstantsManager.getInstance()
                .getConstants()
                .selectVm(), ConstantsManager.getInstance()
                .getConstants()
                .noAvailableVms());
    }

    Map<String, VM> allVmNamesMap;

    public void init(List<VM> vms, List<Guid> usedVms) {
        if (vms == null || usedVms == null) {
            return;
        }

        allVmNamesMap = new HashMap<>();
        Map<Guid, VM> vmsMap = new HashMap<>();
        for (VM vm : vms) {
            allVmNamesMap.put(vm.getName(), vm);
            vmsMap.put(vm.getId(), vm);
        }

        List<String> usedVmNames = new ArrayList<>();
        for (Guid guid : usedVms) {
            usedVmNames.add(vmsMap.get(guid).getName());
        }

        Collections.sort(usedVmNames);

        Set<String> usedVmNamesSorted = new LinkedHashSet<>(usedVmNames);

        init(allVmNamesMap.keySet(), usedVmNamesSorted);
    }

    @Override
    protected void initLineModel(KeyLineModel keyValueLineModel, String key) {
        // no implementation
    }

    public List<Guid> getSelectedVmIds() {
        List<Guid> list = new ArrayList<>();
        for (KeyLineModel keyModel : getItems()) {
            String selectedItem = keyModel.getKeys().getSelectedItem();
            if (isKeyValid(selectedItem)) {
                list.add(allVmNamesMap.get(selectedItem).getId());
            }
        }
        return list;
    }

}
