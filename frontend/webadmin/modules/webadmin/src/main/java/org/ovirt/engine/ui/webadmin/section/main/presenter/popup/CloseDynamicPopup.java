package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when {@link DynamicUrlContentPopupPresenterWidget} should be closed and disposed.
 */
@GenEvent
public class CloseDynamicPopup {

    String dialogToken;

}
