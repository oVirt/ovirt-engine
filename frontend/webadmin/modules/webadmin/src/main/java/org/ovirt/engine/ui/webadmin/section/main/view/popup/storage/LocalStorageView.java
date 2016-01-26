package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.storage.LocalStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LocalStorageView extends AbstractStorageView<LocalStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<LocalStorageModel, LocalStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, LocalStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<LocalStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    WidgetStyle style;

    @UiField
    @WithElementId
    @Path(value = "path.entity")
    StringEntityModelTextBoxEditor localPathEditor;

    @UiField
    Label message;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public LocalStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    void addStyles() {
        localPathEditor.addContentWidgetContainerStyleName(style.localPathContentWidget());
    }

    void localize() {
        localPathEditor.setLabel(constants.storagePopupLocalPathLabel());
    }

    @Override
    public void edit(LocalStorageModel object) {
        driver.edit(object);
    }

    @Override
    public LocalStorageModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String localPathContentWidget();
    }

    @Override
    public void focus() {
        localPathEditor.setFocus(true);
    }

}
