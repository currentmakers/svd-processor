package com.currentmakers.svd.generator.c;

import com.currentmakers.svd.parser.Peripheral;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PeripheralHeader
{
    private final Peripheral peripheral;
    private final String typeName;

    public PeripheralHeader(Peripheral peripheral)
    {
        this(peripheral, peripheral.name);
    }

    public PeripheralHeader(Peripheral peripheral, String typeName)
    {
        this.peripheral = peripheral;
        this.typeName = typeName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        String guardName = typeName.toUpperCase() + "_H";
        String peripheralName = typeName.toUpperCase();

        // Header guard
        sb.append("#ifndef ").append(guardName).append("\n");
        sb.append("#define ").append(guardName).append("\n\n");

        // Header comment
        sb.append("/**\n");
        sb.append(" * @file ").append(typeName.toLowerCase()).append(".h\n");
        sb.append(" * @brief ").append(peripheral.description).append("\n");
        if (peripheral.group != null)
        {
            sb.append(" * @group ").append(peripheral.group).append("\n");
        }
        sb.append(" * \n");
        sb.append(" * This file is auto-generated from SVD file.\n");
        sb.append(" * DO NOT EDIT MANUALLY.\n");
        sb.append(" */\n\n");

        sb.append("#include <stdint.h>\n\n");

        // Sort registers by offset to ensure correct order
        List<Register> sortedRegisters = new ArrayList<>(peripheral.registers);
        sortedRegisters.sort(Comparator.comparingInt(r -> r.offset));

        // Generate register type definitions
        for (Register register : sortedRegisters)
        {
            RegisterHeader registerHeader = new RegisterHeader(register, peripheralName);
            sb.append(registerHeader.generate());
            sb.append("\n");
        }

        // Generate peripheral structure
        sb.append("/**\n");
        sb.append(" * @brief ").append(peripheral.description).append("\n");
        sb.append(" */\n");
        sb.append("typedef struct {\n");

        int currentOffset = 0;
        for (Register register : sortedRegisters)
        {
            // Add padding for gaps between registers
            if (register.offset > currentOffset)
            {
                int padding = register.offset - currentOffset;
                sb.append("    uint8_t RESERVED_").append(String.format("0x%X", currentOffset));
                sb.append("[").append(padding).append("];");
                sb.append("  /* 0x").append(String.format("%02X", currentOffset)).append(" - Reserved */\n");
                currentOffset = register.offset;
            }

            String registerType = peripheralName + "_" + register.name.toUpperCase();
            sb.append("    ").append(registerType).append(" ").append(register.name.toUpperCase()).append(";");
            sb.append("  /* 0x").append(String.format("%02X", register.offset));
            sb.append(": ").append(register.description).append(" */\n");

            currentOffset += register.size / 8;
        }

        sb.append("} ").append(peripheralName).append("_t;\n\n");

        // Footer
        sb.append("#endif /* ").append(guardName).append(" */\n");

        return sb.toString();
    }
}
