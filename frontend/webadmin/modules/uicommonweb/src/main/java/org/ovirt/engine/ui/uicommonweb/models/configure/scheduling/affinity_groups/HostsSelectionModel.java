package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class HostsSelectionModel extends KeyModel {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private boolean initialized;

    public HostsSelectionModel() {
        super(constants.selectHost(), constants.noAvailableHosts());
    }

    final Map<String, VDS> allHostNamesMap = new HashMap<>();

    public void init(List<VDS> hosts, List<Guid> usedHosts) {
        if (hosts == null) {
            return;
        }

        // Create maps for identifying hosts by name or id
        Map<Guid, VDS> allHostIdsMap = new HashMap<>();
        populateHostMaps(hosts, allHostNamesMap, allHostIdsMap);

        Set<String> usedHostNames = getUsedHostNamesFromIds(usedHosts, allHostIdsMap);

        super.init(allHostNamesMap.keySet(), usedHostNames);

        setInitialized();
    }

    private void populateHostMaps(List<VDS> hosts, Map<String, VDS> allHostNamesMap, Map<Guid, VDS> allHostIdsMap) {
        hosts.forEach(host -> {
            allHostNamesMap.put(host.getName(), host);
            allHostIdsMap.put(host.getId(), host);
        });
    }

    private Set<String> getUsedHostNamesFromIds(List<Guid> usedHosts, Map<Guid, VDS> allHostIdsMap) {
        return usedHosts
                .stream()
                .map(guid -> allHostIdsMap.get(guid).getName())
                .sorted()
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    protected void initLineModel(KeyLineModel keyLineModel, String key) {
        // no implementation
    }

    public List<Guid> getSelectedHostIds() {
        List<Guid> list = new ArrayList<>();
        for (KeyLineModel keyModel : getItems()) {
            String selectedItem = keyModel.getKeys().getSelectedItem();
            if (isKeyValid(selectedItem)) {
                list.add(allHostNamesMap.get(selectedItem).getId());
            }
        }
        return list;
    }

    private void setInitialized() {
        initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
