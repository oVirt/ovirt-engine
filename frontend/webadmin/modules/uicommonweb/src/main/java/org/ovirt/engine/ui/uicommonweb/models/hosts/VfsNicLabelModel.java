package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Collection;
import java.util.List;

public class VfsNicLabelModel extends NicLabelModel {

    public VfsNicLabelModel(List<String> originalLabels, Collection<String> suggestedLabels) {
        setOriginalLabels(originalLabels);
        setSuggestedLabels(suggestedLabels);
        initLabelModels();
    }
}
