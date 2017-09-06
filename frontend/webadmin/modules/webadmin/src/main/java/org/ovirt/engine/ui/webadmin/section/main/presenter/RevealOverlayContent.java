package org.ovirt.engine.ui.webadmin.section.main.presenter;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class RevealOverlayContent {
    AbstractOverlayPresenterWidget<?> content;
}
