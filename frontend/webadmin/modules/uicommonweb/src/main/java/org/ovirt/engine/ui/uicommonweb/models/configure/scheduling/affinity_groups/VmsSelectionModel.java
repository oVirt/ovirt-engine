package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.BaseKeyModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueLineModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class VmsSelectionModel extends BaseKeyModel {
    public VmsSelectionModel() {
        super(ConstantsManager.getInstance()
                .getConstants()
                .selectVm(), ConstantsManager.getInstance()
                .getConstants()
                .noAvailableVms());
    }

    Map<String, VM> allVmNameMap;

    public void init(List<VM> vms, List<Guid> usedVms) {
        if (vms == null || usedVms == null) {
            return;
        }
        allVmNameMap = new HashMap<>();
        Map<Guid, VM> vmmMap = new HashMap<>();
        for (VM vm : vms) {
            allVmNameMap.put(vm.getName(), vm);
            vmmMap.put(vm.getId(), vm);
        }

        Set<String> usedVmNames = new HashSet<>();
        for (Guid guid : usedVms) {
            usedVmNames.add(vmmMap.get(guid).getName());
        }

        init(allVmNameMap.keySet(), usedVmNames);
    }

    @Override
    public KeyValueLineModel createNewLineModel(String key) {
        KeyValueLineModel keyValueLineModel = super.createNewLineModel(key);
        keyValueLineModel.getValue().setIsAvailable(false);
        keyValueLineModel.getValues().setIsAvailable(false);
        return keyValueLineModel;
    }

    @Override
    protected void initLineModel(KeyValueLineModel keyValueLineModel, String key) {
        // no implementation
    }

    @Override
    protected void setValueByKey(KeyValueLineModel lineModel, String key) {
        // no implementation
    }

    public List<Guid> getSelectedVmIds() {
        List<Guid> list = new ArrayList<>();
        for (KeyValueLineModel keyModel : getItems()) {
            String selectedItem = keyModel.getKeys().getSelectedItem();
            if (isKeyValid(selectedItem)) {
                list.add(allVmNameMap.get(selectedItem).getId());
            }
        }
        return list;
    }

}
