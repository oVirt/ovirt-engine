package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class AttachCdModel extends Model
{

    private ListModel privateIsoImage;

    public ListModel getIsoImage()
    {
        return privateIsoImage;
    }

    private void setIsoImage(ListModel value)
    {
        privateIsoImage = value;
    }

    public AttachCdModel()
    {
        setIsoImage(new ListModel());
    }
}
