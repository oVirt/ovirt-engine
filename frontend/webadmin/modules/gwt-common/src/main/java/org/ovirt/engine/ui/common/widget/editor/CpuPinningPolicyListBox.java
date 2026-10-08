package org.ovirt.engine.ui.common.widget.editor;

import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.vms.CpuPinningListModel.CpuPinningListModelItem;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.safecss.shared.SafeStylesBuilder;

public class CpuPinningPolicyListBox extends ListModelTypeAheadListBox<CpuPinningListModelItem> {

    public CpuPinningPolicyListBox() {
        super(new CpuPinningPolicyListBoxRenderer(),
                true,
                new SuggestionMatcher.StartWithSuggestionMatcher());
    }

    @Override
    public void render(CpuPinningListModelItem value, boolean fireEvents) {
        super.render(value, fireEvents);
    }

    @Override
    protected void grayOutPlaceholderText(boolean isPlaceholder) {
        super.grayOutPlaceholderText(isPlaceholder);
        if (getValue() != null && !getValue().isEnabled()) {
            asSuggestBox().getElement().getStyle().setColor("red"); //$NON-NLS-1$
        }
    }

    private static class CpuPinningPolicyListBoxRenderer extends ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<CpuPinningListModelItem> {

        // color used in the template for the item's description
        private static final String GRAY = "#acacac"; //$NON-NLS-1$

        private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

        private EnumRenderer<CpuPinningPolicy> renderer = new EnumRenderer<>();

        @Override
        public String getReplacementStringNullSafe(CpuPinningListModelItem pinning) {
            return renderer.render(pinning.getPolicy());
        }

        @Override
        public String getDisplayStringNullSafe(CpuPinningListModelItem pinningType) {
            String description = pinningType.isEnabled() ? pinningType.getDescription()
                    : pinningType.getDisablementReason();
            String color = pinningType.isEnabled() ? null : GRAY;

            return typeAheadNameDescriptionTemplateNullSafeWithColor(
                    renderer.render(pinningType.getPolicy()),
                    description,
                    color
            );
        }

        private String typeAheadNameDescriptionTemplateNullSafeWithColor(String name, String description, String color) {
            SafeStylesBuilder builder = new SafeStylesBuilder();
            builder.whiteSpace(WhiteSpace.NOWRAP);
            builder.overflow(Overflow.HIDDEN);
            builder.textOverflow(TextOverflow.ELLIPSIS);
            builder.trustedNameAndValue("max-width", 49, Unit.PCT); //$NON-NLS-1$
            builder.trustedNameAndValue("float", "left"); //$NON-NLS-1$ //$NON-NLS-2$
            builder.trustedColor(color != null ? color : ""); //$NON-NLS-1$
            return templates.typeAheadNameDescriptionWithColor(
                    name != null ? name : "",
                    description != null ? description : "",
                    builder.toSafeStyles())
                    .asString();
        }
    }
}
