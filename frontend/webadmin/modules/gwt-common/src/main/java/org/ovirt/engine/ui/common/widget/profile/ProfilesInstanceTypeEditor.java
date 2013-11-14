package org.ovirt.engine.ui.common.widget.profile;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ProfilesInstanceTypeEditor extends AddRemoveRowWidget<ListModel, VnicInstanceType, ProfileInstanceTypeEditor> implements HasElementId {

    interface Driver extends SimpleBeanEditorDriver<ListModel, ProfilesInstanceTypeEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetUiBinder extends UiBinder<Widget, ProfilesInstanceTypeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private String elementId = DOM.createUniqueId();

    @Ignore
    @UiField
    Label headerLabel;

    private static final CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);

    private ListModel profilesModel;
    private Iterable<VnicProfileView> vnicProfiles;
    private final List<VmNetworkInterface> vnics;
    private int realEntryCount;

    public ProfilesInstanceTypeEditor() {
        vnics = new ArrayList<VmNetworkInterface>();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    protected void init(ListModel model) {
        vnicProfiles = profilesModel.getItems();
        if (vnicProfiles == null) {
            vnicProfiles = new ArrayList<VnicProfileView>();
        }

        Iterable<VnicInstanceType> values = model.getItems();
        vnics.clear();
        if (values != null) {
            for (VnicInstanceType value : values) {
                vnics.add(value.getNetworkInterface());
            }
        }
        super.init(model);

        realEntryCount = vnics.size() - 1; // don't count the ghost entry
        updateHeaderLabel();
    }

    public void edit(ListModel vnicsModel, ListModel profilesModel) {
        this.profilesModel = profilesModel;
        super.edit(vnicsModel);
        driver.edit(vnicsModel);
    }

    /**
     * @deprecated Please use {@link #edit(ListModel, ListModel)} instead.
     **/
    @Deprecated
    @Override
    public void edit(ListModel model) {
        edit(model, null);
    }


    @Override
    public ListModel flush() {
        super.flush();
        return driver.flush();
    }

    private void updateHeaderLabel() {
        if (realEntryCount == 0) {
            headerLabel.setText(messages.assignNicsNothingToAssign());
        } else if (realEntryCount == 1) {
            headerLabel.setText(messages.assignNicsToProfilesSingular());
        } else {
            headerLabel.setText(messages.assignNicsToProfilesPlural(realEntryCount));
        }
    }

    @Override
    protected void onAdd(VnicInstanceType value, ProfileInstanceTypeEditor widget) {
        super.onAdd(value, widget);
        ++realEntryCount; // necessarily a ghost entry, but this will be offset when the entry is toggled to ghost
        updateHeaderLabel();
    }

    @Override
    protected void onRemove(VnicInstanceType value, ProfileInstanceTypeEditor widget) {
        super.onRemove(value, widget);
        vnics.remove(value.getNetworkInterface());
        --realEntryCount; // necessarily a real entry
        updateHeaderLabel();
    }

    @Ignore
    @Override
    protected ProfileInstanceTypeEditor createWidget(VnicInstanceType value) {
        VnicProfileView profile = (VnicProfileView) value.getSelectedItem();

        ProfileInstanceTypeEditor item = new ProfileInstanceTypeEditor();
        item.edit(value);
        item.setElementId(elementId);

        // small workaround due to UiCommonEditorVisitor changing null selected value
        value.setSelectedItem(profile);

        return item;
    }

    @Override
    protected VnicInstanceType createGhostValue() {
        VmNetworkInterface vnic = new VmNetworkInterface();
        vnic.setName(AsyncDataProvider.getNewNicName(vnics));
        vnics.add(vnic);
        VnicInstanceType vnicWithProfile = new VnicInstanceType(vnic);
        vnicWithProfile.setItems(vnicProfiles);
        return vnicWithProfile;
    }

    @Override
    protected boolean isGhost(VnicInstanceType value) {
        return (value.getSelectedItem() == null);
    }

    @Override
    protected void toggleGhost(VnicInstanceType value, ProfileInstanceTypeEditor item, boolean becomingGhost) {
        item.profileEditor.setEnabled(!becomingGhost);
        item.profileEditor.asWidget().setEnabled(true);

        realEntryCount += (becomingGhost ? -1 : 1);
        updateHeaderLabel();
    }

}
