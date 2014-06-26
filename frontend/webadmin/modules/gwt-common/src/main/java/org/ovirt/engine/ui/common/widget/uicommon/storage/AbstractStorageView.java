package org.ovirt.engine.ui.common.widget.uicommon.storage;

import com.google.gwt.core.client.GWT;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.storage.IStorageModel;

import com.google.gwt.user.client.ui.Composite;

public abstract class AbstractStorageView<M extends IStorageModel> extends Composite implements HasEditorDriver<M> {

    protected static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    protected boolean multiSelection;

    public abstract void focus();

    public boolean isSubViewFocused() {
        return false;
    }

}
