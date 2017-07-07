package org.ovirt.engine.ui.common.widget;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

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

    private void addListeners(final ListModel<Label> labelList) {
        labelList.getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (labelList.getSelectedItems() == null) {
                    labelList.setSelectedItems(new ArrayList<Label>());
                }

                AffinityLabelSelectionWithListWidget.this.getListWidget().refreshItems();

                boolean labelsAvailable = !labelList.getItems().isEmpty();
                AffinityLabelSelectionWithListWidget.this.getAddSelectedItemButton().setEnabled(labelsAvailable);
                AffinityLabelSelectionWithListWidget.this.getSelectionWidget().getFilterListEditor().setEnabled(labelsAvailable);
            }
        });

        labelList.getSelectedItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                if (labelList.getSelectedItems() != null) {
                    AffinityLabelSelectionWithListWidget.this.getListWidget().refreshItems();
                }
            }
        });
    }
}
