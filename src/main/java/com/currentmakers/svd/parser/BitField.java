package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import java.util.Objects;

import static com.currentmakers.svd.parser.Util.getNumber;
import static com.currentmakers.svd.parser.Util.getText;

public class BitField
{
    public String name;
    public String description;
    public int offset;
    public int width;

    @SuppressWarnings("DataFlowIssue")
    public BitField(Element bitFieldElement)
    {
        name = getText(bitFieldElement, "name");
        description = getText(bitFieldElement, "description");
        offset = getNumber(bitFieldElement, "bitOffset").intValue();
        width = getNumber(bitFieldElement, "bitWidth").intValue();
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass()) return false;
        BitField bitField = (BitField) o;
        return offset == bitField.offset && width == bitField.width && Objects.equals(name, bitField.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, offset, width);
    }
}
