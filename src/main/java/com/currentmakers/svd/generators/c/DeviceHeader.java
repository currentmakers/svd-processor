package com.currentmakers.svd.generators.c;

import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

import java.util.*;

public class DeviceHeader
{
    private final Device device;
    private final Map<Peripheral, List<Peripheral>> structuralGroups;
    private final Map<Peripheral, String> representativeTypeNames;

    public DeviceHeader(Device device,
                        Map<Peripheral, List<Peripheral>> structuralGroups,
                        Map<Peripheral, String> representativeTypeNames)
    {
        this.device = device;
        this.structuralGroups = structuralGroups;
        this.representativeTypeNames = representativeTypeNames;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        // Header comment
        sb.append("\\\n");
        sb.append("\\ @file ").append(device.name.toLowerCase()).append(".h\n");
        sb.append("\\ @brief Device header for ").append(device.name).append("\n");
        sb.append("\\ \n");
        sb.append("\\ This file is auto-generated from SVD file.\n");
        sb.append("\\ DO NOT EDIT MANUALLY.\n");
        sb.append("\\\n\n");

        String guardName = device.name.toUpperCase() + "_DEF";
        // Header guard
        sb.append("[ifndef] ").append(guardName).append("\n");

        // Include all representative peripheral headers (only once per structure)
        sb.append("\\ Peripheral Headers\n");
        Set<String> includedHeaders = new HashSet<>();
        for (Peripheral representative : structuralGroups.keySet())
        {
            String typeName = representativeTypeNames.get(representative);
            String headerName = typeName.toLowerCase();

            if (!includedHeaders.contains(headerName))
            {
                sb.append(".include ").append(headerName).append(".fs\n");
                includedHeaders.add(headerName);
            }
        }
        sb.append("\n");

        // Build reverse lookup: peripheral -> representative
        Map<String, Peripheral> peripheralToRepresentative = new HashMap<>();
        for (Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        {
            Peripheral representative = entry.getKey();
            for (Peripheral member : entry.getValue())
            {
                peripheralToRepresentative.put(member.name, representative);
            }
        }

        // Peripheral instance definitions
        sb.append("\\ Peripheral Instance Definitions\n");
        for (Peripheral peripheral : device.peripherals)
        {
            Peripheral representative = peripheralToRepresentative.get(peripheral.name);
            String baseAddr = String.format("0x%08X", peripheral.baseAddress);
            
            sb.append("$")
                .append(baseAddr)
                .append(" constant ")
                .append(peripheral.name.toUpperCase())
            ;

            // Add comment if this uses a different representative's type
            if (!peripheral.name.equals(representative.name))
            {
                sb.append("  \\ Same structure as ").append(representative.name);
            }
            sb.append("\n");
        }
        sb.append("\n");

        // Footer
        sb.append("$0 constant ").append(guardName).append(" */\n");
        sb.append("[then]").append(guardName).append("\n\n");

        return sb.toString();
    }
}