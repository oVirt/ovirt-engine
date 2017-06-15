package org.ovirt.engine.ui.common.view.popup.numa;

import java.util.Iterator;

import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VirtualNumaPanel extends Composite implements HasWidgets {
    interface WidgetUiBinder extends UiBinder<Widget, VirtualNumaPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    FlowPanel container;

    @UiField
    Image virtualNodeStatus;

    @UiField
    Image numaState;

    @UiField
    Label virtualNodeName;

    ImageResource vNumaIcon;
    ImageResource partialVNumaIcon;
    ImageResource pinnedVNumaIcon;
    ImageResource pinnedPartialVNumaIcon;

    @UiField(provided = true)
    static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public VirtualNumaPanel() {
        initializeResouceIcons();

        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
    }

    private void initializeResouceIcons() {
        pinnedPartialVNumaIcon = resources.darkPinnedPartialVNumaIcon();
        pinnedVNumaIcon = resources.darkPinnedVNumaIcon();
        partialVNumaIcon = resources.darkPartialVNumaIcon();
        vNumaIcon = resources.darkVNumaIcon();
    }

    @Override
    public void setStyleName(String className) {
        container.setStyleName(className);
    }

    public void setModel(VNodeModel nodeModel) {
        virtualNodeName.setText(messages.vNumaName(nodeModel.getVm().getName(), nodeModel.getIndex()));
        setStatusIcon(nodeModel);
        setTypeIcon(nodeModel);
    }

    protected void setStatusIcon(VNodeModel nodeModel) {
        if (VMStatus.Up.equals(nodeModel.getVm().getStatus())) {
            virtualNodeStatus.setResource(resources.upImage());
        } else if (VMStatus.Down.equals(nodeModel.getVm().getStatus())) {
            virtualNodeStatus.setResource(resources.downImage());
        } else {
            //Unknown status
            virtualNodeStatus.setResource(resources.questionMarkImage());
        }
    }

    protected void setTypeIcon(VNodeModel nodeModel) {
        if (nodeModel.isPinned()) {
            if (nodeModel.isSplitted()) {
                numaState.setResource(pinnedPartialVNumaIcon);
            } else {
                numaState.setResource(pinnedVNumaIcon);
            }
        } else {
            if (nodeModel.isSplitted()) {
                numaState.setResource(partialVNumaIcon);
            } else {
                numaState.setResource(vNumaIcon);
            }
        }
    }

    public ImageResource getvNumaIcon() {
        return vNumaIcon;
    }

    public void setvNumaIcon(ImageResource vNumaIcon) {
        this.vNumaIcon = vNumaIcon;
    }

    public ImageResource getPartialVNumaIcon() {
        return partialVNumaIcon;
    }

    public void setPartialVNumaIcon(ImageResource partialVNumaIcon) {
        this.partialVNumaIcon = partialVNumaIcon;
    }

    public ImageResource getPinnedVNumaIcon() {
        return pinnedVNumaIcon;
    }

    public void setPinnedVNumaIcon(ImageResource pinnedVNumaIcon) {
        this.pinnedVNumaIcon = pinnedVNumaIcon;
    }

    public ImageResource getPinnedPartialVNumaIcon() {
        return pinnedPartialVNumaIcon;
    }

    public void setPinnedPartialVNumaIcon(ImageResource pinnedPartialVNumaIcon) {
        this.pinnedPartialVNumaIcon = pinnedPartialVNumaIcon;
    }

    @Override
    public void add(Widget w) {
        container.insert(w, 0);
    }

    @Override
    public void clear() {
        throw new RuntimeException("Can't clear this widget"); //$NON-NLS-1$
    }

    @Override
    public Iterator<Widget> iterator() {
        return container.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return container.remove(w);
    }
}
