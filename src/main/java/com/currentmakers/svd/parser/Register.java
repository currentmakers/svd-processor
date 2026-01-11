package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.currentmakers.svd.parser.Util.forEach;
import static com.currentmakers.svd.parser.Util.getBoolean;
import static com.currentmakers.svd.parser.Util.getNumber;
import static com.currentmakers.svd.parser.Util.getText;

public class Register
{
    public String name;
    public String displayName;
    public String description;
    public List<BitField> fields = new ArrayList<>();
    public int offset;
    public int size;
    public long resetValue;
    public boolean readOnly;

    public Register(Element registerElement)
    {
        String tag = "name";
        name = getText(registerElement, tag);
        displayName = getText(registerElement, "displayName");
        description = getText(registerElement, "description");
        offset = (int) getNumber(registerElement, "addressOffset");
        size = (int) getNumber(registerElement, "size");
        resetValue = getNumber(registerElement, "resetValue");
        readOnly = getBoolean(registerElement, "readOnly");
        forEach(registerElement, "fields", "field", element -> fields.add(new BitField(element)));
    }
}
