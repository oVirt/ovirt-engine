package org.ovirt.engine.ui.common.widget.tab;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event generated when a tab accessibility changes.
 */
@GenEvent
public class TabAccessibleChange {
    ModelBoundTab tab;
}
