package org.ovirt.engine.ui.common.widget;

import java.util.ArrayList;

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

    public void init(ListModel<Label> labelList) {
        getListWidget().init(labelList);
        addListeners(labelList);
    }

    private void addListeners(ListModel<Label> labelList) {
        labelList.getItemsChangedEvent().addListener((ev, sender, args) -> {
            if (labelList.getSelectedItems() == null) {
                labelList.setSelectedItems(new ArrayList<>());
            }

            getListWidget().refreshItems();

            boolean labelsAvailable = !labelList.getItems().isEmpty();
            getAddSelectedItemButton().setEnabled(labelsAvailable);
            getSelectionWidget().getFilterListEditor().setEnabled(labelsAvailable);
        });

        labelList.getSelectedItemsChangedEvent().addListener((ev, sender, args) -> {
            if (labelList.getSelectedItems() != null) {
                getListWidget().refreshItems();
            }
        });
    }
}
