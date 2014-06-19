package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.api.model.Range;
import org.ovirt.engine.api.model.Ranges;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;

public class MacPoolMapper {
    @Mapping(from = org.ovirt.engine.api.model.MacPool.class, to = MacPool.class)
    public static MacPool map(org.ovirt.engine.api.model.MacPool model, MacPool template) {
        if (model == null) {
            return template;
        }

        MacPool entity = template == null ? new MacPool() : template;

        if (model.isSetId()) {
            entity.setId(GuidUtils.asGuid(model.getId()));
        }

        if (model.isSetName()) {
            entity.setName(model.getName());
        }

        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }

        if (model.isSetAllowDuplicates()) {
            entity.setAllowDuplicateMacAddresses(model.isAllowDuplicates());
        }

        if (model.isSetDefaultPool()) {
            entity.setDefaultPool(model.isDefaultPool());
        }

        if (model.isSetRanges()) {
            mapRanges(model, entity);
        }

        return entity;
    }

    private static void mapRanges(org.ovirt.engine.api.model.MacPool model, MacPool entity) {
        final List<MacRange> ranges = new ArrayList<>();
        for (org.ovirt.engine.api.model.Range range : model.getRanges().getRanges()) {
            ranges.add(mapRange(range));
        }

        entity.setRanges(ranges);
    }

    private static MacRange mapRange(org.ovirt.engine.api.model.Range range) {
        final MacRange result = new MacRange();
        result.setMacFrom(range.getFrom());
        result.setMacTo(range.getTo());
        return result;
    }

    @Mapping(from = MacPool.class, to = org.ovirt.engine.api.model.MacPool.class)
    public static org.ovirt.engine.api.model.MacPool map(MacPool entity, org.ovirt.engine.api.model.MacPool template) {
        if (entity == null) {
            return template;
        }

        org.ovirt.engine.api.model.MacPool model = template == null ?
                new org.ovirt.engine.api.model.MacPool() :
                template;

        model.setId(entity.getId().toString());
        model.setName(entity.getName());
        model.setAllowDuplicates(entity.isAllowDuplicateMacAddresses());
        model.setDescription(entity.getDescription());
        model.setDefaultPool(entity.isDefaultPool());
        mapRanges(entity, model);

        return model;
    }

    private static void mapRanges(MacPool entity, org.ovirt.engine.api.model.MacPool result) {
        if (entity.getRanges() != null) {
            final Ranges ranges = new Ranges();
            for (MacRange macRange : entity.getRanges()) {
                ranges.getRanges().add(mapRange(macRange));
            }
            result.setRanges(ranges);
        }
    }

    private static Range mapRange(MacRange range) {
        final Range model = new Range();
        model.setFrom(range.getMacFrom());
        model.setTo(range.getMacTo());
        return model;
    }
}
