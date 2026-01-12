package com.currentmakers.svd.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.currentmakers.svd.parser.Util.forEach;
import static com.currentmakers.svd.parser.Util.getText;

public class Device
{
    public String name;
    public List<Core> cpus = new ArrayList<>();
    public List<Peripheral> peripherals = new ArrayList<>();

    public transient Map<String, Peripheral> peripheralsByName = new HashMap<>();

    public Device(Element deviceElement)
    {
        name = getText(deviceElement, "name");
        NodeList cpuList = deviceElement.getElementsByTagName("cpu");
        for(int i = 0; i < cpuList.getLength(); i++)
        {
            Element cpuElement = (Element) cpuList.item(i);
            cpus.add(new Core(cpuElement));
        }
        forEach(deviceElement, "peripherals", "peripheral", element -> {
            Peripheral peripheral = new Peripheral(this, element);
            peripheralsByName.put( peripheral.name, peripheral);
            peripherals.add(peripheral);
        });
    }

    public Peripheral getPeripheral(String name)
    {
        return peripheralsByName.get(name);
    }

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass()) return false;
        Device device = (Device) o;
        return Objects.equals(name, device.name) && Objects.equals(cpus, device.cpus) && Objects.equals(peripherals, device.peripherals) && Objects.equals(peripheralsByName, device.peripheralsByName);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, cpus, peripherals, peripheralsByName);
    }
}
