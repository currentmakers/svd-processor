package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import static com.currentmakers.svd.parser.Util.getNumber;
import static com.currentmakers.svd.parser.Util.getText;

public class BitField
{
    public String name;
    public String description;
    public int offset;
    public int width;

    public BitField(Element bitFieldElement)
    {
        name = getText(bitFieldElement, "name");
        description = getText(bitFieldElement, "description");
        offset = (int) getNumber(bitFieldElement, "bitOffset");
        width = (int) getNumber(bitFieldElement, "bitWidth");
    }
}
