package org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.affinity_groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyModel;

public class EntitySelectionModel extends KeyModel {

    private boolean initialized;

    public EntitySelectionModel(String selectKey, String noKeys) {
        super(selectKey, noKeys);
    }

    private final Map<String, Guid> nameToId = new HashMap<>();

    public<T extends BusinessEntity<Guid> & Nameable> void init(List<T> entities, List<Guid> usedEntities) {
        if (entities == null) {
            return;
        }

        // Create maps for identifying entities by name or id
        Map<Guid, String> idToName = new HashMap<>();
        for (T entity : entities) {
            nameToId.put(entity.getName(), entity.getId());
            idToName.put(entity.getId(), entity.getName());
        }

        // These sets are sorted even if their elements are later added to a HashSet
        // in super.init(). Their order is preserved, which is probably a side
        // effect of the javascript implementation of HashSet.
        // The lines are displayed in the order they were added
        // to the set.
        Set<String> entityNames = nameToId.keySet().stream()
                .sorted(new LexoNumericComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<String> usedEntityNames = usedEntities.stream()
                .map(idToName::get)
                .sorted(new LexoNumericComparator())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        super.init(entityNames, usedEntityNames);

        setInitialized();
    }

    @Override
    protected void initLineModel(KeyLineModel lineModel, String key) {
        // no implementation
    }

    public List<Guid> getSelectedEntityIds() {
        List<Guid> list = new ArrayList<>();
        for (KeyLineModel keyModel : getItems()) {
            String selectedItem = keyModel.getKeys().getSelectedItem();
            if (isKeyValid(selectedItem)) {
                list.add(nameToId.get(selectedItem));
            }
        }
        return list;
    }

    private void setInitialized() {
        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
