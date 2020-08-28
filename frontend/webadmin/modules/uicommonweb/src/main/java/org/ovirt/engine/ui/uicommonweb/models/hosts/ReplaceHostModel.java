package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ReplaceHostConfiguration;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;



public class ReplaceHostModel extends ListModel<ReplaceHostConfiguration.Action> {

    public ReplaceHostModel() {
        setItems(Arrays.asList(ReplaceHostConfiguration.Action.values()));
    }

    public void removeActionFromList(ReplaceHostConfiguration.Action action) {
        List<ReplaceHostConfiguration.Action> actions = new ArrayList<>();

        for (ReplaceHostConfiguration.Action deployAction: ReplaceHostConfiguration.Action.values()) {
            if (!deployAction.equals(action)) {
                actions.add(deployAction);
            }
        }

        setItems(actions);
    }
}
