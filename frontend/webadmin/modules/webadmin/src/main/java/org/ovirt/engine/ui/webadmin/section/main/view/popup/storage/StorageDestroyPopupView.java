package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageDestroyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.Align;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCheckBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class StorageDestroyPopupView extends AbstractModelBoundPopupView<ConfirmationModel>
        implements StorageDestroyPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfirmationModel, StorageDestroyPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, StorageDestroyPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<StorageDestroyPopupView> {
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

    String rawMessage;

    @Inject
    public StorageDestroyPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
    }

    void localize(ApplicationConstants constants) {
        warningLabel.setText(constants.storageDestroyPopupWarningLabel());
        rawMessage = constants.storageDestroyPopupMessageLabel();
    }

    @Override
    public void edit(final ConfirmationModel object) {
        Driver.driver.edit(object);

        // Bind "Latch.IsAvailable"
        object.getLatch().getPropertyChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if ("IsAvailable".equals(((PropertyChangedEventArgs) args).PropertyName)) {
                    EntityModel entity = (EntityModel) sender;
                    if (entity.getIsAvailable()) {
                        latch.setVisible(true);
                    }
                }
            }
        });

        object.getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                updateMessage(object);
            }
        });
    }

    private void updateMessage(ConfirmationModel object) {
        String storageName = "<b>" + object.getItems().iterator().next() + "</b>";
        String formattedMessage = StringFormat.format(rawMessage, storageName);
        messageLabel.setHTML(SafeHtmlUtils.fromTrustedString(formattedMessage));
    }

    @Override
    public ConfirmationModel flush() {
        return Driver.driver.flush();
    }

}
