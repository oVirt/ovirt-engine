package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.uicommonweb.models.storage.IscsiStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class IscsiStorageView extends AbstractStorageView<IscsiStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<IscsiStorageModel, IscsiStorageView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @Inject
    public IscsiStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
    }

    void localize(ApplicationConstants constants) {
    }

    @Override
    public void edit(IscsiStorageModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public IscsiStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
    }

    @Override
    public void focus() {
    }
}
