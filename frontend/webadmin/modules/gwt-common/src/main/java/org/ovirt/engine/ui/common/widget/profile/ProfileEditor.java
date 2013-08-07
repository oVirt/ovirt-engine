package org.ovirt.engine.ui.common.widget.profile;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.widget.editor.ListModelTypeAheadListBoxEditor;

import com.google.gwt.core.client.GWT;

public class ProfileEditor extends ListModelTypeAheadListBoxEditor<Object> {

    public final static CommonApplicationMessages messages = GWT.create(CommonApplicationMessages.class);
    public final static CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);

    public ProfileEditor() {
        super(
                new ListModelTypeAheadListBoxEditor.NullSafeSuggestBoxRenderer<Object>() {

                    @Override
                    public String getReplacementStringNullSafe(Object data) {
                        return messages.profileAndNetworkSelected(((VnicProfileView) data).getName(),
                                ((VnicProfileView) data).getNetworkName()).asString();
                    }

                    @Override
                    public String getDisplayStringNullSafe(Object data) {
                        String profileDescription =
                                ((VnicProfileView) data).getDescription();
                        String profileAndNetwork =
                                messages.profileAndNetwork(((VnicProfileView) data).getName(),
                                        ((VnicProfileView) data).getNetworkName()).asString();

                        return templates.typeAheadNameDescription(profileAndNetwork,
                                profileDescription != null ? profileDescription : "").asString(); //$NON-NLS-1$
                    }

                });
    }

}
