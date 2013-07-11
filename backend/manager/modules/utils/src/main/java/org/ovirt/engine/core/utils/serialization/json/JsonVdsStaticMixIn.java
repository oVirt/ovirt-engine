package org.ovirt.engine.core.utils.serialization.json;

import java.util.HashMap;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.ovirt.engine.core.common.businessentities.VdsStatic;

@SuppressWarnings("serial")
@JsonTypeInfo(use = Id.CLASS, include = As.PROPERTY)
public abstract class JsonVdsStaticMixIn extends VdsStatic {

    @JsonIgnore
    @Override
    public abstract HashMap<String, String> getPmOptionsMap();
}
