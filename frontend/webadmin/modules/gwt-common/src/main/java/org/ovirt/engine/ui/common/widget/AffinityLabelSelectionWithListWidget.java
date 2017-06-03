package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

public class AffinityLabelSelectionWithListWidget extends AbstractItemSelectionWithListWidget<Label> {

    @Override
    public AbstractItemSelectionWidget<Label> initItemSelectionWidget() {
        return new AffinityLabelSelectionWidget();
    }

    @Override
    public AbstractItemListWidget<ListModel<Label>, Label> initItemListWidget() {
        return new AffinityLabelListWidget();
    }
}
