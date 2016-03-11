package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SystemTreeItemCell extends AbstractCell<SystemTreeItemModel> {

    private String elementIdPrefix = DOM.createUniqueId();

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();

    public SystemTreeItemCell() {
    }

    @Override
    public void render(Context context, SystemTreeItemModel value, SafeHtmlBuilder sb) {
        ImageResource imageResource;

        // get the right image resource
        switch (value.getType()) {
        case Cluster:
            imageResource = resources.clusterImage();
            break;
        case Clusters:
            imageResource = resources.clustersImage();
            break;
        case DataCenter:
            imageResource = resources.dataCenterImage();
            break;
        case DataCenters:
            imageResource = resources.dataCentersImage();
            break;
        case Cluster_Gluster:
            imageResource = resources.glusterClusterImage();
            break;
        case Host:
            imageResource = resources.hostImage();
            break;
        case Hosts:
            imageResource = resources.hostsImage();
            break;
        case Storage:
            imageResource = resources.storageImage();
            break;
        case Storages:
            imageResource = resources.storagesImage();
            break;
        case System:
            imageResource = resources.systemImage();
            break;
        case Templates:
            imageResource = resources.templatesImage();
            break;
        case VMs:
            imageResource = resources.vmsImage();
            break;
        case Volume:
            imageResource = resources.volumeImage();
            break;
        case Volumes:
            imageResource = resources.volumesImage();
            break;
        case Network:
            imageResource = resources.networkTreeImage();
            break;
        case Networks:
            imageResource = resources.networksTreeImage();
            break;
        case Provider:
            switch (((Provider) value.getEntity()).getType()) {
            case EXTERNAL_NETWORK:
            case OPENSTACK_NETWORK:
            case OPENSTACK_IMAGE:
            case OPENSTACK_VOLUME:
                imageResource = resources.openstackImage();
                break;
            case FOREMAN:
                imageResource = resources.foremanImage();
                break;
            default:
                imageResource = resources.providersImage();
            }
            break;
        case Providers:
            imageResource = resources.providersImage();
            break;
        case Sessions:
            imageResource = resources.userImage_tree();
            break;
        case Errata:
            imageResource = resources.errataImage();
            break;

        default:
            imageResource = resources.questionMarkImage();
        }

        // get the image HTML
        SafeHtml imageHtml = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(imageResource).getHTML());

        // apply to template
        sb.append(templates.treeItem(imageHtml, value.getTitle(),
                ElementIdUtils.createTreeCellElementId(elementIdPrefix, value, null)));
    }

    public void setElementIdPrefix(String elementIdPrefix) {
        this.elementIdPrefix = elementIdPrefix;
    }

}
