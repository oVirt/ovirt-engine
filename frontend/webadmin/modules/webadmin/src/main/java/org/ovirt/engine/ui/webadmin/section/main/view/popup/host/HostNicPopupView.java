package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.view.popup.AbstractTabbedModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTab;
import org.ovirt.engine.ui.common.widget.dialog.tab.DialogTabPanel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostNicModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostNicPopupPresenterWidget;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.Inject;

public class HostNicPopupView extends AbstractTabbedModelBoundPopupView<HostNicModel> implements HostNicPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<HostNicModel, HostNicPopupView> {
    }

    private Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, HostNicPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    SimplePanel mainPanel;

    @UiField
    SimplePanel contentPanel;

    @UiField
    DialogTabPanel tabPanel;

    @UiField
    protected DialogTab pfTab;

    @UiField
    protected DialogTab vfsConfigTab;

    @UiField
    @Ignore
    NicLabelWidget labelsWidget;

    @UiField
    @Ignore
    VfsConfigWidget vfsConfigWidget;

    private final static ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostNicPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    private void localize() {
        pfTab.setLabel(constants.pfTab());
        vfsConfigTab.setLabel(constants.vfsConfigTab());
    }

    @Override
    public void edit(HostNicModel model) {
        driver.edit(model);
        labelsWidget.edit(model.getLabelsModel());
        vfsConfigWidget.edit(model.getVfsConfigModel());
    }

    @Override
    public HostNicModel flush() {
        HostNicModel hostNicModel = driver.flush();
        labelsWidget.flush();
        vfsConfigWidget.flush();
        return hostNicModel;
    }

    @Override
    public void focusInput() {
        super.focusInput();
        labelsWidget.focusInput();
    }

    @Override
    public DialogTabPanel getTabPanel() {
        return tabPanel;
    }

    @Override
    protected void populateTabMap() {
        getTabNameMapping().put(TabName.PF_TAB, pfTab);
        getTabNameMapping().put(TabName.VFS_CONFIG_TAB, vfsConfigTab);
    }

    @Override
    public void showTabs() {
        // Do nothing- it is the regular config
    }

    @Override
    public void showOnlyPf() {
        tabPanel.setVisible(false);
        contentPanel.setWidget(pfTab.getContent());
        mainPanel.setWidth("400px"); //$NON-NLS-1$
        mainPanel.setHeight("215px"); //$NON-NLS-1$
    }

    @Override
    public void showOnlyVfsConfig() {
        tabPanel.setVisible(false);
        contentPanel.setWidget(vfsConfigTab.getContent());
        mainPanel.setWidth("400px"); //$NON-NLS-1$
    }
}
