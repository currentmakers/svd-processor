package com.currentmakers.svd.generator.asm;

import com.currentmakers.svd.generator.GenerationOptions;
import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AsmGenerator
{
    private final File rootDirectory;
    private final GenerationOptions options;
    private final List<Device> devices;

    public AsmGenerator(File rootDirectory, GenerationOptions options, List<Device> devices)
    {
        this.rootDirectory = rootDirectory;
        this.options = options;
        this.devices = devices;
    }

    public void generate() throws IOException
    {
        for (Device device : devices)
        {
            generateDevice(device);
        }
    }

    private void generateDevice(Device device) throws IOException
    {
        // Create device directory
        String deviceDirName = device.name.toLowerCase();
        File deviceDir = new File(rootDirectory, deviceDirName);
        if (!deviceDir.exists())
        {
            deviceDir.mkdirs();
        }

        // Group peripherals by structural equivalence
//        Map<Peripheral, List<Peripheral>> structuralGroups = groupByStructure(device.peripherals);

        // Debug output
        System.out.println("\nStructural groups for " + device.name + ":");
//        for (Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        for (Peripheral p : device.peripherals)
        {
            System.out.print(p.name + " ");
            System.out.println();
        }

        // Track which representatives we've already generated
        Set<Peripheral> allPeripherals = new HashSet<>();

        // Generate peripheral headers (only for representative peripherals)
        for (Peripheral peripheral : device.peripherals)
        {
            PeripheralEquates peripheralEquates = new PeripheralEquates(peripheral);
            String fileName = peripheral.name.toLowerCase() + ".s";
            writeFile(new File(deviceDir, fileName), peripheralEquates.generate());
            allPeripherals.add(peripheral);
        }

        // Generate main device header (umbrella)
        DeviceEquates deviceEquates = new DeviceEquates(device);
        String deviceHeaderName = device.name.toLowerCase() + ".s";
        writeFile(new File(deviceDir, deviceHeaderName), deviceEquates.generate());
        String commonContent = new CommonGenerator().generate();
        writeFile(new File(deviceDir, "../common.s"), commonContent);
    }

//    /**
//     * Group peripherals by structural equivalence.
//     * Returns a map where the key is a representative peripheral from each group,
//     * and the value is the list of all peripherals in that group.
//     */
//    private Map<Peripheral, List<Peripheral>> groupByStructure(List<Peripheral> peripherals)
//    {
//        Map<Peripheral, List<Peripheral>> groups = new LinkedHashMap<>();
//
//        for (Peripheral peripheral : peripherals)
//        {
//            // Find if this peripheral matches any existing group
//            Peripheral representative = null;
//            for (Peripheral rep : groups.keySet())
//            {
//                if (areStructurallyEqual(peripheral, rep))
//                {
//                    representative = rep;
//                    break;
//                }
//            }
//
//            // If no match found, this peripheral becomes a new representative
//            if (representative == null)
//            {
//                representative = peripheral;
//                groups.put(representative, new ArrayList<>());
//            }
//
//            // Add peripheral to its group
//            groups.get(representative).add(peripheral);
//        }
//
//        return groups;
//    }

    /**
     * Check if two peripherals are structurally equal (same register layout).
     * Uses the equals() method on registers which compares structure but ignores reset values.
     */
    private boolean areStructurallyEqual(Peripheral p1, Peripheral p2)
    {
        // Must have same number of registers
        if (p1.registers.size() != p2.registers.size())
        {
            return false;
        }
        
        // All registers must be equal (the Register.equals ignores resetValue)
        return p1.registers.equals(p2.registers);
    }

    /**
     * Determine the type name for each structural group.
     * Strategy:
     * 1. Find common prefix among all members of the group
     * 2. If there's a collision with other groups, use representative's full name
     * 3. Otherwise use the stripped common prefix
     */
    private Map<Peripheral, String> determineTypeNames(Map<Peripheral, List<Peripheral>> structuralGroups)
    {
        Map<Peripheral, String> typeNames = new HashMap<>();
        
        // First pass: compute potential stripped names
        Map<Peripheral, String> candidateNames = new HashMap<>();
        for (Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        {
            Peripheral representative = entry.getKey();
            List<Peripheral> group = entry.getValue();
            
            String candidateName;
            if (group.size() == 1)
            {
                // Single peripheral - use its name as-is
                candidateName = representative.name;
            }
            else
            {
                // Multiple peripherals - find common prefix
                String commonPrefix = findCommonPrefix(group);
                
                // Only use the stripped prefix if it's reasonable (at least 3 chars and looks valid)
                if (commonPrefix != null && commonPrefix.length() >= 3 && 
                    !commonPrefix.matches(".*\\d$"))  // Don't end with a digit
                {
                    candidateName = commonPrefix;
                }
                else
                {
                    // Use representative name if prefix is too short or ends with digit
                    candidateName = representative.name;
                }
            }
            
            candidateNames.put(representative, candidateName);
        }
        
        // Second pass: detect and resolve collisions
        Map<String, List<Peripheral>> nameCollisions = new HashMap<>();
        for (Map.Entry<Peripheral, String> entry : candidateNames.entrySet())
        {
            nameCollisions.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        // Assign final names
        for (Map.Entry<Peripheral, String> entry : candidateNames.entrySet())
        {
            Peripheral representative = entry.getKey();
            String candidateName = entry.getValue();

            // If there's a collision, use representative's full name to distinguish
            if (nameCollisions.get(candidateName).size() > 1)
            {
                typeNames.put(representative, representative.name);
            }
            else
            {
                typeNames.put(representative, candidateName);
            }
        }

        return typeNames;
    }

    /**
     * Find common prefix among peripheral names and strip instance suffix.
     */
    private String findCommonPrefix(List<Peripheral> peripherals)
    {
        if (peripherals.isEmpty())
        {
            return "";
        }

        if (peripherals.size() == 1)
        {
            return stripInstanceSuffix(peripherals.get(0).name);
        }

        // Find the common prefix among all names
        String prefix = peripherals.get(0).name;
        for (int i = 1; i < peripherals.size(); i++)
        {
            prefix = commonPrefix(prefix, peripherals.get(i).name);
        }
        
        // Don't strip if the common prefix is already too short
        if (prefix.length() <= 2)
        {
            // Just use first peripheral name
            return peripherals.get(0).name;
        }
        
        // Strip any trailing digits or single letter from the common prefix
        String stripped = stripInstanceSuffix(prefix);
        
        // If stripping made it too short, use the unstripped prefix
        if (stripped.length() < 3)
        {
            return peripherals.get(0).name;
        }
        
        return stripped;
    }

    /**
     * Find common prefix between two strings
     */
    private String commonPrefix(String s1, String s2)
    {
        int minLen = Math.min(s1.length(), s2.length());
        int i = 0;
        while (i < minLen && s1.charAt(i) == s2.charAt(i))
        {
            i++;
        }
        return s1.substring(0, i);
    }

    /**
     * Strip instance suffix (trailing digits and/or single letter) from peripheral name.
     * Examples:
     *   GPIOA -> GPIO
     *   TIM1 -> TIM
     *   USART1 -> USART
     *   RNG -> RNG (no change)
     */
    private String stripInstanceSuffix(String name)
    {
        if (name.isEmpty())
        {
            return name;
        }
        
        String original = name;
        
        // Strip trailing digits
        int endIdx = name.length();
        while (endIdx > 0 && Character.isDigit(name.charAt(endIdx - 1)))
        {
            endIdx--;
        }
        
        // If we stripped digits and there's a single trailing letter, strip it too
        if (endIdx < name.length() && endIdx > 0 && Character.isLetter(name.charAt(endIdx - 1)))
        {
            if (endIdx >= 2 && Character.isLetter(name.charAt(endIdx - 2)))
            {
                char lastChar = name.charAt(endIdx - 1);
                char prevChar = name.charAt(endIdx - 2);
                
                // Strip single uppercase letter suffix after uppercase letters (GPIOA -> GPIO)
                if (Character.isUpperCase(lastChar) && Character.isUpperCase(prevChar))
                {
                    endIdx--;
                }
            }
        }
        
        String result = name.substring(0, endIdx);
        
        // If result is empty or too short, return original
        if (result.isEmpty() || result.length() < 2)
        {
            return original;
        }
        
        return result;
    }

    private void writeFile(File file, String content) throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
        {
            writer.write(content);
            writer.write("\n.ltorg\n");
        }
    }
}