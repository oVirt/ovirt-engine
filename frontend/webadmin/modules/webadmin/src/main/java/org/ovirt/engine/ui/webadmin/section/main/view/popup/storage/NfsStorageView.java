package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ShortEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NfsStorageView extends AbstractStorageView<NfsStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<NfsStorageModel, NfsStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, NfsStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NfsStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "path.entity")
    @WithElementId("path")
    StringEntityModelTextBoxEditor pathEditor;

    @UiField
    @Ignore
    Label pathExampleLabel;

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    VerticalPanel expanderContent;

    @UiField
    @Ignore
    Label warningLabel;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    @WithElementId("version")
    ListModelListBoxEditor<EntityModel<NfsVersion>> versionListEditor;

    @UiField
    @Path(value = "retransmissions.entity")
    @WithElementId("retransmissions")
    ShortEntityModelTextBoxEditor retransmissionsEditor;

    @UiField
    @Path(value = "timeout.entity")
    @WithElementId("timeout")
    ShortEntityModelTextBoxEditor timeoutEditor;

    @UiField
    @Path(value = "mountOptions.entity")
    @WithElementId
    StringEntityModelTextBoxEditor mountOptionsEditor;

    @UiField
    Label message;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final UIConstants uiConstants = ConstantsManager.getInstance().getConstants();

    @Inject
    public NfsStorageView() {
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        initExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    private void initExpander() {
        expander.initWithContent(expanderContent.getElement());
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());
        expanderContent.setStyleName(style.expanderContent());
        versionListEditor.setStyleName(style.versionListEditor());
        retransmissionsEditor.setStyleName(style.storageTextBoxEditor());
        timeoutEditor.setStyleName(style.storageTextBoxEditor());
        mountOptionsEditor.setStyleName(style.mountTextBoxEditor());
    }

    void initListBoxEditors() {

        versionListEditor = new ListModelListBoxEditor<>(new AbstractRenderer<EntityModel<NfsVersion>>() {
            @Override
            public String render(EntityModel<NfsVersion> model) {
                return model.getTitle();
            }
        });
    }

    void localize() {
        pathEditor.setLabel(constants.storagePopupNfsPathLabel());
        pathExampleLabel.setText(constants.storagePopupNfsPathExampleLabel());
        warningLabel.setText(constants.advancedOptionsLabel());
        versionListEditor.setLabel(constants.storagePopupNfsVersionLabel());
        retransmissionsEditor.setLabel(constants.storagePopupNfsRetransmissionsLabel());
        timeoutEditor.setLabel(constants.storagePopupNfsTimeoutLabel());
        mountOptionsEditor.setLabel(constants.storagePopupAdditionalMountOptionsLabel());
        mountOptionsEditor.setLabelTooltip(uiConstants.mountOptionsHint());
        expander.setTitleWhenCollapsed(constants.storagePopupConnectionLabel());
        expander.setTitleWhenExpanded(constants.storagePopupConnectionLabel());
    }

    @Override
    public void edit(final NfsStorageModel object) {
        driver.edit(object);

        EntityModel version = (EntityModel) object.getVersion().getSelectedItem();
        versionListEditor.asEditor().setValue(version);

        pathExampleLabel.setVisible(object.getPath().getIsChangable());

        object.getMountOptions().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName.equals("Title")) { //$NON-NLS-1$
                    mountOptionsEditor.setTitle(object.getMountOptions().getTitle());
                }
            }
        });

        setElementVisibility(pathEditor, object.getPath().getIsAvailable());
        setElementVisibility(versionListEditor, object.getVersion().getIsAvailable());
        setElementVisibility(retransmissionsEditor, object.getRetransmissions().getIsAvailable());
        setElementVisibility(timeoutEditor, object.getTimeout().getIsAvailable());
        setElementVisibility(mountOptionsEditor, object.getTimeout().getIsAvailable());
    }

    @Override
    public NfsStorageModel flush() {
        return driver.flush();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

    private void setElementVisibility(UIObject object, boolean value) {
        object.getElement().getStyle().setDisplay(value ? Style.Display.BLOCK : Style.Display.NONE);
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();

        String expanderContent();

        String storageTextBoxEditor();

        String versionListEditor();

        String mountTextBoxEditor();
    }

}
