package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;

public class ShowErrorAsyncQuery extends AsyncQuery {

    public ShowErrorAsyncQuery(final INewAsyncCallback onRealSuccessCallback) {
        super(new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                final VdcQueryReturnValue queryReturnValue = (VdcQueryReturnValue) returnValue;
                if (!queryReturnValue.getSucceeded()) {
                    final ErrorPopupManager popupManager =
                            (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
                    popupManager.show(queryReturnValue.getExceptionMessage());
                    return;
                }
                onRealSuccessCallback.onSuccess(model, returnValue);
            }
        });
        setHandleFailure(true);
    }
}
