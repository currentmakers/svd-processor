package com.currentmakers.svd.generator.c;

import com.currentmakers.svd.parser.BitField;
import com.currentmakers.svd.parser.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RegisterHeader
{
    private final Register register;
    private final String peripheralName;

    public RegisterHeader(Register register, String peripheralName)
    {
        this.register = register;
        this.peripheralName = peripheralName;
    }

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        String registerTypeName = peripheralName + "_" + register.name.toUpperCase() + "_Register";

        // Comment block
        sb.append("/**\n");
        sb.append(" * @brief ").append(register.description).append("\n");
        sb.append(" * Address offset: 0x").append(String.format("%02X", register.offset)).append("\n");
        sb.append(" * Reset value: 0x").append(String.format("%08X", register.resetValue)).append("\n");
        if (register.readOnly)
        {
            sb.append(" * Access: Read-only\n");
        }
        sb.append(" */\n");

        // Union definition
        sb.append("typedef union {\n");

        // Value field (with const if read-only)
        sb.append("    ");
        if (register.readOnly)
        {
            sb.append("const ");
        }
        sb.append(getCType(register.size)).append(" value;\n");

        // Bitfield struct (only if there are fields)
        if (!register.fields.isEmpty())
        {
            sb.append("    struct {\n");

            // CRITICAL: Sort fields by bit offset to ensure correct order
            List<BitField> sortedFields = new ArrayList<>(register.fields);
            sortedFields.sort(Comparator.comparingInt(f -> f.offset));

            int currentBit = 0;
            for (BitField field : sortedFields)
            {
                // Add reserved bits before this field if needed
                if (field.offset > currentBit)
                {
                    int reservedBits = field.offset - currentBit;
                    sb.append("        ").append(getCType(register.size)).append(" : ");
                    sb.append(reservedBits).append(";");
                    sb.append("  /* Reserved */\n");
                    currentBit = field.offset;
                }

                // Add the field
                sb.append("        ");
                if (register.readOnly)
                {
                    sb.append("const ");
                }
                sb.append(getCType(register.size)).append(" ");
                sb.append(field.name).append(" : ").append(field.width).append(";");
                
                // Build comment with bit range and description
                sb.append("  /* [").append(field.offset);
                if (field.width > 1)
                {
                    sb.append(":").append(field.offset + field.width - 1);
                }
                sb.append("] ");
                
                // Add description if available
                if (field.description != null && !field.description.isEmpty())
                {
                    // Clean up the description for single-line comment
                    String cleanDesc = field.description.replaceAll("\\s+", " ").trim();
                    sb.append(cleanDesc);
                }
                else
                {
                    sb.append(field.name);
                }
                sb.append(" */\n");

                currentBit = field.offset + field.width;
            }

            // Add reserved bits at the end if needed
            if (currentBit < register.size)
            {
                int reservedBits = register.size - currentBit;
                sb.append("        ").append(getCType(register.size)).append(" : ");
                sb.append(reservedBits).append(";");
                sb.append("  /* Reserved */\n");
            }

            sb.append("    };\n");
        }

        sb.append("} ").append(registerTypeName).append(";\n");

        return sb.toString();
    }

    private String getCType(int bitWidth)
    {
        return switch (bitWidth)
        {
            case 8 -> "uint8_t";
            case 16 -> "uint16_t";
            case 32 -> "uint32_t";
            case 64 -> "uint64_t";
            default -> "uint32_t"; // Default to 32-bit
        };
    }
}
