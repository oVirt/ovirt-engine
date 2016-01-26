package org.ovirt.engine.ui.common.view.popup.numa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.view.CollapsiblePanelView;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AssignedVNumaNodesPanel extends Composite {

    interface WidgetUiBinder extends UiBinder<Widget, AssignedVNumaNodesPanel> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface Style extends CssResource {
        String numaTitle();
    }

    private final VNumaTitleTemplate titleTemplate;

    @UiField
    FlowPanel container;

    @UiField
    Style style;

    private final Provider<DraggableVirtualNumaPanel> virtualNumaPanelProvider;

    private final CollapsiblePanelView collapsiblePanel;

    private final DragTargetScrollPanel scrollPanel;

    private static final CommonApplicationResources resources = AssetProvider.getResources();

    @Inject
    public AssignedVNumaNodesPanel(CollapsiblePanelView collapsiblePanel,
            Provider<DraggableVirtualNumaPanel> virtualNumaPanelProvider, DragTargetScrollPanel scrollPanel,
            VNumaTitleTemplate vNumaTitleTemplate) {
        this.collapsiblePanel = collapsiblePanel;
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        container.add(collapsiblePanel);
        this.virtualNumaPanelProvider = virtualNumaPanelProvider;
        this.scrollPanel = scrollPanel;
        this.titleTemplate = vNumaTitleTemplate;
        collapsiblePanel.addContentWidget(scrollPanel);
    }

    public void setNodes(Collection<VNodeModel> virtualNodes, int numaNodeIndex, List<VdsNumaNode> numaNodeList) {
        String myImageHtml = AbstractImagePrototype.create(resources.vNumaTitleIcon()).getHTML();
        SafeHtml mySafeImageHtml = new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(myImageHtml);
        if (virtualNodes == null) {
            virtualNodes = new ArrayList<>();
        }
        SafeHtml title = titleTemplate.title(virtualNodes.size(), mySafeImageHtml, style.numaTitle());
        collapsiblePanel.setTitleWidget(new HTML(title));
        scrollPanel.setIndex(numaNodeIndex);
        scrollPanel.clear();
        for (VNodeModel nodeModel: virtualNodes) {
            DraggableVirtualNumaPanel numaNodePanel = virtualNumaPanelProvider.get();
            numaNodePanel.setModel(nodeModel, numaNodeList);
            scrollPanel.add(numaNodePanel);
        }
    }
}
