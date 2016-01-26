package org.ovirt.engine.ui.userportal.section.main.view.tab.extended;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.widget.table.cell.TextCell;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDataurlImageColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.SideTabWithDetailsViewStyle;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AbstractSideTabWithDetailsView;
import org.ovirt.engine.ui.userportal.uicommon.model.template.UserPortalTemplateListProvider;
import org.ovirt.engine.ui.userportal.widget.action.UserPortalButtonDefinition;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.inject.Inject;

public class SideTabExtendedTemplateView extends AbstractSideTabWithDetailsView<VmTemplate, UserPortalTemplateListModel>
        implements SideTabExtendedTemplatePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SideTabExtendedTemplateView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    public interface TemplateTableResources extends CellTable.Resources {
        interface TableStyle extends CellTable.Style {
        }

        @Override
        @Source({ CellTable.Style.DEFAULT_CSS, "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTable.css" })
        TableStyle cellTableStyle();
    }

    public interface TemplateTableHeaderResources extends CellTable.Resources {

        @Source({ CellTable.Style.DEFAULT_CSS,
                "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTable.css",
                "org/ovirt/engine/ui/userportal/css/ExtendedTemplateListTableHeader.css"})
        @Override
        CellTable.Style cellTableStyle();
    }

    public interface TemplateSideTabWithDetailsViewStyle extends ClientBundle {

        @Source({"org/ovirt/engine/ui/userportal/css/SideTabWithDetailsViewStyle.css",
                 "org/ovirt/engine/ui/userportal/css/SideTabExtendedTemplateViewStyle.css"})
        SideTabWithDetailsViewStyle templateSideTab();
    }

    private static final TemplateTableResources templateTableResources = GWT.create(TemplateTableResources.class);

    private static final TemplateTableHeaderResources TEMPLATE_TABLE_HEADER_RESOURCES =
            GWT.create(TemplateTableHeaderResources.class);

    private static final TemplateSideTabWithDetailsViewStyle TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE =
            GWT.create(TemplateSideTabWithDetailsViewStyle.class);
    static {
        TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE.templateSideTab().ensureInjected();
    }

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationResources resources = AssetProvider.getResources();
    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SideTabExtendedTemplateView(
            UserPortalTemplateListProvider provider,
            ClientStorage clientStorage) {
        super(provider, clientStorage);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
    }

    @Override
    protected Resources getTableResources() {
        return templateTableResources;
    }

    @Override
    protected Resources getTableHeaderResources() {
        return TEMPLATE_TABLE_HEADER_RESOURCES;
    }

    @Override
    protected String getTableContainerStyleName() {
        return TEMPLATE_SIDE_TAB_WITH_DETAILS_VIEW_STYLE.templateSideTab().mainContentPanel();
    }

    @Override
    protected Object getSubTabPanelContentSlot() {
        return SideTabExtendedTemplatePresenter.TYPE_SetSubTabPanelContent;
    }

    private void initTable() {
        getTable().enableColumnResizing();
        final String elementIdPrefix = getTable().getContentTableElementId();

        getTable().addColumn(new AbstractDataurlImageColumn<VmTemplate>() {
            @Override public String getValue(VmTemplate template) {
                final Guid smallIconId = template.getSmallIconId();
                return IconCache.getInstance().getIcon(smallIconId);
            }
        }, "", "77px"); //$NON-NLS-1$ //$NON-NLS-2$

        Cell<VmTemplate> nameCell = new AbstractCell<VmTemplate>() {
            @Override
            public void render(Context context, VmTemplate template, SafeHtmlBuilder sb) {
                sb.append(templates.vmNameCellItem(
                        ElementIdUtils.createTableCellElementId(elementIdPrefix, "name", context), //$NON-NLS-1$
                        template.getName()));
            }
        };

        Column<VmTemplate, VmTemplate> nameColumn = new Column<VmTemplate, VmTemplate>(nameCell) {
            @Override
            public VmTemplate getValue(VmTemplate template) {
                return template;
            }
        };
        getTable().addColumn(nameColumn, constants.templateName(), "350px"); //$NON-NLS-1$

        final TextCell subversionNumberCell = new TextCell();

        final AbstractTextColumn<VmTemplate> subversionNumberColumn = new AbstractTextColumn<VmTemplate>(subversionNumberCell) {

            @Override
            public String getValue(VmTemplate template) {
                return "(" + template.getTemplateVersionNumber() + ")"; //$NON-NLS-1$ //$NON-NLS-2$;
            }
        };
        table.addColumn(subversionNumberColumn, constants.templateVersion(), "140px"); //$NON-NLS-1$

        final TextCell subversionNameCell = new TextCell();

        final AbstractTextColumn<VmTemplate> subversionNameColumn = new AbstractTextColumn<VmTemplate>(subversionNameCell) {

            @Override
            public String getValue(VmTemplate template) {
                return template.getTemplateVersionName() != null && !template.getTemplateVersionName().isEmpty()
                        ? template.getTemplateVersionName()
                        : ""; //$NON-NLS-1$
            }
        };
        table.addColumn(subversionNameColumn, constants.templateVersionName(), "350px"); //$NON-NLS-1$

        final TextCell descriptionCell = new TextCell();

        final AbstractTextColumn<VmTemplate> descriptionColumn = new AbstractTextColumn<VmTemplate>(descriptionCell) {

            @Override
            public String getValue(VmTemplate template) {
                return template.getDescription() != null && !template.getDescription().isEmpty()
                        ? template.getDescription()
                        : ""; //$NON-NLS-1$
            }
        };
        table.addColumn(descriptionColumn, constants.templateDescription());

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>(constants.editTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UserPortalButtonDefinition<VmTemplate>(constants.removeTemplate()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }



}
