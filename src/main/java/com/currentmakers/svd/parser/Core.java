package com.currentmakers.svd.parser;

import org.w3c.dom.Element;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if(o == null || getClass() != o.getClass()) return false;
        Core core = (Core) o;
        return mpuPresent == core.mpuPresent && fpuPresent == core.fpuPresent && nvicPrioBits == core.nvicPrioBits && vendorSystickConfig == core.vendorSystickConfig && Objects.equals(name, core.name) && Objects.equals(revision, core.revision) && endian == core.endian;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, revision, endian, mpuPresent, fpuPresent, nvicPrioBits, vendorSystickConfig);
    }
}
