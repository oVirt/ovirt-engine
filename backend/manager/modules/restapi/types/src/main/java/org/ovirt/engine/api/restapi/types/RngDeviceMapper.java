package org.ovirt.engine.api.restapi.types;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.model.Rate;
import org.ovirt.engine.api.model.RngDevice;
import org.ovirt.engine.api.model.RngSource;
import org.ovirt.engine.api.model.RngSources;
import org.ovirt.engine.core.common.businessentities.VmRngDevice;

public class RngDeviceMapper {

    @Mapping(from = VmRngDevice.class, to = RngDevice.class)
    public static RngDevice map(VmRngDevice entity, RngDevice template) {
        RngDevice model = (template == null)
                ? new RngDevice()
                : template;

        if (entity.getBytes() != null) {
            model.setRate(new Rate());
            model.getRate().setBytes(entity.getBytes());
            if (entity.getPeriod() != null) {
                model.getRate().setPeriod(entity.getPeriod());
            }
        }

        RngSource restSource = map(entity.getSource(), null);
        model.setSource(restSource.toString());

        return model;
    }

    @Mapping(from = VmRngDevice.Source.class, to = RngSource.class)
    public static RngSource map(VmRngDevice.Source backend, RngSource rest) {
        if (backend != null) {
            return RngSource.fromValue(backend.name());
        }

        return null;
    }

    @Mapping(from = RngSource.class, to = VmRngDevice.Source.class)
    public static VmRngDevice.Source map(RngSource model, VmRngDevice.Source template) {
        if (model != null) {
            return VmRngDevice.Source.valueOf(model.name());
        }

        return null;
    }

    @Mapping(from = RngDevice.class, to = VmRngDevice.class)
    public static VmRngDevice map(RngDevice model, VmRngDevice template) {
        if (model != null && model.isSetSource()) {
            VmRngDevice dev = new VmRngDevice();

            if (model.isSetRate()) {
                dev.setBytes(model.getRate().getBytes());
                if (model.getRate().isSetPeriod()) {
                    dev.setPeriod(model.getRate().getPeriod());
                }
            }

            RngSource restSource = RngSource.fromValue(model.getSource());
            VmRngDevice.Source source = VmRngDevice.Source.valueOf(restSource.name());

            if (source != null) {
                dev.setSource(source);
                return dev;
            }
        }

        return null;
    }

    @Mapping(from = RngSources.class, to = VmRngDevice.Source.class)
    public static List<VmRngDevice.Source> mapRngSources(RngSources model, List<VmRngDevice.Source> template) {
        if (model.getRngSources() != null) {
            List<VmRngDevice.Source> result = new ArrayList<>();
            for (String sourceString : model.getRngSources()) {
                try {
                    RngSource source = RngSource.fromValue(sourceString);
                    result.add(map(source, null));
                } catch (IllegalArgumentException iae) {
                    // do nothing - we have illegal string here
                }
            }
            return result;
        }

        return null;
    }

    @Mapping(from = VmRngDevice.Source.class, to = RngSources.class)
    public static RngSources mapRngSources(Collection<VmRngDevice.Source> entity, RngSources template) {
        RngSources result = (template == null)
                ? new RngSources()
                : template;

        if (entity != null) {
            for (VmRngDevice.Source source : entity) {
                RngSource restSource = map(source, null);
                result.getRngSources().add(restSource.name());
            }
        }

        return result;
    }

}
