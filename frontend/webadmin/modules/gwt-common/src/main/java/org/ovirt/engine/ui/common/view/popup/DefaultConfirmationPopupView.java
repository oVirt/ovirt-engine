package org.ovirt.engine.ui.common.view.popup;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class DefaultConfirmationPopupView extends AbstractConfirmationPopupView implements DefaultConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<ConfirmationModel, DefaultConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DefaultConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DefaultConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    FlowPanel descriptionPanel;

    @UiField(provided = true)
    @Path(value = "doNotShowAgain.entity")
    @WithElementId
    protected EntityModelCheckBoxEditor doNotShowAgain;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DefaultConfirmationPopupView(EventBus eventBus) {
        super(eventBus);

        doNotShowAgain = new EntityModelCheckBoxEditor(Align.RIGHT);
        doNotShowAgain.setLabel(constants.doNotShowAgain());

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(final ConfirmationModel object) {
        driver.edit(object);

        messagePanel.setAlertType(AlertType.valueOf(object.getAlertType().name()));

        object.getItemsChangedEvent().addListener((ev, sender, args) -> {
            ArrayList<String> items = (ArrayList<String>) object.getItems();

            for (String item : items) {
                descriptionPanel.add(new Label(getItemTextFormatted(item)));
            }
        });

        // Bind "DoNotShowAgain.IsAvailable"
        object.getDoNotShowAgain().getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                EntityModel<?> entity = (EntityModel<?>) sender;
                doNotShowAgain.setVisible(entity.getIsAvailable());
            }
        });
    }

    private String getItemTextFormatted(String itemText) {
        return "- " + itemText; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public ConfirmationModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
