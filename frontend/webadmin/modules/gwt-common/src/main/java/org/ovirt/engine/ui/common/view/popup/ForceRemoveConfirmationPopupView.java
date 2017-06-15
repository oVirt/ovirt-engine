package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.ForceRemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public abstract class ForceRemoveConfirmationPopupView extends AbstractModelBoundPopupView<ConfirmationModel>
        implements ForceRemoveConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ConfirmationModel, ForceRemoveConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, ForceRemoveConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ForceRemoveConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "latch.entity")
    @WithElementId
    EntityModelCheckBoxEditor latch;

    @UiField
    @Ignore
    Label warningLabel;

    @UiField
    @Ignore
    HTML messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ForceRemoveConfirmationPopupView(EventBus eventBus) {
        super(eventBus);

        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        latch.setLabel(constants.approveOperation());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        updateWarning();
    }

    @Override
    public void edit(final ConfirmationModel object) {
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

        object.getItemsChangedEvent().addListener((ev, sender, args) -> updateMessage(object));
    }

    protected void updateWarning() {
        warningLabel.setText(getWarning());
    }

    protected void updateMessage(ConfirmationModel object) {
        String itemName = "<b>" + object.getItems().iterator().next() + "</b>"; //$NON-NLS-1$ //$NON-NLS-2$
        messageLabel.setHTML(SafeHtmlUtils.fromTrustedString(getFormattedMessage(itemName)));
    }

    protected abstract String getWarning();

    protected abstract String getFormattedMessage(String itemName);

    @Override
    public ConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
