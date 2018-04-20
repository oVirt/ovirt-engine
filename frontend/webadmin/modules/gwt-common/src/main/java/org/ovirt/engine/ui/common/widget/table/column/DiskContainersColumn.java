package org.ovirt.engine.ui.common.widget.table.column;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public class DiskContainersColumn extends AbstractTextColumn<Disk> implements ColumnWithElementId {

    private CommonApplicationMessages applicationMessages = AssetProvider.getMessages();

    @Override
    public String getValue(Disk object) {

        if (object.getNumberOfVms() == 0) {
            return ""; //$NON-NLS-1$
        }

        String entityType = EnumTranslator.getInstance().translate(object.getVmEntityType());

        if (object.getNumberOfVms() == 1) {
            String entityName = object.getVmNames().get(0);

            if (object.getVmEntityType() == VmEntityType.TEMPLATE) {
                List<String> templateNames = object.getTemplateVersionNames();

                String versionName = (templateNames != null && !templateNames.isEmpty()) ?
                        applicationMessages.templateVersionName(templateNames.get(0)) : ""; //$NON-NLS-1$
                entityName += versionName;
            }

            return entityName;
        } else {
            return object.getNumberOfVms() + " " + entityType + "s"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    @Override
    public SafeHtml getTooltip(Disk object) {
        if (object.getNumberOfVms() < 2) {
            return null;
        }
        return SafeHtmlUtils.fromString(String.join(", ", object.getVmNames())); //$NON-NLS-1$
    }

}
