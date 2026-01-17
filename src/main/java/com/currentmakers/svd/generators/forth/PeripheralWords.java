package com.currentmakers.svd.generators.forth;

import com.currentmakers.svd.parser.Peripheral;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PeripheralWords
{
    private final Peripheral peripheral;
    private final String typeName;

    public PeripheralWords(Peripheral peripheral, String typeName)
    {
        this.peripheral = peripheral;
        this.typeName = typeName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("\\\n");
        sb.append("\\ @file ").append(typeName.toLowerCase()).append(".fs\n");
        if( peripheral.description != null && !peripheral.description.isEmpty())
            sb.append("\\ @brief ").append(peripheral.description.replaceAll(" +", " ")).append("\n");
        if (peripheral.group != null)
        {
            sb.append("\\ @group ").append(peripheral.group).append("\n");
        }
        sb.append("\\\n");
        sb.append("\\ This file is auto-generated from SVD file.\n");
        sb.append("\\ DO NOT EDIT MANUALLY.\n");
        sb.append("\\\n\n");

        String guardName = typeName.toUpperCase() + "_DEF";
        String peripheralName = typeName.toUpperCase();

        sb.append("[ifndef] ").append(guardName).append("\n");

        List<Register> sortedRegisters = new ArrayList<>(peripheral.registers);
        sortedRegisters.sort(Comparator.comparingInt(r -> r.offset));

        for (Register register : sortedRegisters)
        {
            sb.append("\n")
                .append("  [ifdef] ")
                .append(peripheralName + "_" + register.name + "_DEF")
                .append("\n")
            ;
            RegisterWords registerWords = new RegisterWords(register, peripheralName);
            sb.append(registerWords.generate());
            sb.append("  [then]\n\n");
        }

        sb.append("  \\\n");
        if( peripheral.description != null && !peripheral.description.isEmpty())
            sb.append("  \\ @brief ").append(peripheral.description.replaceAll(" +", " ")).append("\n");
        sb.append("  \\\n");

        for (Register register : sortedRegisters)
        {
            String registerType = peripheralName + "_" + register.name.toUpperCase();
            StringBuilder b = new StringBuilder();
            b.append(String.format("  $%02X", register.offset));
            b.append(" constant ").append(registerType);
            int spaces = 40 - b.length();
            if( spaces < 1)
                spaces = 4;
            b.repeat(' ', spaces);
            sb.append(b);
            if(register.description != null && !register.description.isEmpty())
                sb.append("\\ ").append(register.description.replaceAll(" +", " "));
            sb.append("\n");
        }

        sb.append("\n: ").append(guardName).append(" ; ");
        sb.append("[then]\n");

        return sb.toString();
    }
}
