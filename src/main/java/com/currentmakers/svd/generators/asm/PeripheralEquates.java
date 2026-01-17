package com.currentmakers.svd.generators.asm;

import com.currentmakers.svd.parser.Peripheral;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PeripheralEquates
{
    private final Peripheral peripheral;
    private final String typeName;

    public PeripheralEquates(Peripheral peripheral)
    {
        this(peripheral, peripheral.name);
    }

    public PeripheralEquates(Peripheral peripheral, String typeName)
    {
        this.peripheral = peripheral;
        this.typeName = typeName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        String peripheralName = typeName.toUpperCase();

        // Header comment
        sb.append("@\n");
        sb.append("@ @file ").append(typeName.toLowerCase()).append(".s\n");
        sb.append("@ @brief ").append(peripheral.description).append("\n");
        if (peripheral.group != null)
        {
            sb.append("@ @group ").append(peripheral.group).append("\n");
        }
        sb.append("@ \n");
        sb.append("@ This file is auto-generated from SVD file.\n");
        sb.append("@ DO NOT EDIT MANUALLY.\n");
        sb.append("@\n\n");

        sb.append(".include ../common.s\n\n");
        // Sort registers by offset to ensure correct order
        List<Register> sortedRegisters = new ArrayList<>(peripheral.registers);
        sortedRegisters.sort(Comparator.comparingInt(r -> r.offset));

        // Generate register type definitions
        for (Register register : sortedRegisters)
        {
            RegisterEquates registerEquates = new RegisterEquates(register, peripheralName);
            sb.append(registerEquates.generate());
            sb.append("\n");
        }

        // Generate peripheral structure
        sb.append("@\n");
        sb.append("@ @brief ").append(peripheral.description.replaceAll(" +", " ")).append("\n");
        sb.append("@\n");

        for (Register register : sortedRegisters)
        {
            StringBuilder b = new StringBuilder();
            b.append(".equ ")
                .append(peripheral.name.toUpperCase())
                .append("_")
                .append(register.name.toUpperCase())
                .append(", ")
                .append(String.format("0x%02X", register.offset));
            int spaces = 32 - b.length();
            if( spaces > 0 )
                b.repeat(' ', spaces);
            b.append("@ offset: 0x").append(String.format("%02X", register.offset));
            b.append(": ")
                .append(register.description.replaceAll(" +", " "))
                .append("\n");
            sb.append(b);
        }
        return sb.toString();
    }
}
