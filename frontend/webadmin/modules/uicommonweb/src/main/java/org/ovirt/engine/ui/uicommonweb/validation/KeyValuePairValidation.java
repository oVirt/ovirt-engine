package org.ovirt.engine.ui.uicommonweb.validation;

import org.ovirt.engine.core.compat.StringHelper;

@SuppressWarnings("unused")
public class KeyValuePairValidation implements IValidation
{
    private boolean privateallowAlsoKey;

    private boolean getallowAlsoKey()
    {
        return privateallowAlsoKey;
    }

    private void setallowAlsoKey(boolean value)
    {
        privateallowAlsoKey = value;
    }

    public KeyValuePairValidation()
    {
        this.setallowAlsoKey(false);
    }

    // allows key without value, i.e. key,key=value,key,key</param>
    public KeyValuePairValidation(boolean allowAlsoKey)
    {
        this.setallowAlsoKey(allowAlsoKey);
    }

    @Override
    public ValidationResult Validate(Object value)
    {
        ValidationResult result = new ValidationResult();

        if (value != null && value instanceof String && !((String) value).equals(""))
        {
            String strValue = (String) value;

            if (strValue.endsWith(","))
            {
                result.setSuccess(false);
            }
            else
            {
                // Try parse value.
                for (String pair : strValue.split("[,]", -1))
                {
                    if (!result.getSuccess())
                    {
                        break;
                    }

                    String[] array = pair.split("[=]", -1);

                    // if the split length is 2, its a 'key=value'
                    // if the split length is 1 (key), we accept only when we allow it (allowAlsoKey==true)
                    if (getallowAlsoKey())
                    {
                        if (array.length < 1 || array.length > 2)
                        {
                            result.setSuccess(false);
                            break;
                        }
                    }
                    else
                    {
                        if (array.length != 2)
                        {
                            result.setSuccess(false);
                            break;
                        }
                    }

                    for (String t : array)
                    {
                        if (StringHelper.isNullOrEmpty(t.trim()))
                        {
                            result.setSuccess(false);
                            break;
                        }
                    }
                }
            }
        }

        if (!result.getSuccess())
        {
            if (!getallowAlsoKey())
            {
                result.getReasons().add("Value doesn't not match pattern: key=value,key=value...");
            }
            else
            {
                result.getReasons().add("Value doesn't not match pattern: key=value,key,key=value...");
            }
        }

        return result;
    }
}
