package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TagsType")
public enum TagsType {
    GeneralTag(0),
    AdElementTag(1);

    private int intValue;
    private static java.util.HashMap<Integer, TagsType> mappings = new HashMap<Integer, TagsType>();

    static {
        for (TagsType tagsType : values()) {
            mappings.put(tagsType.getValue(), tagsType);
        }
    }

    private TagsType(int value) {
        intValue = value;
    }

    public int getValue() {
        return intValue;
    }

    public static TagsType forValue(int value) {
        return mappings.get(value);
    }
}
