package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.widget.ScrollableAddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NetworkProfilesModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class VnicProfilesEditor extends ScrollableAddRemoveRowWidget<NetworkProfilesModel, VnicProfileModel, VnicProfileWidget> {

    interface WidgetUiBinder extends UiBinder<Widget, VnicProfilesEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private Guid dcId;

    public VnicProfilesEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    @Override
    public void edit(final NetworkProfilesModel model) {
        super.edit(model);
        model.getDcId().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                dcId = model.getDcId().getEntity();
            }
        });
    }

    @Override
    protected VnicProfileWidget createWidget(VnicProfileModel value) {
        VnicProfileWidget vnicProfileWidget = new VnicProfileWidget();
        vnicProfileWidget.edit(value);
        return vnicProfileWidget;
    }

    @Override
    protected VnicProfileModel createGhostValue() {
        VnicProfileModel profile = new NewVnicProfileModel();
        profile.initNetworkQoSList(dcId);
        return profile;
    }

    @Override
    protected boolean isGhost(VnicProfileModel value) {
        String name = value.getName().getEntity();
        return name == null || name.isEmpty();
    }

    @Override
    protected void toggleGhost(VnicProfileModel value, VnicProfileWidget widget, boolean becomingGhost) {
        super.toggleGhost(value, widget, becomingGhost);
        widget.publicUseEditor.setEnabled(!becomingGhost && value.getPublicUse().getIsChangable());
        widget.networkQoSEditor.setEnabled(!becomingGhost && value.getNetworkQoS().getIsChangable());
    }

}
