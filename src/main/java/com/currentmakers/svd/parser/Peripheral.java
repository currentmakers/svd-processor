package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static com.currentmakers.svd.parser.Util.*;

public class Peripheral
{
    public String name;
    public String description;
    public String group;
    public long baseAddress;
    public AddressBlock addressBlock;
    public Interrupt interrupt;
    public List<Register> registers = new ArrayList<>();

    public Peripheral(Element peripheralElement)
    {
        name = getText(peripheralElement, "name");
        description = getText(peripheralElement, "description");
        group = getText(peripheralElement, "group");
        baseAddress = getNumber(peripheralElement, "baseAddress");
        forEach(peripheralElement, "registers", "register", element -> {
            registers.add(new Register(element));
        });
    }

}
