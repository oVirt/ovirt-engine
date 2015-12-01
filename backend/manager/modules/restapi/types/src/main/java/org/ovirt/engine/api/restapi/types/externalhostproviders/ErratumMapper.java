package org.ovirt.engine.api.restapi.types.externalhostproviders;

import static org.ovirt.engine.api.restapi.utils.HexUtils.hex2string;
import static org.ovirt.engine.api.restapi.utils.HexUtils.string2hex;

import java.util.ArrayList;

import org.ovirt.engine.api.model.KatelloErratum;
import org.ovirt.engine.api.model.Package;
import org.ovirt.engine.api.model.Packages;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.api.restapi.types.Mapping;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataSeverity;
import org.ovirt.engine.core.common.businessentities.Erratum.ErrataType;
public class ErratumMapper {

    @Mapping(from = Erratum.class, to = KatelloErratum.class)
    public static KatelloErratum map(Erratum entity, KatelloErratum model) {
        model = model == null ? new KatelloErratum() : model;
        model.setId(string2hex(entity.getId()));
        model.setName(entity.getId());
        model.setTitle(entity.getTitle());
        model.setSummary(entity.getSummary());
        model.setSolution(entity.getSolution());
        model.setDescription(entity.getDescription());
        if (entity.getIssued() != null) {
            model.setIssued(DateMapper.map(entity.getIssued(), null));
        }

        model.setSeverity(entity.getSeverity() == null ? null : entity.getSeverity().getDescription());
        model.setType(entity.getType() == null ? null : entity.getType().getDescription());
        if (entity.getPackages() != null && !entity.getPackages().isEmpty()) {
            Packages packages = new Packages();
            for (String packageName : entity.getPackages()) {
                Package p = new Package();
                p.setName(packageName);
                packages.getPackages().add(p);
            }

            model.setPackages(packages);
        }

        return model;
    }

    @Mapping(from = KatelloErratum.class, to = Erratum.class)
    public static Erratum map(KatelloErratum model, Erratum template) {
        Erratum entity = template != null ? template : new Erratum();

        if (model.isSetId()) {
            entity.setId(hex2string(model.getId()));
        }

        if (model.isSetTitle()) {
            entity.setTitle(model.getTitle());
        }

        if (model.isSetSummary()) {
            entity.setSummary(model.getSummary());
        }

        if (model.isSetSolution()) {
            entity.setSolution(model.getSolution());
        }

        if (model.isSetDescription()) {
            entity.setDescription(model.getDescription());
        }

        if (model.isSetIssued()) {
            entity.setIssued(model.getIssued().toGregorianCalendar().getTime());
        }

        if (model.isSetSeverity()) {
            entity.setSeverity(ErrataSeverity.byDescription(model.getSeverity()));
        }

        if (model.isSetType()) {
            entity.setType(ErrataType.byDescription(model.getType()));
        }

        if (model.isSetPackages() && !model.getPackages().getPackages().isEmpty()) {
            entity.setPackages(new ArrayList<>(model.getPackages().getPackages().size()));
            for (Package p : model.getPackages().getPackages()) {
                entity.getPackages().add(p.getName());
            }
        }

        return entity;
    }
}
