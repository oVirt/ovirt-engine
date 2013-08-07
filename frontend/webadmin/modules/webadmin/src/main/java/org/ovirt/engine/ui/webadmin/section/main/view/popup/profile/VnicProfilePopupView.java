package org.ovirt.engine.ui.webadmin.section.main.view.popup.profile;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.form.key_value.KeyValueWidget;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.profile.VnicProfilePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.Editor.Path;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VnicProfilePopupView extends AbstractModelBoundPopupView<VnicProfileModel> implements VnicProfilePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VnicProfileModel, VnicProfilePopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VnicProfilePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VnicProfilePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    protected interface Style extends CssResource {
        String portMirroringEditor();

        String checkBox();

        String publicUseEditor();
    }

    @UiField
    protected Style style;

    @UiField
    @Path("name.entity")
    @WithElementId("name")
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path("portMirroring.entity")
    @WithElementId("portMirroring")
    protected EntityModelCheckBoxEditor portMirroringEditor;

    @UiField
    @Ignore
    public KeyValueWidget customPropertiesSheetEditor;

    @UiField(provided = true)
    @Path(value = "publicUse.entity")
    public final EntityModelCheckBoxEditor publicUseEditor;

    @UiField(provided = true)
    @Path("network.selectedItem")
    ListModelListBoxEditor<Object> networkEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VnicProfilePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        publicUseEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        networkEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((Network) object).getName();
            }
        });
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        applyStyles();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        networkEditor.setLabel(constants.profileNetworkIntefacePopup());
        nameEditor.setLabel(constants.nameNetworkIntefacePopup());
        portMirroringEditor.setLabel(constants.portMirroringNetworkIntefacePopup());
        publicUseEditor.setLabel(constants.profilePublicUseLabel());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VnicProfileModel profile) {
        driver.edit(profile);
    }

    @Override
    public void initCustomPropertySheet(final VnicProfileModel profile) {
        profile.getCustomPropertySheet().getKeyValueLines().getItemsChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                customPropertiesSheetEditor.edit(profile.getCustomPropertySheet());
            }
        });

        profile.getCustomPropertySheet().getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                String propName = ((PropertyChangedEventArgs) args).PropertyName;

                // IsChangable
                if ("IsChangable".equals(propName)) { //$NON-NLS-1$
                    customPropertiesSheetEditor.setEnabled(false);
                }

            }
        });
    }

    @Override
    public VnicProfileModel flush() {
        return driver.flush();
    }

    private void applyStyles() {
        portMirroringEditor.addContentWidgetStyleName(style.portMirroringEditor());
        publicUseEditor.addContentWidgetStyleName(style.publicUseEditor());
        publicUseEditor.asCheckBox().addStyleName(style.checkBox());
    }
}
