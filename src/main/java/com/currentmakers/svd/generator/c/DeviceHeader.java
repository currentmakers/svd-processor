package com.currentmakers.svd.generator.c;

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
        String guardName = device.name.toUpperCase() + "_H";

        // Header guard
        sb.append("#ifndef ").append(guardName).append("\n");
        sb.append("#define ").append(guardName).append("\n\n");

        // Header comment
        sb.append("/**\n");
        sb.append(" * @file ").append(device.name.toLowerCase()).append(".h\n");
        sb.append(" * @brief Device header for ").append(device.name).append("\n");
        sb.append(" * \n");
        sb.append(" * This file is auto-generated from SVD file.\n");
        sb.append(" * DO NOT EDIT MANUALLY.\n");
        sb.append(" */\n\n");

        // Standard includes
        sb.append("#include <stdint.h>\n\n");

        // Include all representative peripheral headers (only once per structure)
        sb.append("/* Peripheral Headers */\n");
        Set<String> includedHeaders = new HashSet<>();
        for (Peripheral representative : structuralGroups.keySet())
        {
            String typeName = representativeTypeNames.get(representative);
            String headerName = typeName.toLowerCase();

            if (!includedHeaders.contains(headerName))
            {
                sb.append("#include \"").append(headerName).append(".h\"\n");
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
        sb.append("/* Peripheral Instance Definitions */\n");
        for (Peripheral peripheral : device.peripherals)
        {
            Peripheral representative = peripheralToRepresentative.get(peripheral.name);
            String typeName = representativeTypeNames.get(representative);
            String peripheralType = typeName.toUpperCase() + "_t";
            String baseAddr = String.format("0x%08X", peripheral.baseAddress);
            
            sb.append("#define ").append(peripheral.name.toUpperCase());
            sb.append("  ((volatile ").append(peripheralType).append(" *) ");
            sb.append(baseAddr).append(")");

            // Add comment if this uses a different representative's type
            if (!peripheral.name.equals(representative.name))
            {
                sb.append("  /* Same structure as ").append(representative.name).append(" */");
            }
            sb.append("\n");
        }
        sb.append("\n");

        // Footer
        sb.append("#endif /* ").append(guardName).append(" */\n");

        return sb.toString();
    }
}