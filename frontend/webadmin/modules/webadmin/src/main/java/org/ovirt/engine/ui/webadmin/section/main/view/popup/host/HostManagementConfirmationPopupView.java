package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementConfirmationPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;

public class HostManagementConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel> implements HostManagementConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfirmationModel, HostManagementConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostManagementConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostManagementConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    public HTML messageLabel;

    @UiField(provided = true)
    @Path(value = "latch.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor latch;

    @UiField
    @Ignore
    protected HTML noteLabel;

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationTemplates templates = AssetProvider.getTemplates();
    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostManagementConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
    }

    @Override
    public void edit(ConfirmationModel object) {
        driver.edit(object);
    }

    protected void localize() {
        latch.setLabel(constants.checkConnectivityManageConfirmPopup());
        messageLabel.setHTML(templates.blackRedBold(constants.youAreAboutManageConfirmPopup(), constants.thisMightCauseManageConfirmPopup()));
        noteLabel.setHTML(templates.middleBold(constants.itIsManageConfirmPopup(), constants.highlyRecommendedManageConfirmPopup(), constants.toProceeedWithConnectivityCheckManageConfirmPopup()));
    }

    @Override
    public ConfirmationModel flush() {
        return driver.flush();
    }

}
