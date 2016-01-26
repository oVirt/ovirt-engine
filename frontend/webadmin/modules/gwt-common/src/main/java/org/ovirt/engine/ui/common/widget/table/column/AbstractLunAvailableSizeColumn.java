package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractToggleButtonCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;

public abstract class AbstractLunAvailableSizeColumn extends Column<LunModel, LunModel> {

    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public AbstractLunAvailableSizeColumn() {
        super(new AbstractToggleButtonCell<LunModel>() {
            @Override
            public void onClickEvent(LunModel lunModel) {
                if (lunModel !=null) {
                    lunModel.setAdditionalAvailableSizeSelected(!lunModel.isAdditionalAvailableSizeSelected());
                }
            }
            @Override
            public void render(Context context, LunModel value, SafeHtmlBuilder sb, String id) {
                boolean isGrayedOut = value.getIsGrayedOut();
                String inputId = id + "_input"; //$NON-NLS-1$
                SafeHtml input = null;

                int additionalAvailableSizeSize = value.getAdditionalAvailableSize();
                String additionalAvailableSizeSizeString =
                        messages.additionalAvailableSizeInGB(additionalAvailableSizeSize);

                if (additionalAvailableSizeSize == 0 || !value.getIsIncluded()) {
                    input = templates.disabled("", "color:gray", inputId); //$NON-NLS-1$
                }
                else if (!isGrayedOut) {
                    input = templates.disabled("", "color:black", inputId); //$NON-NLS-1$
                }
                else if (value.isAdditionalAvailableSizeSelected()) {
                    input = templates.toggledDown(inputId, additionalAvailableSizeSizeString);
                }
                else {
                    input = templates.toggledUp(inputId, additionalAvailableSizeSizeString);
                }

                sb.append(templates.span(id, input));

            }
        });
    }
}
