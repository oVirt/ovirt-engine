package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.ui.common.presenter.AbstractMainTabSelectedItems;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.UserSelectionChangeEvent;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

public class UserMainTabSelectedItems extends AbstractMainTabSelectedItems<DbUser>
    implements UserSelectionChangeEvent.UserSelectionChangeHandler {

    @Inject
    UserMainTabSelectedItems(EventBus eventBus) {
        //This is singleton, so won't leak handlers.
        eventBus.addHandler(UserSelectionChangeEvent.getType(), this);
    }

    @Override
    public void onUserSelectionChange(UserSelectionChangeEvent event) {
        selectedItemsChanged(event.getSelectedItems());
    }
}
