package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Collection;

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
    protected void updateActionAvailability() {
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
}
