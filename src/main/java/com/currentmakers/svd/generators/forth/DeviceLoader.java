package com.currentmakers.svd.generators.forth;

import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceLoader
{
    private final Device device;
    private final Map<Peripheral, List<Peripheral>> structuralGroups;
    private final Map<Peripheral, String> representativeTypeNames;

    public DeviceLoader(Device device,
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
        sb.append("\\\n");
        sb.append("\\ @file ").append(device.name.toLowerCase()).append(".fload\n");
        sb.append("\\ @brief Device header for ").append(device.name).append("\n");
        sb.append("\\\n");
        sb.append("\\ This file is auto-generated from SVD file.\n");
        sb.append("\\ DO NOT EDIT MANUALLY.\n");
        sb.append("\\\n\n");

        String guardName = device.name.toUpperCase() + "_LOADER";
        sb.append("[ifndef] ").append(guardName).append("\n");

        Set<String> includeds = new HashSet<>();
        for(Peripheral representative : structuralGroups.keySet())
        {
            String typeName = representativeTypeNames.get(representative);
            String name = typeName.toLowerCase();

            if(!includeds.contains(name))
            {
                sb.append("  .include \"").append(name).append(".fs\"\n");
                includeds.add(name);
            }
        }
        sb.append("\n");

        Map<String, Peripheral> peripheralToRepresentative = new HashMap<>();
        for(Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        {
            Peripheral representative = entry.getKey();
            for(Peripheral member : entry.getValue())
            {
                peripheralToRepresentative.put(member.name, representative);
            }
        }

        for(Peripheral peripheral : device.peripherals)
        {
            StringBuilder b = new StringBuilder();
            Peripheral representative = peripheralToRepresentative.get(peripheral.name);
            String typeName = representativeTypeNames.get(representative);
            String baseAddr = String.format("0x%08X", peripheral.baseAddress);

            b.append("  $")
                .append(baseAddr)
                .append(" constant ")
                .append(peripheral.name.toUpperCase())
            ;

            int spaces = 40 - b.length();
            if(spaces < 1)
                spaces = 4;
            b.repeat(' ', spaces);
            b.append("\\ See ").append(typeName.toLowerCase()).append(".fs");
            b.append("\n");

            sb.append(b);
        }
        sb.append("\n");

        // Footer
        sb.append(": ")
            .append(guardName)
            .append(" ;")
            .append(" [then]\n");

        return sb.toString();
    }
}