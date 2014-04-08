package org.ovirt.engine.ui.common.widget.renderer;

import java.util.Iterator;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

import com.google.gwt.text.shared.AbstractRenderer;

public class VolumeTransportTypeRenderer extends AbstractRenderer<Set<TransportType>> {

    public VolumeTransportTypeRenderer() {

    }

    @Override
    public String render(Set<TransportType> transportTypes) {
        Translator transportTypeTranslator = EnumTranslator.getInstance();
        StringBuilder transportTypesBuilder = new StringBuilder();
        Iterator<TransportType> iterator = transportTypes.iterator();
        while (iterator.hasNext()) {
            TransportType transportType = iterator.next();

            transportTypesBuilder.append(transportTypeTranslator.translate(transportType));

            if (iterator.hasNext()) {
                transportTypesBuilder.append(", "); //$NON-NLS-1$
            }
        }
        return transportTypesBuilder.toString();
    }

}
