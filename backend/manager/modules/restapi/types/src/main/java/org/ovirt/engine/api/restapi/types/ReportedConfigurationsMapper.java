package org.ovirt.engine.api.restapi.types;

import java.util.List;

import org.ovirt.engine.api.model.ReportedConfiguration;
import org.ovirt.engine.api.model.ReportedConfigurations;

public class ReportedConfigurationsMapper {

    @Mapping(from = org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations.class, to = ReportedConfigurations.class)
    public static ReportedConfigurations map(org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations entity,
            ReportedConfigurations template) {

        ReportedConfigurations model =
                template == null ? new ReportedConfigurations() : template;

        List<ReportedConfiguration> reportedConfigurationList = model.getReportedConfigurations();
        for (org.ovirt.engine.core.common.businessentities.network.ReportedConfiguration reportedConfiguration : entity.getReportedConfigurationList()) {

            ReportedConfiguration conf = new ReportedConfiguration();
            conf.setInSync(reportedConfiguration.isInSync());
            conf.setName(reportedConfiguration.getType().getName());
            conf.setActualValue(reportedConfiguration.getActualValue());
            conf.setExpectedValue(reportedConfiguration.getExpectedValue());
            reportedConfigurationList.add(conf);
        }


        return model;
    }
}
