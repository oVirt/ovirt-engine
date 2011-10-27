package org.ovirt.engine.core.common.businessentities;

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TagsType")
public enum TagsType {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
    GeneralTag(0),
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    // [EnumMember]
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
