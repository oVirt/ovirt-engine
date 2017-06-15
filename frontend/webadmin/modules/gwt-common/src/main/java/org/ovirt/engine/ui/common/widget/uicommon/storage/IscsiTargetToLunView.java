package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class IscsiTargetToLunView extends Composite implements HasEditorDriver<SanStorageModelBase> {

    interface Driver extends UiCommonEditorDriver<SanStorageModelBase, IscsiTargetToLunView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, IscsiTargetToLunView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel targetsListPanel;

    @UiField
    SimplePanel targetsToLunsDiscoverPanel;

    @Ignore
    IscsiDiscoverTargetsView iscsiDiscoverTargetsView;

    SanStorageTargetToLunList sanStorageTargetToLunList;

    private double treeCollapsedHeight;
    private double treeExpandedHeight;
    private boolean hideLeaf;
    private boolean multiSelection;

    private final Driver driver = GWT.create(Driver.class);

    public IscsiTargetToLunView(double treeCollapsedHeight, double treeExpandedHeight) {
        this(treeCollapsedHeight, treeExpandedHeight, false, false);
    }

    public IscsiTargetToLunView(double treeCollapsedHeight, double treeExpandedHeight,
            boolean hideLeaf, boolean multiSelection) {
        this.treeCollapsedHeight = treeCollapsedHeight;
        this.treeExpandedHeight = treeExpandedHeight;
        this.hideLeaf = hideLeaf;
        this.multiSelection = multiSelection;

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(final SanStorageModelBase object) {
        driver.edit(object);

        initLists(object);

        // Set DiscoverTargetsCommand as default (for iscsiDiscoverTargetsView)
        object.getDiscoverTargetsCommand().setIsDefault(true);
        object.getCommands().add(object.getDiscoverTargetsCommand());

        // Add event handlers
        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if (propName.equals("ProposeDiscoverTargets")) { //$NON-NLS-1$
                setProposeDiscover(object.getProposeDiscoverTargets());
            }
        });

        // Edit sub view
        iscsiDiscoverTargetsView.edit(object);

        // Set discover panel's style by 'ProposeDiscover' flag
        setProposeDiscover(object.getProposeDiscoverTargets());
    }

    private void setProposeDiscover(boolean proposeDiscover) {
        // Update discover panel visibility according to propose discover flag
        if (sanStorageTargetToLunList != null) {
            sanStorageTargetToLunList.setTreeContainerHeight(proposeDiscover ? treeCollapsedHeight : treeExpandedHeight);
        }
    }

    void initLists(SanStorageModelBase object) {
        // Create discover panel and storage lists
        iscsiDiscoverTargetsView = new IscsiDiscoverTargetsView();
        sanStorageTargetToLunList = new SanStorageTargetToLunList(object, hideLeaf, multiSelection);

        // Add view widgets to panel
        targetsToLunsDiscoverPanel.add(iscsiDiscoverTargetsView);
        targetsListPanel.add(sanStorageTargetToLunList);

        // Disable Discover widget in view-only mode
        iscsiDiscoverTargetsView.setEnabled(object.getContainer().getIsChangable());
    }

    @Override
    public SanStorageModelBase flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    public void activateItemsUpdate() {
        sanStorageTargetToLunList.activateItemsUpdate();
    }

    public void disableItemsUpdate() {
        sanStorageTargetToLunList.disableItemsUpdate();
    }

    public boolean isDiscoverPanelFocused() {
        return iscsiDiscoverTargetsView.isDiscoverPanelFocused();
    }

}
