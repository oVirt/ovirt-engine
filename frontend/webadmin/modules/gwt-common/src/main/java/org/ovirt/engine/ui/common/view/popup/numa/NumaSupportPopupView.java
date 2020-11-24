package org.ovirt.engine.ui.common.view.popup.numa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsNumaNode;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.popup.numa.NumaSupportPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.NumaSupportModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.numa.VNodeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.gwtplatform.mvp.client.View;

public class NumaSupportPopupView extends AbstractModelBoundPopupView<NumaSupportModel>
    implements NumaSupportPopupPresenterWidget.ViewDef {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, NumaSupportPopupView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NumaSupportPopupView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Style extends CssResource {
        String hostSummaryNumaTitle();
    }

    static final String DELIMITER = ", "; //$NON-NLS-1$
    private static final int SPLITTER_THICKNESS = 1;

    @Inject
    Provider<CpuSummaryPanel> cpuSummaryPanelProvider;

    @Inject
    Provider<MemorySummaryPanel> memorySummaryPanelProvider;

    @Inject
    Provider<SocketPanel> socketPanelProvider;

    @Inject
    Provider<NumaPanel> numaPanelProvider;

    @Inject
    Provider<AssignedVNumaNodesPanel> assignedVNumaPanelProvider;

    @Inject
    Provider<HostSummaryContentPanel> hostSummaryContentProvider;

    private static final CommonApplicationResources resources = AssetProvider.getResources();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @UiField
    FlowPanel groupPanel;

    @UiField
    FlowPanel hostSummaryPanel;

    @UiField
    FlowPanel unassignedvNumaNodesContainer;

    @UiField
    FlowPanel socketListPanel;

    @UiField(provided = true)
    SplitLayoutPanel horizontalSplitLayoutPanel;

    @UiField
    Style style;

    final VNumaTitleTemplate vNumaTitleTemplate;
    final NumaTitleTemplate numaTitleTemplate;

    @Inject
    public NumaSupportPopupView(EventBus eventBus, ClientStorage clientStorage, VNumaTitleTemplate vNumaTitleTemplate,
            NumaTitleTemplate numaTitleTemplate) {
        super(eventBus);
        this.numaTitleTemplate = numaTitleTemplate;
        this.vNumaTitleTemplate = vNumaTitleTemplate;
        horizontalSplitLayoutPanel = new SplitLayoutPanel(SPLITTER_THICKNESS);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        horizontalSplitLayoutPanel.setWidgetToggleDisplayAllowed(unassignedvNumaNodesContainer, true);
    }

    @Override
    public void edit(NumaSupportModel object) {
    }

    @Override
    public NumaSupportModel flush() {
        return null;
    }

    @Override
    public void cleanup() {
        // TODO clean up stuff if needed
    }

    @Override
    public void setUnassignedGroupPanel(View view) {
        unassignedvNumaNodesContainer.insert(view, 1);
    }

    @Override
    public IsWidget getHostSummaryTitle(int totalCpus, int usedCpuPercentage, int totalMemory, int usedMemory,
            int totalNumaNodes, int totalVNumaNodes) {
        FlowPanel summaryPanel = new FlowPanel();
        CpuSummaryPanel cpuSummaryPanel = this.cpuSummaryPanelProvider.get();
        cpuSummaryPanel.setName(constants.numaSummaryTotals());
        cpuSummaryPanel.setCpus(totalCpus, usedCpuPercentage);
        summaryPanel.add(cpuSummaryPanel);

        MemorySummaryPanel memorySummaryPanel = this.memorySummaryPanelProvider.get();
        memorySummaryPanel.setMemoryStats(totalMemory, usedMemory);
        summaryPanel.add(memorySummaryPanel);

        summaryPanel.add(new HTML(numaTitleTemplate.title(totalNumaNodes, style.hostSummaryNumaTitle())));

        String myImageHtml = AbstractImagePrototype.create(resources.vNumaTitleIcon()).getHTML();
        SafeHtml mySafeImageHtml = new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(myImageHtml);
        summaryPanel.add(new HTML(vNumaTitleTemplate.title(totalVNumaNodes, mySafeImageHtml,
                style.hostSummaryNumaTitle())));
        return summaryPanel;
    }

    @Override
    public IsWidget getHostSummaryContent(VDS selectedItem, NumaSupportModel supportModel) {
        HostSummaryContentPanel contentPanel = hostSummaryContentProvider.get();

        List<VM> vmsWithVNuma = supportModel.getVmsWithvNumaNodeList();
        List<VNodeModel> numaNodes = new ArrayList<>();
        for (VM vm: vmsWithVNuma) {
            for(VmNumaNode vmNumaNode: vm.getvNumaNodeList()) {
                numaNodes.add(new VNodeModel(vm, vmNumaNode, false));
            }
        }
        contentPanel.setModel(supportModel.getNumaNodeList(), numaNodes);
        return contentPanel;
    }

    @Override
    public void setHostSummaryPanel(IsWidget widget) {
        this.hostSummaryPanel.add(widget);
    }

    @Override
    public void addVNumaInfoPanel(Set<VdsNumaNode> numaNodes, int socketIndex, NumaSupportModel supportModel) {
        SocketPanel socketPanel = socketPanelProvider.get();
        socketPanel.setHeaderText(messages.numaSocketNumber(socketIndex));
        for (VdsNumaNode numaNode: numaNodes) {
            NumaPanel numaPanel = numaPanelProvider.get();
            numaPanel.getCpuSummaryPanel().setName(messages.numaNode(numaNode.getIndex()));
            numaPanel.getCpuSummaryPanel().setCpus(numaNode.getCpuIds().size(),
                    numaNode.getNumaNodeStatistics().getCpuUsagePercent());
            numaPanel.getMemorySummaryPanel().setMemoryStats(numaNode.getMemTotal(),
                    numaNode.getMemTotal() - numaNode.getNumaNodeStatistics().getMemFree());
            socketPanel.addWidget(numaPanel);
            AssignedVNumaNodesPanel assignedPanel = assignedVNumaPanelProvider.get();
            assignedPanel.setNodes(supportModel.getVNumaNodeByNodeIndx(numaNode.getIndex()), numaNode.getIndex(),
                    supportModel.getNumaNodeList());
            socketPanel.addWidget(assignedPanel);
        }
        this.socketListPanel.add(socketPanel);
    }

    @Override
    public void displayVmDetails(VNodeModel vNodeModel) {
    }

    @Override
    public void clear() {
        this.socketListPanel.clear();
        this.hostSummaryPanel.clear();
    }
}
