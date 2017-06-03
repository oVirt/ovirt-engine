package org.ovirt.engine.ui.common.widget;

import java.util.Collections;
import java.util.List;

import org.gwtbootstrap3.client.ui.html.Span;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public class AffinityLabelListWidget extends AbstractItemListWidget<ListModel<Label>, Label> {

    public AffinityLabelListWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    @Override
    public void init(ListModel<Label> labelModel) {
        super.init(labelModel);
        itemListLabel.setText(constants.selectedAffinityLabels());
    }

    @Override
    protected void createItems() {
        List<Label> selectedLabels = getModel().getSelectedItems();
        boolean noLabelsSelected = selectedLabels == null || selectedLabels.isEmpty();

        if (noLabelsSelected) {
            addNoLabelsMessage();
            return;
        }

        itemList.clear();
        Collections.sort(selectedLabels, new NameableComparator());

        selectedLabels.forEach(label -> {
            ItemListItem labelListItem = new ItemListItem();
            labelListItem.init(label.getName());
            labelListItem.getDeactivationAnchor().addClickHandler(event -> {
                getModel().getSelectedItems().remove(label);
                refreshItems();
            });
            itemList.add(labelListItem);
        });

        itemListPanel.add(itemList);
    }

    private void addNoLabelsMessage() {
        Span span = new Span();
        span.setText(constants.noAffinityLabelsSelected());
        itemListPanel.add(span);
    }
}
