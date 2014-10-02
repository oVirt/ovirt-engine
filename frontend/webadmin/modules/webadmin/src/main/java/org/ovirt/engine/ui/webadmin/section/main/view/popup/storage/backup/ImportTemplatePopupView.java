package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.backup;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelObjectCellTable;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.ImageResourceColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportTemplateData;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportVmFromExportDomainModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.backup.ImportTemplatePopupPresenterWidget;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;

public class ImportTemplatePopupView extends ImportVmFromExportDomainPopupView implements ImportTemplatePopupPresenterWidget.ViewDef {

    private ImportTemplateGeneralSubTabView generalView;

    @Inject
    public ImportTemplatePopupView(EventBus eventBus,
            ApplicationResources resources,
            ApplicationConstants constants) {
        super(eventBus, resources, constants);
    }

    @Override
    protected void initMainTable() {
        this.table = new ListModelObjectCellTable<Object, ImportVmFromExportDomainModel>();

        CheckboxColumn<Object> cloneTemplateColumn = new CheckboxColumn<Object>(new FieldUpdater<Object, Boolean>() {
            @Override
            public void update(int index, Object model, Boolean value) {
                ((ImportTemplateData) model).getClone().setEntity(value);
                table.asEditor().edit(importModel);
            }
        }) {
            @Override
            public Boolean getValue(Object model) {
                return (Boolean) ((ImportTemplateData) model).getClone().getEntity();
            }

            @Override
            protected boolean canEdit(Object model) {
                return ((ImportTemplateData) model).getClone().getIsChangable();
            }
        };
        table.addColumn(cloneTemplateColumn, constants.cloneVM(), "50px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> nameColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return ((ImportTemplateData) object).getTemplate().getName();
            }
        };
        table.addColumn(nameColumn, constants.nameTemplate(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> versionNameColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                VmTemplate template = ((ImportTemplateData) object).getTemplate();
                if (template.isBaseTemplate()) {
                    return ""; //$NON-NLS-1$
                }

                return StringFormat.format("%s (%s)", //$NON-NLS-1$
                        template.getTemplateVersionName() != null ? template.getTemplateVersionName() : "", //$NON-NLS-1$
                        template.getTemplateVersionNumber());
            }
        };
        table.addColumn(versionNameColumn, constants.versionTemplate(), "150px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> originColumn = new EnumColumn<Object, OriginType>() {
            @Override
            protected OriginType getRawValue(Object object) {
                return ((ImportTemplateData) object).getTemplate().getOrigin();
            }
        };
        table.addColumn(originColumn, constants.originTemplate(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> memoryColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportTemplateData) object).getTemplate().getMemSizeMb()) + " MB"; //$NON-NLS-1$
            }
        };
        table.addColumn(memoryColumn, constants.memoryTemplate(), "100px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> cpuColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportTemplateData) object).getTemplate().getNumOfCpus());
            }
        };
        table.addColumn(cpuColumn, constants.cpusTemplate(), "50px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> archColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportTemplateData) object).getTemplate().getClusterArch());
            }
        };
        table.addColumn(archColumn, constants.architectureTemplate(), "50px"); //$NON-NLS-1$

        TextColumnWithTooltip<Object> diskColumn = new TextColumnWithTooltip<Object>() {
            @Override
            public String getValue(Object object) {
                return String.valueOf(((ImportTemplateData) object).getTemplate().getDiskList().size());
            }
        };
        table.addColumn(diskColumn, constants.disksTemplate(), "50px"); //$NON-NLS-1$

        isObjectInSystemColumn = new ImageResourceColumn<Object>() {
            @Override
            public ImageResource getValue(Object object) {
                return ((ImportTemplateData) object).isExistsInSystem() ? getCommonResources().logNormalImage() : null;
            }
        };
        table.addColumn(isObjectInSystemColumn, constants.templateInSetup(), "60px"); //$NON-NLS-1$

        ScrollPanel sp = new ScrollPanel();
        sp.add(table);
        splitLayoutPanel.add(sp);
        table.getElement().getStyle().setPosition(Position.RELATIVE);
    }

    @Override
    protected void initGeneralSubTabView() {
        ScrollPanel generalPanel = new ScrollPanel();
        DetailModelProvider<TemplateListModel, TemplateGeneralModel> modelProvider =
                new DetailModelProvider<TemplateListModel, TemplateGeneralModel>() {
                    @Override
                    public TemplateGeneralModel getModel() {
                        return (TemplateGeneralModel) importModel.getDetailModels().get(0);
                    }

                    @Override
                    public void onSubTabSelected() {
                    }

                    @Override
                    public void onSubTabDeselected() {
                    }
                };
        generalView = new ImportTemplateGeneralSubTabView(modelProvider, constants);
        generalPanel.add(generalView);
        subTabLayoutPanel.add(generalPanel, constants.generalImpTempTab());
    }

    @Override
    protected void setGeneralViewSelection(Object selectedItem) {
        generalView.setMainTabSelectedItem((VmTemplate) selectedItem);
    }

    @Override
    protected void addAllocationColumn() {
    }

    @Override
    protected void initAppTable() {
    }

}
