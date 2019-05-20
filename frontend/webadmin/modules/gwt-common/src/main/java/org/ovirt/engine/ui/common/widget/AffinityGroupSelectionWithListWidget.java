package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;

public class AffinityGroupSelectionWithListWidget extends AbstractItemSelectionWithListWidget<AffinityGroup>{

    @Override
    public AbstractItemSelectionWidget<AffinityGroup> initItemSelectionWidget() {
        return new AffinityGroupSelectionWidget();
    }

    @Override
    public AbstractItemListWidget<AffinityGroup> initItemListWidget() {
        return new AffinityGroupListWidget();
    }
}
