package org.ovirt.engine.ui.webadmin.widget.table.cell;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils;
import org.ovirt.engine.ui.frontend.utils.GlusterVolumeUtils.VolumeStatus;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class VolumeStatusCell extends AbstractCell<GlusterVolumeEntity> {

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private UICommand onClickCommand = null;

    protected ImageResource downImage = resources.downImage();
    protected ImageResource upImage = resources.upImage();
    protected ImageResource allBricksDownImage = resources.volumeAllBricksDownWarning();
    protected ImageResource volumeSomeBricksDownImage = resources.volumeBricksDownWarning();

    public VolumeStatusCell() {
    }

    public VolumeStatusCell(UICommand onClickCommand) {
        this.onClickCommand = onClickCommand;
    }

    @Override
    public Set<String> getConsumedEvents() {
        Set<String> set = new HashSet<>(super.getConsumedEvents());
        set.add(BrowserEvents.CLICK);
        return set;
    }

    @Override
    public void onBrowserEvent(Context context,
            Element parent,
            GlusterVolumeEntity volume,
            NativeEvent event, ValueUpdater<GlusterVolumeEntity> valueUpdater) {
        super.onBrowserEvent(context, parent, volume, event, valueUpdater);
        VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);

        if (BrowserEvents.CLICK.equals(event.getType()) && onClickCommand != null && (status == VolumeStatus.ALL_BRICKS_DOWN || status == VolumeStatus.SOME_BRICKS_DOWN)) {
            onClickCommand.execute();
        }
    }

    protected ImageResource getStatusImage(VolumeStatus vStatus) {
        // Find the image corresponding to the status of the volume:
        ImageResource statusImage = null;

        switch (vStatus) {
        case DOWN:
            return downImage;
        case UP :
            return upImage;
        case ALL_BRICKS_DOWN :
            return allBricksDownImage;
        case SOME_BRICKS_DOWN :
            return volumeSomeBricksDownImage;
        }
        return statusImage;
    }

    @Override
    public void render(Context context, GlusterVolumeEntity volume, SafeHtmlBuilder sb, String id) {
        // Nothing to render if no volume is provided:
        if (volume == null) {
            return;
        }

        VolumeStatus status = GlusterVolumeUtils.getVolumeStatus(volume);
        ImageResource statusImage = getStatusImage(status);

        // Generate the HTML for the image:
        SafeHtml statusImageHtml =
                SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(statusImage).getHTML());

        if (status == VolumeStatus.ALL_BRICKS_DOWN || status == VolumeStatus.SOME_BRICKS_DOWN
                || GlusterVolumeUtils.isHealingRequired(volume)) {
            SafeHtml alertImageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.alertImage()).getHTML());
            sb.append(templates.statusWithAlertTemplate(statusImageHtml, alertImageHtml, id, status.toString()));
        } else {
            sb.append(templates.statusTemplate(statusImageHtml, id, status.toString()));
        }
    }

}
