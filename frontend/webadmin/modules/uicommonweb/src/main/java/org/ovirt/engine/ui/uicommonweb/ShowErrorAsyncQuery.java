package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;

public class ShowErrorAsyncQuery extends AsyncQuery<QueryReturnValue> {

    public ShowErrorAsyncQuery(final AsyncCallback<QueryReturnValue> onRealSuccessCallback) {
        super(returnValue -> {
            if (!returnValue.getSucceeded()) {
                final ErrorPopupManager popupManager =
                        (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
                popupManager.show(returnValue.getExceptionMessage());
                return;
            }
            onRealSuccessCallback.onSuccess(returnValue);
        });
        setHandleFailure(true);
    }
}
