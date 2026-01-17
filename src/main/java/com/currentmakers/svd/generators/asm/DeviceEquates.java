package com.currentmakers.svd.generators.asm;

import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

import java.util.HashSet;
import java.util.Set;

public class DeviceEquates
{
    private final Device device;

    public DeviceEquates(Device device)
    {
        this.device = device;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();

        // Header comment
        sb.append("@\n");
        sb.append("@ @file ").append(device.name.toLowerCase()).append(".h\n");
        sb.append("@ @brief Device header for ").append(device.name).append("\n");
        sb.append("@ \n");
        sb.append("@ This file is auto-generated from SVD file.\n");
        sb.append("@ DO NOT EDIT MANUALLY.\n");
        sb.append("@\n\n");

        // Include all representative peripheral headers (only once per structure)
        sb.append("@ Peripheral Headers\n\n");
        Set<String> includedHeaders = new HashSet<>();
        for(Peripheral peripheral : device.peripherals)
        {
            String typeName = peripheral.name;
            String headerName = typeName.toLowerCase();

            if(!includedHeaders.contains(headerName))
            {
                sb.append(".include ").append(headerName).append(".d\n");
                includedHeaders.add(headerName);
            }
        }
        sb.append("\n");

        // Peripheral instance definitions
        sb.append("@ Peripheral Instance Definitions\n");
        for(Peripheral peripheral : device.peripherals)
        {
            String baseAddr = String.format("0x%08X", peripheral.baseAddress);

            StringBuilder b = new StringBuilder();
            b.append(".equ ").append(peripheral.name.toUpperCase());
            b.append(", ");
            b.append(baseAddr);
            int spaces = 32 - b.length();
            if(spaces > 0)
                b.repeat(' ', spaces);
            b.append("@ ");
            b.append(peripheral.description.replaceAll(" +", " "));
            b.append("\n");
            sb.append(b);
        }
        sb.append("\n");
        return sb.toString();
    }
}