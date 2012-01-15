package org.ovirt.engine.ui.uicommonweb.models.vms;

@SuppressWarnings("unused")
public class SpiceMenuContainerItem extends SpiceMenuItem
{
    private String privateText;

    public String getText()
    {
        return privateText;
    }

    public void setText(String value)
    {
        privateText = value;
    }

    private java.util.List<SpiceMenuItem> items;

    public java.util.List<SpiceMenuItem> getItems()
    {
        if (items == null)
        {
            items = new java.util.ArrayList<SpiceMenuItem>();
        }

        return items;
    }

    public SpiceMenuContainerItem(int id, String text)
    {
        setId(id);
        setText(text);
    }
}
