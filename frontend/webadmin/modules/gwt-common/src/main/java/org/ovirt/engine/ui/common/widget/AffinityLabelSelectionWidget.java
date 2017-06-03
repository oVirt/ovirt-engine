package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.SuggestionMatcher;

public class AffinityLabelSelectionWidget extends AbstractItemSelectionWidget<Label> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    public AffinityLabelSelectionWidget() {
        init();
    }

    @Override
    public void init() {
        super.init();
        filterListLabel.setText(constants.affinityLabelsDropDownInstruction());
    }

    @Override
    protected ListModelTypeAheadListBoxEditor<Label> createFilterListEditor() {
        return new ListModelTypeAheadListBoxEditor<>(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<Label>() {
                    @Override
                    public String getReplacementStringNullSafe(Label item) {
                        return item.getName();
                    }

                    @Override
                    public String getDisplayStringNullSafe(Label item) {
                        return typeAheadNameTemplateNullSafe(item.getName());
                    }
                },
                new VisibilityRenderer.SimpleVisibilityRenderer(),
                new SuggestionMatcher.ContainsSuggestionMatcher());
    }
}
