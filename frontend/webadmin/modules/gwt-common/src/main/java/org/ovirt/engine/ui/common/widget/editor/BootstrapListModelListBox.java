package org.ovirt.engine.ui.common.widget.editor;

import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.base.HasId;

import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.Widget;

/**
 * Bootstrap based ListModelListBox
 */
public class BootstrapListModelListBox<T> extends ListModelListBox<T> implements HasId {

    public BootstrapListModelListBox(Renderer<T> renderer) {
        super(renderer);
    }

    @Override
    protected ListBox getWidget() {
        return (ListBox) super.getWidget();
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(new ListBox());
    }

    @Override
    public void setId(String id) {
        getWidget().setId(id);
    }

    @Override
    public String getId() {
        return getWidget().getId();
    }
}
