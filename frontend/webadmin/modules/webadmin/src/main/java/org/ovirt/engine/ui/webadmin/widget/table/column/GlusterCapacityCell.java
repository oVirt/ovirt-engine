package org.ovirt.engine.ui.webadmin.widget.table.column;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractCell;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class GlusterCapacityCell<P extends Serializable> extends AbstractCell<P>{

    protected ApplicationTemplates templates = AssetProvider.getTemplates();
    protected ApplicationMessages messages = AssetProvider.getMessages();
    protected ApplicationConstants constants = AssetProvider.getConstants();

    private Double freeSize;
    private Double totalSize;
    private Double usedSize;
    private SizeUnit inUnit;

    protected String getSizeString(Double size, SizeUnit inUnit) {
        if(size == null) {
            return constants.notAvailableLabel();
        } else {
            Pair<SizeUnit, Double> sizeWithUnits = SizeConverter.autoConvert(size.longValue(), inUnit);
            return ConstantsManager.getInstance().getMessages().sizeUnitString(formatSize(sizeWithUnits.getSecond()), sizeWithUnits.getFirst());//$NON-NLS-1$
        }
    }

    private String formatSize(double size) {
        return NumberFormat.getFormat("#.##").format(size);//$NON-NLS-1$
    }

    protected String getProgressText(Double freeSize, Double totalSize) {
        if(freeSize == null || totalSize == null) {
            return "?";//$NON-NLS-1$
        } else {
            return (int) getPercentageUsage(freeSize, totalSize) + "%";//$NON-NLS-1$
        }
    }

    protected int getProgressValue(Double freeSize, Double totalSize) {
        if(freeSize == null || totalSize == null) {
            return 0;
        }
        return (int) Math.round(getPercentageUsage(freeSize, totalSize));
    }

    private double getPercentageUsage(Double freeSize, Double totalSize) {
        return ((totalSize - freeSize)  * 100 )/totalSize;
    }

    protected void setFreeSize(Double freeSize) {
        this.freeSize = freeSize;
    }

    protected void setTotalSize(Double totalSize) {
        this.totalSize = totalSize;
    }

    protected void setInUnit(SizeUnit inUnit) {
        this.inUnit = inUnit;
    }

    protected void setUsedSize(Double usedSize) {
        this.usedSize = usedSize;
    }

    public void clearAll() {
        setFreeSize(null);
        setTotalSize(null);
        setUsedSize(null);
        setInUnit(null);
    }

    @Override
    public void render(Context context, Serializable value, SafeHtmlBuilder sb, String id) {
        if(value == null) {
            clearAll();
        }
        int progress = getProgressValue(freeSize, totalSize);
        String sizeString = getProgressText(freeSize, totalSize);
        String color = progress < 70 ? "#669966" : progress < 95 ? "#FF9900" : "#FF0000"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String toolTip = messages.glusterCapacityInfo(getSizeString(freeSize, inUnit), getSizeString(usedSize, inUnit), getSizeString(totalSize, inUnit));
        SafeHtml safeHtml = templates.glusterCapcityProgressBar(progress, sizeString, color, toolTip, id);
        sb.append(safeHtml);
    }
}
