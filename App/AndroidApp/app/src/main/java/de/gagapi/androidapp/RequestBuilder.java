package de.gagapi.androidapp;


/**
 * Helper class to build the server request string.
 */
public class RequestBuilder
{
    final boolean NORMALIZE_VALUES = false;
    StringBuilder sb = new StringBuilder();
    public RequestBuilder(){}

    public void AppendValue(float value, float minRange, float maxRange)
    {
        if(NORMALIZE_VALUES)
        {
            float normalizedValue = (value - minRange) / (maxRange - minRange) * (1-(-1)) + (-1);
            sb.append(normalizedValue).append(",");
        }
        else
        {
            sb.append(value).append(",");
        }

    }

    public void AppendValueFinal(float value, float minRange, float maxRange)
    {
        if(NORMALIZE_VALUES)
        {
            float normalizedValue = (value - minRange) / (maxRange - minRange) * (1-(-1)) + (-1);
            sb.append(normalizedValue);
        }
        else
        {
            sb.append(value);
        }
    }

    public String GetString()
    {
        return sb.toString();
    }
    public void Clear()
    {
        sb.setLength(0);
    }
}
