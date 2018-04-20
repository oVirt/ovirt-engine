package org.ovirt.engine.ui.common.widget.table.column;

import java.util.Comparator;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.table.cell.AbstractToggleButtonCell;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.safecss.shared.SafeStylesUtils;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public abstract class AbstractLunActionsColumn extends AbstractColumn<LunModel, LunModel> {

    private static final UIConstants uiConstants = ConstantsManager.getInstance().getConstants();
    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public AbstractLunActionsColumn() {
        super(new AbstractToggleButtonCell<LunModel>() {
            @Override
            public void onClickEvent(LunModel lunModel) {
                if (lunModel != null && !lunModel.getIsGrayedOut()) {
                    lunModel.setIsSelected(!lunModel.getIsSelected());
                } else if (lunModel != null && lunModel.getAdditionalAvailableSize() != 0) {
                    lunModel.setAdditionalAvailableSizeSelected(!lunModel.isAdditionalAvailableSizeSelected());
                }
            }

            @Override
            public void render(Context context, LunModel value, SafeHtmlBuilder sb, String id) {
                int availableSizeToAdd = value.getAdditionalAvailableSize();
                String availableSizeToAddString =
                        messages.additionalAvailableSizeInGB(availableSizeToAdd);

                boolean isGrayedOut = value.getIsGrayedOut();
                String inputId = id + "_input"; //$NON-NLS-1$
                SafeHtml input;

                if (!isGrayedOut && !value.getIsSelected()) {
                    input = templates.toggledUp(inputId, constants.addSanStorage());
                } else if (!isGrayedOut) {
                    input = templates.toggledDown(inputId, constants.addSanStorage());
                } else if (!value.getIsIncluded()) {
                    input = templates.noButton(uiConstants.notAvailableLabel(), SafeStylesUtils.forTrustedColor("gray"), inputId); //$NON-NLS-1$
                } else if (availableSizeToAdd == 0){
                    input = templates.noButton(constants.cannotExtendSanStorage(), SafeStylesUtils.forTrustedColor("gray"), inputId); //$NON-NLS-1$
                } else if (value.isAdditionalAvailableSizeSelected()) {
                    input = templates.toggledDown(inputId, availableSizeToAddString);
                } else {
                    input = templates.toggledUp(inputId, availableSizeToAddString);
                }

                sb.append(templates.span(id, input));
            }
        });
    }

    public void makeSortable() {
        makeSortable(Comparator.comparingInt(LunModel::getAdditionalAvailableSize));
    }

}
