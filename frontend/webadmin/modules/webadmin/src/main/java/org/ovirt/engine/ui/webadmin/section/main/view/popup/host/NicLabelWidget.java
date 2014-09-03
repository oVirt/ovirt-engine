package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import java.util.Collection;

import org.ovirt.engine.ui.common.widget.ScrollableAddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.NicLabelModel;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class NicLabelWidget extends ScrollableAddRemoveRowWidget<NicLabelModel, ListModel<String>, NicLabelEditor> {

    public interface WidgetUiBinder extends UiBinder<Widget, NicLabelWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private Collection<String> suggestions;

    public NicLabelWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    protected NicLabelEditor createWidget(ListModel<String> value) {
        NicLabelEditor widget = new NicLabelEditor();
        widget.edit(value);
        return widget;
    }

    @Override
    protected ListModel<String> createGhostValue() {
        ListModel<String> value = new ListModel<String>();
        value.setItems(suggestions);
        value.setSelectedItem(""); //$NON-NLS-1$
        return value;
    }

    @Override
    protected boolean isGhost(ListModel<String> value) {
        String text = value.getSelectedItem();
        return text == null || text.isEmpty();
    }

    @Override
    public void edit(NicLabelModel model) {
        suggestions = model.getSuggestedLabels();
        super.edit(model);
    }

}
