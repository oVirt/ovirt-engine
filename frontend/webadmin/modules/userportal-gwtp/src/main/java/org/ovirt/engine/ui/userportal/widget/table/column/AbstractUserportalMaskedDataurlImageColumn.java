package org.ovirt.engine.ui.userportal.widget.table.column;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.widget.table.column.AbstractDataurlImageColumn;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

public abstract class AbstractUserportalMaskedDataurlImageColumn extends AbstractDataurlImageColumn<UserPortalItemModel> {

    private ImageResource mask;

    public AbstractUserportalMaskedDataurlImageColumn(ImageResource mask) {
        this.mask = mask;
    }

    @Override
    public SafeHtml getTooltip(UserPortalItemModel itemModel) {
        String osName = AsyncDataProvider.getInstance().getOsName(itemModel.getOsId());
        return SafeHtmlUtils.fromString(osName);
    }

    @Override
    public String getValue(UserPortalItemModel itemModel) {
        return IconCache.getInstance().getIcon(getIconId(itemModel));
    }

    public abstract Guid getIconId(UserPortalItemModel itemModel);

    @Override
    public void render(Cell.Context context, UserPortalItemModel itemModel, SafeHtmlBuilder sb) {
        if (!itemModel.isVmUp()) {
            AbstractMaskedVmImageColumn.renderMask(mask, sb);
        }
        super.render(context, itemModel, sb);
    }
}
