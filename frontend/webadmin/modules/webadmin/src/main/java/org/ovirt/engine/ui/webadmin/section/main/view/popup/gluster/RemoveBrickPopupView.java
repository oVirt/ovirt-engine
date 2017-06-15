package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import java.util.ArrayList;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.gluster.RemoveBrickModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.RemoveBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class RemoveBrickPopupView extends AbstractModelBoundPopupView<RemoveBrickModel> implements RemoveBrickPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<RemoveBrickModel, RemoveBrickPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, RemoveBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RemoveBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    Alert message;

    @UiField
    Alert itemsAlert;

    @UiField
    FlowPanel migratePanel;

    @UiField(provided = true)
    @Path("migrateData.entity")
    EntityModelCheckBoxEditor migrateEditor;

    @UiField(provided = true)
    InfoIcon migrateInfoIcon;

    @UiField
    @Ignore
    Alert warning;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RemoveBrickPopupView(EventBus eventBus) {
        super(eventBus);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void initEditors() {
        migrateEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        migrateInfoIcon = new InfoIcon(templates.italicText(constants.removeBricksMigrateDataInfo()));
    }

    @Override
    public void edit(final RemoveBrickModel object) {
        driver.edit(object);

        object.getItemsChangedEvent().addListener((ev, sender, args) -> {
            ArrayList<String> items = (ArrayList<String>) object.getItems();

            for (String item : items) {
                itemsAlert.add(new Label(getItemTextFormatted(item)));
            }
        });

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;

            if ("IsMigrationSupported".equals(propName)) { //$NON-NLS-1$
                migratePanel.setVisible(object.isMigrationSupported());
            }
        });

        object.getMigrateData().getEntityChangedEvent().addListener((ev, sender, args) -> warning.setVisible(!object.getMigrateData().getEntity()));
    }

    private String getItemTextFormatted(String itemText) {
        return "- " + itemText; //$NON-NLS-1$
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
    }

    @Override
    public RemoveBrickModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}
