package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.HostRestartConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostRestartConfirmationModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostRestartConfirmationPopupView extends AbstractModelBoundPopupView<HostRestartConfirmationModel> implements HostRestartConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<HostRestartConfirmationModel, HostRestartConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostRestartConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostRestartConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);

    }
    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    public AlertWithIcon messagePanel;

    @UiField
    protected FlowPanel itemPanel;

    @UiField(provided = true)
    @Path(value = "forceToMaintenance.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor forceToMaintenance;

    @Inject
    public HostRestartConfirmationPopupView(EventBus eventBus) {
        super(eventBus);

        forceToMaintenance = new EntityModelCheckBoxEditor(Align.RIGHT);
        forceToMaintenance.setLabel(constants.forceToMaintenance());
        forceToMaintenance.getContentWidgetContainer().getElement().getStyle().setWidth(90, Unit.PCT);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }
    @Override
    public void setItems(Iterable<?> items) {
        if (items != null) {
            for (Object item : items) {
                itemPanel.add(new Label("- " + item)); //$NON-NLS-1$
            }
        } else {
            itemPanel.clear();
        }
    }

    @Override
    public void setMessage(String message) {
        String escapedMessage = SafeHtmlUtils.htmlEscape(message != null ? message : ""); //$NON-NLS-1$
        escapedMessage = escapedMessage.replace("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        messagePanel.setHtmlText(SafeHtmlUtils.fromTrustedString(escapedMessage));
    }

    @Override
    public void edit(final HostRestartConfirmationModel object) {
        driver.edit(object);
        forceToMaintenance.asCheckBox().setValue(object.getForceToMaintenance().getEntity());
    }

    @Override
    public HostRestartConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
