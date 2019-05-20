package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.businessentities.Label;

public class AffinityLabelSelectionWithListWidget extends AbstractItemSelectionWithListWidget<Label> {

    @Override
    public AbstractItemSelectionWidget<Label> initItemSelectionWidget() {
        return new AffinityLabelSelectionWidget();
    }

    @Override
    public AbstractItemListWidget<Label> initItemListWidget() {
        return new AffinityLabelListWidget();
    }
}
