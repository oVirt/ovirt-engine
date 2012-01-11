package org.ovirt.engine.ui.common.uicommon.model;

import org.ovirt.engine.ui.uicommonweb.models.CommonModel;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when a new {@link CommonModel} instance is created by {@link CommonModelManager}.
 */
@GenEvent
public class CommonModelChange {

    CommonModel commonModel;

}
