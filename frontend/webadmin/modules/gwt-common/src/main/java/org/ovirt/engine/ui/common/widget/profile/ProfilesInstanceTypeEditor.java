package org.ovirt.engine.ui.common.widget.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstanceType;
import org.ovirt.engine.ui.uicommonweb.models.vms.VnicInstancesModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class ProfilesInstanceTypeEditor extends AddRemoveRowWidget<VnicInstancesModel, VnicInstanceType,
    ProfileInstanceTypeEditor> implements HasElementId {

    interface WidgetUiBinder extends UiBinder<Widget, ProfilesInstanceTypeEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    public interface ProfileWidgetStyle extends WidgetStyle {
        String mainPanelPadding();
        String fullWidth();
    }

    private String elementId = DOM.createUniqueId();

    private Collection<VnicProfileView> vnicProfiles;
    private final List<VmNetworkInterface> vnics;

    @UiField
    FlowPanel mainPanel;

    public ProfilesInstanceTypeEditor() {
        vnics = new ArrayList<>();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    @Override
    protected void init(VnicInstancesModel model) {
        vnicProfiles = model.getVnicProfiles().getItems();
        if (vnicProfiles == null) {
            vnicProfiles = new ArrayList<>();
        }

        Iterable<VnicInstanceType> values = model.getItems();
        vnics.clear();
        if (values != null) {
            for (VnicInstanceType value : values) {
                vnics.add(value.getNetworkInterface());
            }
        }
        super.init(model);
    }

    @Override
    public void setUsePatternFly(boolean use) {
        super.setUsePatternFly(use);
        if (use) {
            mainPanel.removeStyleName(((ProfileWidgetStyle)style).mainPanelPadding());
            mainPanel.addStyleName(((ProfileWidgetStyle)style).fullWidth());
        }
    }

    @Override
    protected void onRemove(VnicInstanceType value, ProfileInstanceTypeEditor widget) {
        super.onRemove(value, widget);
        vnics.remove(value.getNetworkInterface());
    }

    @Ignore
    @Override
    protected ProfileInstanceTypeEditor createWidget(VnicInstanceType value) {
        VnicProfileView profile = value.getSelectedItem();

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
        vnic.setName(AsyncDataProvider.getInstance().getNewNicName(vnics));
        vnics.add(vnic);
        VnicInstanceType vnicWithProfile = new VnicInstanceType(vnic);
        vnicWithProfile.setItems(vnicProfiles);
        return vnicWithProfile;
    }

    @Override
    protected boolean isGhost(VnicInstanceType value) {
        return value.getSelectedItem() == null;
    }

    @Override
    protected void toggleGhost(VnicInstanceType value, ProfileInstanceTypeEditor widget, boolean becomingGhost) {
        super.toggleGhost(value, widget, becomingGhost);
        widget.vnicLabel.setEnabled(!becomingGhost);
    }

}
