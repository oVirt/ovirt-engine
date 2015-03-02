package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.configure.UserPortalPermissionListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateStorageListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateVmListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.UserPortalTemplateEventListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.TemplateVmModelBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UserPortalTemplateVmModelBehavior;

import com.google.inject.Inject;
import org.ovirt.engine.ui.uicommonweb.place.UserPortalApplicationPlaces;

public class UserPortalTemplateListModel extends TemplateListModel {

    @Inject
    public UserPortalTemplateListModel(TemplateGeneralModel templateGeneralModel,
            TemplateVmListModel templateVmListModel,
            TemplateInterfaceListModel templateInterfaceListModel,
            TemplateStorageListModel templateStorageListModel,
            UserPortalTemplateDiskListModel templateDiskListModel,
            UserPortalTemplateEventListModel templateEventListModel,
            UserPortalPermissionListModel permissionListModel) {
        super(templateGeneralModel,
                templateVmListModel,
                templateInterfaceListModel,
                templateStorageListModel,
                templateDiskListModel,
                templateEventListModel,
                permissionListModel, 2);
        setApplicationPlace(UserPortalApplicationPlaces.extendedTemplateSideTabPlace);
    }

    @Override
    protected void syncSearch() {
        AsyncDataProvider.getInstance().getAllVmTemplates(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                ((UserPortalTemplateListModel) model).setItems((Collection) returnValue);
            }
        }), getIsQueryFirstTime());
    }

    @Override
    protected void updateActionsAvailability() {
        VmTemplate item = (VmTemplate) getSelectedItem();
        if (item != null) {
            ArrayList items = new ArrayList();
            items.add(item);
            getEditCommand().setIsExecutionAllowed(
                    item.getStatus() != VmTemplateStatus.Locked &&
                            !isBlankTemplateSelected());
            getRemoveCommand().setIsExecutionAllowed(
                    VdcActionUtils.canExecute(items, VmTemplate.class,
                            VdcActionType.RemoveVmTemplate) &&
                            !isBlankTemplateSelected()
                    );
        } else {
            getEditCommand().setIsExecutionAllowed(false);
            getRemoveCommand().setIsExecutionAllowed(false);
        }
    }

    @Override
    protected String getEditTemplateAdvancedModelKey() {
        return "up_template_dialog"; //$NON-NLS-1$
    }

    @Override
    protected TemplateVmModelBehavior createBehavior(VmTemplate template) {
        return new UserPortalTemplateVmModelBehavior(template);
    }

    @Override
    public void setItems(Collection value) {
        final List<VmTemplate> sortedValues = sortTemplates(value);
        super.setItems(sortedValues);
    }

    /**
     * It sorts {@link org.ovirt.engine.core.common.businessentities.VmTemplate}s using
     * {@link org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel.TemplateComparator}
     */
    private List<VmTemplate> sortTemplates(Collection<VmTemplate> value) {
        final List<VmTemplate> sortedValues = new ArrayList<>(value);
        Collections.sort(sortedValues, new TemplateComparator());
        return sortedValues;
    }

    /**
     * Comparator sorting templates
     * <ul>
     *     <li>alphabetically by base-template name case insensitive</li>
     *     <li>alphabetically by base-template name case sensitive</li>
     *     <li>and then by version number - descending</li>
     * </ul>
     */
    private static class TemplateComparator implements Comparator<VmTemplate>, Serializable {

        @Override
        public int compare(VmTemplate t1, VmTemplate t2) {
            final int baseNameCaseInsensitiveComparison = t1.getName().compareToIgnoreCase(t2.getName());
            if (baseNameCaseInsensitiveComparison != 0) {
                return baseNameCaseInsensitiveComparison;
            }
            final int baseNameComparison = t1.getName().compareTo(t2.getName());
            if (baseNameComparison != 0) {
                return baseNameComparison;
            }
            final int versionComparison =
                    - Integer.signum(Integer.compare(t1.getTemplateVersionNumber(), t2.getTemplateVersionNumber()));
            return versionComparison;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && this.getClass().equals(obj.getClass());
        }

        @Override
        public int hashCode() {
            return this.getClass().hashCode();
        }
    }
}
