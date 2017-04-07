package org.ovirt.engine.ui.common.widget.form.key_value;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyLineModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class KeyLineWidget extends BaseKeyLineWidget<KeyLineModel> {

    interface WidgetUiBinder extends UiBinder<Widget, KeyLineWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Driver extends UiCommonEditorDriver<KeyLineModel, KeyLineWidget> {
    }

    protected final Driver driver = GWT.create(Driver.class);

    public KeyLineWidget() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        init();
    }

    @Override
    protected void init() {
        driver.initialize(this);
        hideLabels();
    }

    @Override
    public void edit(final KeyLineModel object) {
        super.doEdit(object);
        driver.edit(object);
    }

    @Override
    public KeyLineModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
