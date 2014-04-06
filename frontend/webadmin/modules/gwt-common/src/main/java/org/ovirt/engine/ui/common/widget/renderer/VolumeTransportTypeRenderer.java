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
        Translator transportTypeTranslator = EnumTranslator.create(TransportType.class);
        StringBuilder transportTypesBuilder = new StringBuilder();
        Iterator<TransportType> iterator = transportTypes.iterator();
        while (iterator.hasNext()) {
            TransportType transportType = iterator.next();
            if (transportTypeTranslator.containsKey(transportType)) {
                transportTypesBuilder.append(transportTypeTranslator.get(transportType));
            }
            else {
                transportTypesBuilder.append(transportType.toString());
            }

            if (iterator.hasNext()) {
                transportTypesBuilder.append(", "); //$NON-NLS-1$
            }
        }
        return transportTypesBuilder.toString();
    }

}
