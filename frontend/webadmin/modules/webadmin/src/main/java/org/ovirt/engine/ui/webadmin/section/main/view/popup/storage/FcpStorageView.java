package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.uicommonweb.models.storage.FcpStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class FcpStorageView extends AbstractStorageView<FcpStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<FcpStorageModel, FcpStorageView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, FcpStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @Inject
    public FcpStorageView() {
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
    public void edit(FcpStorageModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public FcpStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
    }

    @Override
    public void focus() {
    }
}
