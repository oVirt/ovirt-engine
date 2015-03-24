package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.ShortEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBox;
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
    @WithElementId
    @Path(value = "path.entity")
    StringEntityModelTextBoxOnlyEditor pathEditor;

    @UiField
    @Ignore
    Label pathLabel;

    @UiField
    @Ignore
    Label pathHintLabel;

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    Label warningLabel;

    @UiField(provided = true)
    @Path(value = "override.entity")
    @WithElementId("overrideEditor")
    EntityModelCheckBoxEditor overrideEditor;

    @UiField
    @Ignore
    TableElement expanderContent;

    @UiField(provided = true)
    @WithElementId
    @Path(value = "version.selectedItem")
    ListModelListBoxOnlyEditor<EntityModel<NfsVersion>> versionEditor;

    @UiField
    @Ignore
    StringEntityModelTextBoxOnlyEditor versionReadOnlyEditor;

    @UiField
    @Ignore
    Label versionLabel;

    @UiField
    @WithElementId
    @Path(value = "retransmissions.entity")
    ShortEntityModelTextBoxOnlyEditor retransmissionsEditor;

    @UiField
    @Ignore
    Label retransmissionsLabel;

    @UiField
    @WithElementId
    @Path(value = "timeout.entity")
    ShortEntityModelTextBoxOnlyEditor timeoutEditor;

    @UiField
    @Ignore
    Label timeoutLabel;

    @UiField
    @WithElementId
    @Path(value = "mountOptions.entity")
    StringEntityModelTextBoxOnlyEditor mountOptionsEditor;

    @UiField
    @Ignore
    Label mountOptionsLabel;

    @UiField
    Label message;

    private final Driver driver = GWT.create(Driver.class);

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public NfsStorageView() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        initExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    private void initExpander() {
        expander.initWithContent(expanderContent);
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());

        expanderContent.setClassName(style.expanderContent());

    }

    void initEditors() {

        versionEditor = new ListModelListBoxOnlyEditor<EntityModel<NfsVersion>>(new AbstractRenderer<EntityModel<NfsVersion>>() {
            @Override
            public String render(EntityModel<NfsVersion> model) {
                return model.getTitle();
            }
        });

        overrideEditor = new EntityModelCheckBoxEditor(Align.RIGHT, new VisibilityRenderer.SimpleVisibilityRenderer(), true);
    }

    void localize() {
        pathLabel.setText(constants.storagePopupNfsPathLabel());
        pathHintLabel.setText(constants.storagePopupNfsPathHintLabel());
        warningLabel.setText(constants.advancedOptionsLabel());
        overrideEditor.setLabel(constants.storagePopupNfsOverrideLabel());
        versionLabel.setText(constants.storagePopupNfsVersionLabel());
        retransmissionsLabel.setText(constants.storagePopupNfsRetransmissionsLabel());
        timeoutLabel.setText(constants.storagePopupNfsTimeoutLabel());
        mountOptionsLabel.setText(constants.storagePopupAdditionalMountOptionsLabel());
        expander.setTitleWhenCollapsed(constants.storagePopupConnectionLabel());
        expander.setTitleWhenExpanded(constants.storagePopupConnectionLabel());
    }

    @Override
    public void edit(final NfsStorageModel object) {
        driver.edit(object);

        EntityModel version = (EntityModel) object.getVersion().getSelectedItem();
        versionReadOnlyEditor.asValueBox().setValue(version != null ? version.getTitle() : null);

        pathHintLabel.setVisible(object.getPath().getIsChangable());

        object.getMountOptions().getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if (args.propertyName.equals("Title")) { //$NON-NLS-1$
                    mountOptionsEditor.setTitle(object.getMountOptions().getTitle());
                }
            }
        });

        styleTextBoxEditor(pathEditor, object.getPath().getIsChangable());
        styleTextBoxEditor(timeoutEditor,  object.getOverride().getIsChangable());
        styleTextBoxEditor(retransmissionsEditor, object.getOverride().getIsChangable());
        styleTextBoxEditor(versionReadOnlyEditor, object.getOverride().getIsChangable());
        styleTextBoxEditor(mountOptionsEditor, object.getOverride().getIsChangable());

        setElementVisibility(versionEditor,  object.getOverride().getIsChangable());
        setElementVisibility(versionReadOnlyEditor, !object.getOverride().getIsChangable());
        setElementVisibility(versionLabel, object.getVersion().getIsAvailable());
        setElementVisibility(retransmissionsLabel, object.getRetransmissions().getIsAvailable());
        setElementVisibility(timeoutLabel, object.getTimeout().getIsAvailable());
        setElementVisibility(mountOptionsLabel, object.getMountOptions().getIsAvailable());

        // When all advanced fields are unavailable - hide the expander.
        boolean anyField = object.getVersion().getIsAvailable()
            || object.getRetransmissions().getIsAvailable()
            || object.getTimeout().getIsAvailable()
            || object.getMountOptions().getIsAvailable();

        expander.getElement().getStyle().setVisibility(anyField ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
    }

    @Override
    public NfsStorageModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();

        String expanderContent();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

    /*
     * Makes a provided editor look like label (enabled, read-only textbox).
     */
    private <T> void styleTextBoxEditor(EntityModelTextBoxOnlyEditor<T> editor, boolean enabled) {

        if (!enabled) {

            editor.setEnabled(true);

            ValueBox<T> valueBox = editor.asValueBox();
            valueBox.setReadOnly(true);
            valueBox.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        }
    }

    private void setElementVisibility(UIObject object, boolean value) {

        object.getElement().getStyle().setDisplay(value ? Style.Display.BLOCK : Style.Display.NONE);
    }

}
