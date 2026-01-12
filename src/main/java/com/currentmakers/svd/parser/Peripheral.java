package com.currentmakers.svd.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.currentmakers.svd.parser.Util.forEach;
import static com.currentmakers.svd.parser.Util.getNumber;
import static com.currentmakers.svd.parser.Util.getText;

public class Peripheral
{
    public String name;
    public String description;
    public String group;
    public long baseAddress;
    public AddressBlock addressBlock;
    public Interrupt interrupt;
    public List<Register> registers = new ArrayList<>();

    public Peripheral(Device device, Element peripheralElement)
    {
        String derivedFrom = peripheralElement.getAttribute("derivedFrom");
        if(!derivedFrom.isEmpty())
        {
            Peripheral derived = device.getPeripheral(derivedFrom);
            this.name = derived.name;
            this.description = derived.description;
            this.group = derived.group;
            this.baseAddress = derived.baseAddress;
            this.addressBlock = derived.addressBlock;
            this.interrupt = derived.interrupt;
            this.registers.addAll(derived.registers);
        }
        String s = getText(peripheralElement, "name");
        if(s != null)
            name = s;
        s = getText(peripheralElement, "description");
        if(s != null)
            description = s;
        s = getText(peripheralElement, "group");
        if(s != null)
            group = s;
        Long n = getNumber(peripheralElement, "baseAddress");
        if(n != null)
            baseAddress = n;
        NodeList abList = peripheralElement.getElementsByTagName("addressBlock");
        if(abList.getLength() > 0)
            addressBlock = new AddressBlock((Element) abList.item(0));
        NodeList interruptList = peripheralElement.getElementsByTagName("interrupt");
        if(interruptList.getLength() > 0)
            interrupt = new Interrupt((Element) interruptList.item(0));
        forEach(peripheralElement, "registers", "register", element -> {
            registers.add(new Register(element));
        });
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass()) return false;
        Peripheral that = (Peripheral) o;
        return Objects.equals(registers, that.registers);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(registers);
    }
}
