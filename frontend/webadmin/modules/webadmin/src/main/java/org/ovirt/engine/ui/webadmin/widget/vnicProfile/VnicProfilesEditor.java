package org.ovirt.engine.ui.webadmin.widget.vnicProfile;

import java.util.Collection;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.widget.AddRemoveRowWidget;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

public class VnicProfilesEditor extends AddRemoveRowWidget<ListModel, VnicProfileModel, VnicProfileWidget> {

    interface Driver extends SimpleBeanEditorDriver<ListModel, VnicProfilesEditor> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetUiBinder extends UiBinder<Widget, VnicProfilesEditor> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private Collection<VnicProfileModel> profiles;
    private Version dcCompatibilityVersion;
    private Guid dcId;
    private VnicProfileModel defaultProfile;

    public VnicProfilesEditor() {
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    public void edit(ListModel model, Version dcCompatibilityVersion, Guid dcId, VnicProfileModel defaultProfile) {
        driver.edit(model);
        profiles = (Collection<VnicProfileModel>) model.getItems();
        this.dcCompatibilityVersion = dcCompatibilityVersion;
        this.dcId = dcId;
        this.defaultProfile = defaultProfile;
        init(model);
    }

    @Override
    public void edit(ListModel model) {
        edit(model, dcCompatibilityVersion, dcId, defaultProfile);
    }

    public ListModel flush() {
        ListModel model = driver.flush();
        flush(model);
        return model;
    }

    @Override
    protected VnicProfileWidget createWidget(VnicProfileModel value) {
        VnicProfileWidget vnicProfileWidget = new VnicProfileWidget();
        vnicProfileWidget.edit(value);
        return vnicProfileWidget;
    }

    @Override
    protected VnicProfileModel createGhostValue() {
        return new NewVnicProfileModel(dcCompatibilityVersion, dcId);
    }

    @Override
    protected boolean isGhost(VnicProfileModel value) {
        if (value != defaultProfile) {
            String name = (String) value.getName().getEntity();
            return (name == null || name.isEmpty());
        }
        return false;
    }

    @Override
    protected void toggleGhost(VnicProfileModel value, VnicProfileWidget widget, boolean becomingGhost) {
        widget.publicUseEditor.setEnabled(!becomingGhost && value.getPublicUse().getIsChangable());
        widget.networkQoSEditor.setEnabled(!becomingGhost && value.getNetworkQoS().getIsChangable());

        // commit change to model without triggering items changed event
        if (profiles != null) {
            if (becomingGhost) {
                profiles.remove(value);
            } else if (!Linq.containsByIdentity(profiles, value)) {
                profiles.add(value);
            }
        }
    }

    @Override
    protected void onRemove(VnicProfileModel value, VnicProfileWidget widget) {
        super.onRemove(value, widget);

        // commit change to model without triggering items changed event
        if (profiles != null) {
            profiles.remove(value);
        }
    }

}
