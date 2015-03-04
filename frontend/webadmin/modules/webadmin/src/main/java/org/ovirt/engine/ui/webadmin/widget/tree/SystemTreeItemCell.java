package org.ovirt.engine.ui.webadmin.widget.tree;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SystemTreeItemCell extends AbstractCell<SystemTreeItemModel> {

    private final ApplicationResources applicationResources;
    private final ApplicationTemplates templates;

    private String elementIdPrefix = DOM.createUniqueId();

    public SystemTreeItemCell(ApplicationResources applicationResources, ApplicationTemplates templates) {
        this.applicationResources = applicationResources;
        this.templates = templates;
    }

    @Override
    public void render(Context context, SystemTreeItemModel value, SafeHtmlBuilder sb) {
        ImageResource imageResource;

        // get the right image resource
        switch (value.getType()) {
        case Cluster:
            imageResource = applicationResources.clusterImage();
            break;
        case Clusters:
            imageResource = applicationResources.clustersImage();
            break;
        case DataCenter:
            imageResource = applicationResources.dataCenterImage();
            break;
        case DataCenters:
            imageResource = applicationResources.dataCentersImage();
            break;
        case Cluster_Gluster:
            imageResource = applicationResources.glusterClusterImage();
            break;
        case Host:
            imageResource = applicationResources.hostImage();
            break;
        case Hosts:
            imageResource = applicationResources.hostsImage();
            break;
        case Storage:
            imageResource = applicationResources.storageImage();
            break;
        case Storages:
            imageResource = applicationResources.storagesImage();
            break;
        case System:
            imageResource = applicationResources.systemImage();
            break;
        case Templates:
            imageResource = applicationResources.templatesImage();
            break;
        case VMs:
            imageResource = applicationResources.vmsImage();
            break;
        case Volume:
            imageResource = applicationResources.volumeImage();
            break;
        case Volumes:
            imageResource = applicationResources.volumesImage();
            break;
        case Network:
            imageResource = applicationResources.networkTreeImage();
            break;
        case Networks:
            imageResource = applicationResources.networksTreeImage();
            break;
        case Provider:
            switch (((Provider) value.getEntity()).getType()) {
            case OPENSTACK_NETWORK:
            case OPENSTACK_IMAGE:
                imageResource = applicationResources.openstackImage();
                break;
            case FOREMAN:
                imageResource = applicationResources.foremanImage();
                break;
            default:
                imageResource = applicationResources.providersImage();
            }
            break;
        case Providers:
            imageResource = applicationResources.providersImage();
            break;
        case Sessions:
            imageResource = applicationResources.userImage();
            break;

        default:
            imageResource = applicationResources.questionMarkImage();
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
