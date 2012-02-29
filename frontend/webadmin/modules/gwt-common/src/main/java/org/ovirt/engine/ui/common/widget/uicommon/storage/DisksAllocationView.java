package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class DisksAllocationView extends Composite implements HasEditorDriver<DisksAllocationModel> {

    interface Driver extends SimpleBeanEditorDriver<DisksAllocationModel, DisksAllocationView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, DisksAllocationView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "isSingleStorageDomain.entity")
    EntityModelCheckBoxEditor isSingleStorageEditor;

    @UiField(provided = true)
    @Path(value = "storageDomain.selectedItem")
    ListModelListBoxEditor<Object> singleStorageEditor;

    @UiField
    ScrollPanel diskListPanel;

    boolean showVolumeType;

    CommonApplicationConstants constants;

    @UiConstructor
    public DisksAllocationView() {
    }

    public DisksAllocationView(CommonApplicationConstants constants) {
        this.constants = constants;
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(constants);
        addStyles();
        Driver.driver.initialize(this);
    }

    void initListBoxEditors() {
        isSingleStorageEditor = new EntityModelCheckBoxEditor(Align.RIGHT);

        singleStorageEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_domains) object).getstorage_name();
            }
        });
    }

    void addStyles() {
        singleStorageEditor.setLabelStyleName(style.singleStorageEditorLabel());
        singleStorageEditor.addContentWidgetStyleName(style.singleStorageEditorContent());
        isSingleStorageEditor.addContentWidgetStyleName(style.isSingleStorageEditorContent());
    }

    void localize(CommonApplicationConstants constants) {
        isSingleStorageEditor.setLabel(constants.singleDestinationStorage());
    }

    @Override
    public void edit(DisksAllocationModel model) {
        Driver.driver.edit(model);
        initListerners(model);
    }

    private void initListerners(final DisksAllocationModel model) {
        model.getPropertyChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                if (args instanceof PropertyChangedEventArgs) {
                    PropertyChangedEventArgs changedArgs = (PropertyChangedEventArgs) args;
                    if ("Disks".equals(changedArgs.PropertyName)) {
                        addDiskList(model);
                    }
                }
            }
        });
    }

    void addDiskList(DisksAllocationModel model) {
        VerticalPanel container = new VerticalPanel();
        container.setWidth("100%");

        for (final DiskModel diskModel : model.getDisks()) {
            DisksAllocationItemView disksAllocationItemView = new DisksAllocationItemView(constants);
            disksAllocationItemView.edit(diskModel);
            container.add(disksAllocationItemView);
        }

        diskListPanel.clear();
        diskListPanel.add(container);
    }

    @Override
    public DisksAllocationModel flush() {
        return Driver.driver.flush();
    }

    public void setListHeight(String listHeight) {
        diskListPanel.setHeight(listHeight);
    }

    public void setVolumeType(boolean showVolumeType) {
        this.showVolumeType = showVolumeType;
    }

    public void setEnabled(boolean enabled) {
        isSingleStorageEditor.setEnabled(enabled);
        singleStorageEditor.setEnabled(enabled);
    }

    interface WidgetStyle extends CssResource {
        String singleStorageEditorLabel();

        String singleStorageEditorContent();

        String isSingleStorageEditorContent();
    }

}
