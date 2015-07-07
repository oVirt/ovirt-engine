package org.ovirt.engine.ui.userportal.section.main.presenter;

import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Event triggered when a UiCommon {@link SearchableListModel} needs to be activated (and any other list models need to
 * be stopped).
 */
@GenEvent
public class SearchableModelActivation {

    SearchableListModel listModel;

}
