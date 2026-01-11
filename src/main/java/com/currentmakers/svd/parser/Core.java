package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

public class Core
{
    public String name;
    public String revision;
    public Endian endian;
    public boolean mpuPresent;
    public boolean fpuPresent;
    public int nvicPrioBits;
    public boolean vendorSystickConfig;

    public Core(Element cpuElement)
    {
        name = cpuElement.getElementsByTagName("name").item(0).getTextContent();
        revision = cpuElement.getElementsByTagName("revision").item(0).getTextContent();
        String endianess = cpuElement.getElementsByTagName("endian").item(0).getTextContent();
        endian = Endian.valueOf(endianess);
        mpuPresent = Boolean.parseBoolean(cpuElement.getElementsByTagName("mpuPresent").item(0).getTextContent());
        fpuPresent = Boolean.parseBoolean(cpuElement.getElementsByTagName("fpuPresent").item(0).getTextContent());
        nvicPrioBits = Integer.parseInt(cpuElement.getElementsByTagName("nvicPrioBits").item(0).getTextContent());
        vendorSystickConfig = Boolean.parseBoolean(cpuElement.getElementsByTagName("vendorSystickConfig").item(0).getTextContent());
    }

}
