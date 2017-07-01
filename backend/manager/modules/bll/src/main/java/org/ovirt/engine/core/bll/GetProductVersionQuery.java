package org.ovirt.engine.core.bll;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.compat.Version;

/**
 * The query returns the product version. There are two sources for this information: 'VdcVersion' contains 'major' and
 * 'minor' versions. The string looks like: 3.5.0.0, 3.6.0.0, etc. 'ProductRPMVersion' is more accurate. It usually
 * looks something like: 3.5.1-0.2.el6ev This query tries to parse ProduceRPMVersion to get 'major', 'minor' and 'build'
 * info. If it fails, it falls back to VdcVersion which has only 'major' and 'minor' info.
 */
public class GetProductVersionQuery<P extends QueryParametersBase> extends QueriesCommandBase<P> {

    public static final String RPM_REG_EX = "^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<build>\\d+)";
    public static final String VDC_VERSION_REG_EX =
            "^(?<major>\\d+)\\.(?<minor>\\d+)\\.(?<build>\\d+)\\.(?<revision>\\d+)";
    public static final Pattern rpmRegEx = Pattern.compile(RPM_REG_EX);
    public static final Pattern vdcVersionRegEx = Pattern.compile(VDC_VERSION_REG_EX);

    public GetProductVersionQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        String rpmVersion = Config.getValue(ConfigValues.ProductRPMVersion);
        Version version = parseRpmVersion(rpmVersion);
        if (version == null) {
            String vdcVersion = Config.getValue(ConfigValues.VdcVersion);
            version = parseVdcVersion(vdcVersion);
        }
        setReturnValue(version);
    }

    private Version parseRpmVersion(String rpmVersion) {
        Matcher matcher = rpmRegEx.matcher(rpmVersion);
        if (matcher.find()) {
            return new Version(Integer.parseInt(matcher.group("major")),
                    Integer.parseInt(matcher.group("minor")),
                    Integer.parseInt(matcher.group("build")), 0);
        } else {
            return null;
        }
    }

    private Version parseVdcVersion(String vdcVersion) {
        Matcher matcher = vdcVersionRegEx.matcher(vdcVersion);
        if (matcher.find()) {
            return new Version(Integer.parseInt(matcher.group("major")),
                    Integer.parseInt(matcher.group("minor")),
                    Integer.parseInt(matcher.group("build")),
                    Integer.parseInt(matcher.group("revision")));
        } else {
            return null;
        }
    }

}
