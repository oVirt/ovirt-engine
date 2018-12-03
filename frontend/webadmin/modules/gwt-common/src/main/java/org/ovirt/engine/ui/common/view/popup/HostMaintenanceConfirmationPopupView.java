package org.ovirt.engine.ui.common.view.popup;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.HostMaintenanceConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.AlertWithIcon;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HostMaintenanceConfirmationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class HostMaintenanceConfirmationPopupView extends AbstractModelBoundPopupView<HostMaintenanceConfirmationModel> implements HostMaintenanceConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<HostMaintenanceConfirmationModel, HostMaintenanceConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostMaintenanceConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<HostMaintenanceConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final UIConstants uiConstants = ConstantsManager.getInstance().getConstants();

    @UiField
    @Ignore
    public HTML messageHTML;

    @UiField
    protected FlowPanel itemPanel;

    @UiField(provided = true)
    @Path(value = "latch.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor latch;

    @UiField(provided = true)
    @Path(value = "force.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor force;

    @UiField
    @Ignore
    protected HTML noteHTML;

    @UiField
    @Path(value = "reason.entity")
    @WithElementId
    StringEntityModelTextBoxEditor reasonEditor;

    @UiField(provided = true)
    @Path(value = "stopGlusterServices.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor stopGlusterServices;

    @UiField
    FlowPanel pinnedVMsInfoPanel;

    @UiField
    protected AlertWithIcon pinnedVMsInfoMessage;

    @Inject
    public HostMaintenanceConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        latch.setLabel(constants.approveOperation());
        force = new EntityModelCheckBoxEditor(Align.RIGHT);
        force.setLabel(constants.forceRemove());
        force.getContentWidgetContainer().getElement().getStyle().setWidth(90, Unit.PCT);
        stopGlusterServices = new EntityModelCheckBoxEditor(Align.RIGHT);
        stopGlusterServices.getContentWidgetContainer().getElement().getStyle().setWidth(90, Unit.PCT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
        pinnedVMsInfoPanel.setVisible(false);
        pinnedVMsInfoMessage.setAlertType(AlertType.INFO);
    }

    @Override
    public void setItems(Iterable<?> items) {
        if (items != null) {
            addItems(items);
        } else {
            itemPanel.clear();
        }
    }

    void setNote(String note) {
        noteHTML.setHTML(SafeHtmlUtils.fromString(note != null ? note : "").asString().replace("\n", "<br>")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    protected void addItems(Iterable<?> items) {
        for (Object item : items) {
            addItemText(item);
        }
    }

    protected void addItemText(Object item) {
        addItemLabel(getItemTextFormatted(String.valueOf(item)));
    }

    protected void addItemLabel(String text) {
        itemPanel.add(new Label(text));
    }

    protected void addItemLabel(SafeHtml html) {
        itemPanel.add(new HTML(html));
    }

    protected String getItemTextFormatted(String itemText) {
        return "- " + itemText; //$NON-NLS-1$
    }

    @Override
    public void edit(HostMaintenanceConfirmationModel object) {
        driver.edit(object);

        // Bind "Latch.IsAvailable"
        object.getLatch().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                EntityModel entity = (EntityModel) sender;
                if (entity.getIsAvailable()) {
                    latch.setVisible(true);
                }
            }
        });

        if (object.getForceLabel() != null) {
            force.setLabel(object.getForceLabel());
        }

        force.asCheckBox().setValue(object.getForce().getEntity());
        // Bind "Force.Label"
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("ForceLabel".equals(args.propertyName)) { //$NON-NLS-1$
                ConfirmationModel entity = (ConfirmationModel) sender;
                force.setLabel(entity.getForceLabel());
            }
        });

        setNote(object.getNote());
        // Bind "Note"
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("Note".equals(args.propertyName)) { //$NON-NLS-1$
                ConfirmationModel entity = (ConfirmationModel) sender;
                setNote(entity.getNote());
            }
        });


        // Bind "pinnedVMsInfoPanelVisible"
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("pinnedVMsInfoPanelVisible".equals(args.propertyName)) { //$NON-NLS-1$
                updatePinnedVMsInfoVisibility((HostMaintenanceConfirmationModel) sender);
            }
        });

        setPinnedVMsInfoMessage(object.getPinnedVMsInfoMessage());
        // Bind "pinnedVMsInfoMessage"
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("pinnedVMsInfoMessage".equals(args.propertyName)) { //$NON-NLS-1$
                HostMaintenanceConfirmationModel entity = (HostMaintenanceConfirmationModel) sender;
                setPinnedVMsInfoMessage(entity.getPinnedVMsInfoMessage());
            }
        });
    }

    @Override
    public void setMessage(String message) {
        messageHTML.setHTML(SafeHtmlUtils.fromString(message != null ? message : "").asString().replace("\n", "<br>"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public void updatePinnedVMsInfoVisibility(HostMaintenanceConfirmationModel model) {
        pinnedVMsInfoPanel.setVisible(model.getPinnedVMsInfoPanelVisible());
    }

    public void setPinnedVMsInfoMessage(String message) {
        String escapedMessage = SafeHtmlUtils.htmlEscape(message != null ? message : ""); //$NON-NLS-1$
        escapedMessage = escapedMessage.replace("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
        pinnedVMsInfoMessage.setHtmlText(SafeHtmlUtils.fromTrustedString(escapedMessage));
    }

    protected void localize() {
        latch.setLabel(constants.latchApproveOperationLabel());
        reasonEditor.setLabel(constants.reasonLabel());
        stopGlusterServices.setLabel(uiConstants.stopGlusterServices());
    }

    @Override
    public HostMaintenanceConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
