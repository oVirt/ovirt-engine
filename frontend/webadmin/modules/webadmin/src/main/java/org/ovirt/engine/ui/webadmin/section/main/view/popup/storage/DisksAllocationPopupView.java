package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.DisksAllocationView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.DisksAllocationPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.inject.Inject;

public class DisksAllocationPopupView extends AbstractModelBoundPopupView<DisksAllocationModel> implements DisksAllocationPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DisksAllocationPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface Driver extends SimpleBeanEditorDriver<DisksAllocationModel, DisksAllocationPopupView> {
    }

    final Driver driver = GWT.create(Driver.class);

    @UiField
    WidgetStyle style;

    SafeHtml warningImage;

    @UiField
    FlowPanel messagePanel;

    @UiField
    HorizontalPanel warningPanel;

    @UiField(provided = true)
    @Path(value = "dynamicWarning.entity")
    StringEntityModelTextAreaLabelEditor dynamicWarningLabel;

    @UiField(provided = true)
    @Ignore
    DisksAllocationView disksAllocationView;

    DisksAllocationModel disksAllocationModel;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    @Inject
    public DisksAllocationPopupView(EventBus eventBus) {
        super(eventBus);

        warningImage = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(
                resources.logWarningImage()).getHTML());

        disksAllocationView = new DisksAllocationView();
        dynamicWarningLabel = new StringEntityModelTextAreaLabelEditor();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        addStyles();
        driver.initialize(this);
    }

    @Override
    public void edit(DisksAllocationModel object) {
        driver.edit(object);

        disksAllocationView.edit(object);
        disksAllocationModel = object;

        object.getDynamicWarning().getPropertyChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                EntityModel ownerModel = (EntityModel) sender;
                String propName = ((PropertyChangedEventArgs) args).propertyName;

                if ("IsAvailable".equals(propName)) { //$NON-NLS-1$
                    warningPanel.setVisible(ownerModel.getIsAvailable());
                }
            }
        });
    }

    private void addStyles() {
        dynamicWarningLabel.setCustomStyle(style.dynamicWarningTextArea());
        dynamicWarningLabel.hideLabel();
    }

    @Override
    public DisksAllocationModel flush() {
        driver.flush();
        return disksAllocationView.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);

        if (message != null && !message.isEmpty()) {
            messagePanel.add(new HTML(templates.iconWithText(warningImage, message)));
        }

        messagePanel.setVisible(messagePanel.iterator().hasNext());
    }

    interface WidgetStyle extends CssResource {
        String messagePanel();

        String dynamicWarningTextArea();
    }
}
