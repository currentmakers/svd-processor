package com.currentmakers.svd.parser;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static com.currentmakers.svd.parser.Util.forEach;
import static com.currentmakers.svd.parser.Util.getText;

public class Device
{
    public String name;
    public List<Core> cpus = new ArrayList<>();
    public List<Peripheral> peripherals = new ArrayList<>();

    public Device(Element deviceElement)
    {
        name = getText(deviceElement, "name");
        NodeList cpuList = deviceElement.getElementsByTagName("cpu");
        for(int i = 0; i < cpuList.getLength(); i++)
        {
            Element cpuElement = (Element) cpuList.item(i);
            cpus.add(new Core(cpuElement));
        }
        forEach(deviceElement, "peripherals", "peripheral", element -> peripherals.add(new Peripheral(element)));
    }
}
