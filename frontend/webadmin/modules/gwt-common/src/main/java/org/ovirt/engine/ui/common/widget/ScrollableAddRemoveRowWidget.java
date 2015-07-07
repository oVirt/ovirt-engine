package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class ScrollableAddRemoveRowWidget<M extends ListModel<T>, T, V extends Widget & HasValueChangeHandlers<T>> extends AddRemoveRowWidget<M, T, V> {

    @UiField
    public ScrollPanel scrollPanel;

    @Override
    protected void onLoad() {
        super.onLoad();
        scrollPanel.scrollToBottom();
    }

    protected void init(M model) {
        super.init(model);
        scrollPanel.scrollToBottom();
    }
}
