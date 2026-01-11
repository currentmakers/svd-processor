package com.currentmakers.svd.generator.c;

import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

public class DeviceHeader
{
    private final Device device;

    public DeviceHeader(Device device)
    {
        this.device = device;
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

        // Include all peripheral headers
        sb.append("/* Peripheral Headers */\n");
        for (Peripheral peripheral : device.peripherals)
        {
            sb.append("#include \"").append(peripheral.name.toLowerCase()).append(".h\"\n");
        }
        sb.append("\n");

        // Peripheral instance definitions
        sb.append("/* Peripheral Instance Definitions */\n");
        for (Peripheral peripheral : device.peripherals)
        {
            String peripheralType = peripheral.name.toUpperCase() + "_TypeDef";
            String baseAddr = String.format("0x%08X", peripheral.baseAddress);
            sb.append("#define ").append(peripheral.name.toUpperCase());
            sb.append("  ((volatile ").append(peripheralType).append(" *) ");
            sb.append(baseAddr).append(")\n");
        }
        sb.append("\n");

        // Footer
        sb.append("#endif /* ").append(guardName).append(" */\n");

        return sb.toString();
    }
}
