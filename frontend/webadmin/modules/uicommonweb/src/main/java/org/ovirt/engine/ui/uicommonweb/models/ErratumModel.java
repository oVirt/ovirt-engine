package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

/**
 * EntityModel that holds an Erratum.
 */
public class ErratumModel extends EntityModel<Erratum> {

    private EntityModel<Erratum> erratum = new EntityModel<>();

    public void addErratumChangeListener(IEventListener<? super EventArgs> listener) {
        erratum.getEntityChangedEvent().addListener(listener);
    }

}
