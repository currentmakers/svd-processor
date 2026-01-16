package com.currentmakers.svd.generator.asm;

public class CommonGenerator
{

    public String generate()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("@\n");
        sb.append("@ @file cmasm_common.s\n");
        sb.append("@ @brief ").append("Common definitions needed by all devices." );
        sb.append("@ \n");
        sb.append("@ This file is auto-generated from SVD file.\n");
        sb.append("@ DO NOT EDIT MANUALLY.\n");
        sb.append("@\n\n");
        sb.append("-----------------------------------------------------------------------------\n");
        sb.append("Bit-position equates (for setting or clearing a single bit)\n");
        sb.append("-----------------------------------------------------------------------------\n");
        sb.append("\n");
        sb.append(".equ  BIT0,    0x00000001\n");
        sb.append(".equ  BIT1,    0x00000002\n");
        sb.append(".equ  BIT2,    0x00000004\n");
        sb.append(".equ  BIT3,    0x00000008\n");
        sb.append(".equ  BIT4,    0x00000010\n");
        sb.append(".equ  BIT5,    0x00000020\n");
        sb.append(".equ  BIT6,    0x00000040\n");
        sb.append(".equ  BIT7,    0x00000080\n");
        sb.append(".equ  BIT8,    0x00000100\n");
        sb.append(".equ  BIT9,    0x00000200\n");
        sb.append(".equ  BIT10,   0x00000400\n");
        sb.append(".equ  BIT11,   0x00000800\n");
        sb.append(".equ  BIT12,   0x00001000\n");
        sb.append(".equ  BIT13,   0x00002000\n");
        sb.append(".equ  BIT14,   0x00004000\n");
        sb.append(".equ  BIT15,   0x00008000\n");
        sb.append(".equ  BIT16,   0x00010000\n");
        sb.append(".equ  BIT17,   0x00020000\n");
        sb.append(".equ  BIT18,   0x00040000\n");
        sb.append(".equ  BIT19,   0x00080000\n");
        sb.append(".equ  BIT20,   0x00100000\n");
        sb.append(".equ  BIT21,   0x00200000\n");
        sb.append(".equ  BIT22,   0x00400000\n");
        sb.append(".equ  BIT23,   0x00800000\n");
        sb.append(".equ  BIT24,   0x01000000\n");
        sb.append(".equ  BIT25,   0x02000000\n");
        sb.append(".equ  BIT26,   0x04000000\n");
        sb.append(".equ  BIT27,   0x08000000\n");
        sb.append(".equ  BIT28,   0x10000000\n");
        sb.append(".equ  BIT29,   0x20000000\n");
        sb.append(".equ  BIT30,   0x40000000\n");
        sb.append(".equ  BIT31,   0x80000000\n");
        return sb.toString();
    }
}
