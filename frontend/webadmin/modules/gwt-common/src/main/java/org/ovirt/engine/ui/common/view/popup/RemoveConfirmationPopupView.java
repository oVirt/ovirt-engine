package org.ovirt.engine.ui.common.view.popup;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class RemoveConfirmationPopupView extends AbstractConfirmationPopupView implements RemoveConfirmationPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<ConfirmationModel, RemoveConfirmationPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, RemoveConfirmationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RemoveConfirmationPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

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

    @UiField
    FlowPanel reasonPanel;

    @Inject
    public RemoveConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
        latch = new EntityModelCheckBoxEditor(Align.RIGHT);
        latch.setLabel(constants.approveOperation());
        force = new EntityModelCheckBoxEditor(Align.RIGHT);
        force.setLabel(constants.forceRemove());
        force.getContentWidgetContainer().getElement().getStyle().setWidth(90, Unit.PCT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        driver.initialize(this);
        reasonPanel.setVisible(false);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message == null ? constants.removeConfirmationPopupMessage() : message);
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
    public void edit(ConfirmationModel object) {
        driver.edit(object);

        // Bind "Latch.IsAvailable"
        object.getLatch().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsAvailable".equals(args.propertyName)) { //$NON-NLS-1$
                    EntityModel entity = (EntityModel) sender;
                    if (entity.getIsAvailable()) {
                        latch.setVisible(true);
                    }
                }
            }
        });

        if (object.getForceLabel() != null) {
            force.setLabel(object.getForceLabel());
        }

        force.asCheckBox().setValue(object.getForce().getEntity());
        // Bind "Force.Label"
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("ForceLabel".equals(args.propertyName)) { //$NON-NLS-1$
                    ConfirmationModel entity = (ConfirmationModel) sender;
                    force.setLabel(entity.getForceLabel());
                }
            }
        });

        setNote(object.getNote());
        // Bind "Note"
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("Note".equals(args.propertyName)) { //$NON-NLS-1$
                    ConfirmationModel entity = (ConfirmationModel) sender;
                    setNote(entity.getNote());
                }
            }
        });

        // Bind "ReasonVisible"
        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("ReasonVisible".equals(args.propertyName)) { //$NON-NLS-1$
                    updateReasonVisibility((ConfirmationModel) sender);
                }
            }
        });
    }

    public void updateReasonVisibility(ConfirmationModel model) {
        reasonPanel.setVisible(model.getReasonVisible());
    }

    protected void localize() {
        latch.setLabel(constants.latchApproveOperationLabel());
        reasonEditor.setLabel(constants.reasonLabel());
    }

    @Override
    public ConfirmationModel flush() {
        return driver.flush();
    }

}
