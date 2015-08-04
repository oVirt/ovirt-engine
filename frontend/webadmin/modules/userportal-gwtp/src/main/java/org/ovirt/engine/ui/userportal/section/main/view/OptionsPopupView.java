package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.EditOptionsModel;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.OptionsPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class OptionsPopupView extends AbstractModelBoundPopupView<EditOptionsModel> implements OptionsPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<OptionsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends SimpleBeanEditorDriver<EditOptionsModel, OptionsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, OptionsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @UiField(provided = true)
    @Path(value = "enableConnectAutomatically.entity")
    @WithElementId("enableConnectAutomatically")
    EntityModelCheckBoxEditor connectAutomaticallyEditor;

    @UiField
    @Ignore
    public Label connectAutomaticallyMessage;

    @Inject
    public OptionsPopupView(EventBus eventBus) {
        super(eventBus);
        connectAutomaticallyEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        connectAutomaticallyEditor.setLabel(constants.connectAutomaticallyLabel());
        connectAutomaticallyMessage.setText(constants.connectAutomaticallyMessage());

        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(final EditOptionsModel model) {
        driver.edit(model);
    }

    @Override
    public EditOptionsModel flush() {
        return driver.flush();
    }
}
