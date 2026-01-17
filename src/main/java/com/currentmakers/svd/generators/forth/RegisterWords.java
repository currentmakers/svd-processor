package com.currentmakers.svd.generators.forth;

import com.currentmakers.svd.parser.BitField;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RegisterWords
{
    private final Register register;
    private final String peripheralName;

    public RegisterWords(Register register, String peripheralName)
    {
        this.register = register;
        this.peripheralName = peripheralName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        String registerTypeName = (peripheralName + "_" + register.name).toUpperCase();

        sb.append("    \\\n");
        if(register.description != null && !register.description.isEmpty())
            sb.append("    \\ @brief ").append(register.description.replaceAll(" +", " ")).append("\n");
        sb.append("    \\ Address offset: 0x").append(String.format("%02X", register.offset)).append("\n");
        sb.append("    \\ Reset value: 0x").append(String.format("%08X", register.resetValue)).append("\n");
        if(register.readOnly)
        {
            sb.append("    \\ Access: Read-only\n");
        }
        sb.append("    \\\n");

        List<BitField> sortedFields = new ArrayList<>(register.fields);
        sortedFields.sort(Comparator.comparingInt(f -> f.offset));

        for(BitField field : sortedFields)
        {
            StringBuilder b = new StringBuilder();
            b.append(String.format("    $%02x constant %s ", field.offset, peripheralName + "_" + field.name));

            int spaces = 48 - b.length();
            if(spaces < 1)
                spaces = 4;
            b.repeat(' ', spaces);

            b.append(String.format("\\ [0x%02x", field.offset));
            if(field.width > 1)
            {
                b.append(" : ").append(field.width);
            }
            b.append("] ");

            if(field.description != null && !field.description.isEmpty())
            {
                String cleanDesc = field.description.replaceAll("\\s+", " ").trim();
                b.append(cleanDesc);
            }
            else
            {
                b.append(field.name);
            }
            b.append("\n");
            sb.append(b);
        }
        return sb.toString();
    }
}
