package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.ReportInit;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ReportParser.Category;
import org.ovirt.engine.ui.uicompat.ReportParser.Resource;
import org.ovirt.engine.ui.uicompat.ReportParser.URI;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class ReportActionsHelper {

    private static final ReportActionsHelper INSTANCE = new ReportActionsHelper();

    public static ReportActionsHelper getInstance() {
        return INSTANCE;
    }

    private ReportActionsHelper() {
    }

    public <T> List<ActionButtonDefinition<T>> getResourceSubActions(String resourceType,
            MainModelProvider<?, ? extends SearchableListModel> modelProvider) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<>();

        Resource resource = ReportInit.getInstance().getResource(resourceType);
        if (resource != null) {
            for (Category category : resource.getCatergoriesList()) {
                List<ActionButtonDefinition<T>> categerySubActions = getCategorySubActions(category, modelProvider);
                subActions.add(new WebAdminMenuBarButtonDefinition<T>(category.getName(), categerySubActions, true) {
                    @Override
                    public boolean isVisible(List<T> selectedItems) {
                        boolean isVisible = false;

                        for (ActionButtonDefinition<T> subAction : getSubActions()) {
                            if (subAction.isVisible(selectedItems)) {
                                return true;
                            }
                        }

                        return isVisible;
                    }
                });
            }
        }

        return subActions;
    }

    public <T> List<ActionButtonDefinition<T>> getCategorySubActions(final Category category,
            final MainModelProvider<?, ? extends SearchableListModel> modelProvider) {
        List<ActionButtonDefinition<T>> subActions = new LinkedList<>();

        for (final URI uri : category.getUriList()) {
            subActions.add(new WebAdminButtonDefinition<T>(uri.getName(), null, true) {

                @Override
                public boolean isVisible(List<T> selectedItems) {
                    return isEnabled(selectedItems);
                }

                @Override
                protected UICommand resolveCommand() {
                    return modelProvider.getModel().addOpenReportCommand(uri.getId(), uri.isMultiple(), uri.getValue());
                }

                @Override
                public SafeHtml getTooltip() {
                    return SafeHtmlUtils.fromString(uri.getDescription());
                }

            });
        }

        return subActions;
    }

}
