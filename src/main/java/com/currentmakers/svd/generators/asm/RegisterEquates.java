package com.currentmakers.svd.generators.asm;

import com.currentmakers.svd.parser.BitField;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RegisterEquates
{
    private final Register register;
    private final String peripheralName;

    public RegisterEquates(Register register, String peripheralName)
    {
        this.register = register;
        this.peripheralName = peripheralName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();

        // Comment block
        sb.append("@\n");
        sb.append("@ @brief ").append(register.description.replaceAll(" +", " ")).append("\n");
        sb.append("@ Address offset: 0x").append(String.format("%02X", register.offset)).append("\n");
        sb.append("@ Reset value: 0x").append(String.format("%08X", register.resetValue)).append("\n");
        if(register.readOnly)
        {
            sb.append("@ Access: Read-only\n");
        }
        sb.append("@\n\n");

        if(!register.fields.isEmpty())
        {
            // Sort fields by bit offset
            List<BitField> sortedFields = new ArrayList<>(register.fields);
            sortedFields.sort(Comparator.comparingInt(f -> f.offset));

            for(BitField field : sortedFields)
            {
                StringBuilder b = new StringBuilder();
                b.append(".equ ")
                    .append(peripheralName.toUpperCase())
                    .append("_")
                    .append(register.name.toUpperCase())
                    .append("_")
                    .append(field.name.toUpperCase())
                    .append(", ")
                ;
                b.append("(BIT").append(field.offset);

                for( int j= 1; j < field.width; j++ )
                    b.append(" | BIT").append(field.offset + j);
                b.append(")");
                sb.append(b);

                int spaces = 64 - b.length();
                if( spaces > 0 )
                    sb.repeat(' ', spaces);
                // Add description if available
                if(field.description != null && !field.description.isEmpty())
                {
                    // Clean up the description for single-line comment
                    String cleanDesc = field.description.replaceAll("\\s+", " ").trim();
                    sb.append("    @ ");
                    sb.append(cleanDesc);
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
