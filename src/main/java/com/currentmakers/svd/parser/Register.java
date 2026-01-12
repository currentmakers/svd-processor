package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    public Long resetValue;
    public boolean readOnly;

    @SuppressWarnings("DataFlowIssue")
    public Register(Element registerElement)
    {
        String tag = "name";
        name = getText(registerElement, tag);
        displayName = getText(registerElement, "displayName");
        description = getText(registerElement, "description");
        offset = getNumber(registerElement, "addressOffset").intValue();
        size = getNumber(registerElement, "size").intValue();
        resetValue = getNumber(registerElement, "resetValue");
        readOnly = getBoolean(registerElement, "readOnly");
        forEach(registerElement, "fields", "field", element -> fields.add(new BitField(element)));
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass()) return false;
        Register register = (Register) o;
        return offset == register.offset && size == register.size && readOnly == register.readOnly && Objects.equals(name, register.name) && Objects.equals(fields, register.fields);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, fields, offset, size, readOnly);
    }
}
