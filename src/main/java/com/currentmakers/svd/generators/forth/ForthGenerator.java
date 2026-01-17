package com.currentmakers.svd.generators.forth;

import com.currentmakers.svd.generators.GenerationOptions;
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

public class ForthGenerator
{
    private final File rootDirectory;
    private final GenerationOptions options;
    private final List<Device> devices;

    public ForthGenerator(File rootDirectory, GenerationOptions options, List<Device> devices)
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
        String deviceDirName = device.name.toLowerCase();
        File deviceDir = new File(rootDirectory, deviceDirName);
        if (!deviceDir.exists())
        {
            deviceDir.mkdirs();
        }

        Map<Peripheral, List<Peripheral>> structuralGroups = groupByStructure(device.peripherals);

        System.out.println("\nStructural groups for " + device.name + ":");
        for (Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        {
            System.out.print("  " + entry.getKey().name + ": ");
            for (Peripheral p : entry.getValue())
            {
                System.out.print(p.name + " ");
            }
            System.out.println();
        }

        Map<Peripheral, String> representativeTypeNames = determineTypeNames(structuralGroups);

        System.out.println("\nType names:");
        for (Map.Entry<Peripheral, String> entry : representativeTypeNames.entrySet())
        {
            System.out.println("  " + entry.getKey().name + " -> " + entry.getValue());
        }

        Set<Peripheral> generatedRepresentatives = new HashSet<>();

        for (Peripheral representative : structuralGroups.keySet())
        {
            if (!generatedRepresentatives.contains(representative))
            {
                String typeName = representativeTypeNames.get(representative);
                PeripheralWords peripheralWords = new PeripheralWords(representative, typeName);
                String fileName = typeName.toLowerCase() + ".fs";
                writeFile(new File(deviceDir, fileName), peripheralWords.generate());
                generatedRepresentatives.add(representative);
            }
        }

        DeviceLoader deviceLoader = new DeviceLoader(device, structuralGroups, representativeTypeNames);
        String deviceLoaderName = device.name.toLowerCase() + ".fload";
        writeFile(new File(deviceDir, deviceLoaderName), deviceLoader.generate());
    }

    /**
     * Group peripherals by structural equivalence.
     * Returns a map where the key is a representative peripheral from each group,
     * and the value is the list of all peripherals in that group.
     */
    private Map<Peripheral, List<Peripheral>> groupByStructure(List<Peripheral> peripherals)
    {
        Map<Peripheral, List<Peripheral>> groups = new LinkedHashMap<>();

        for (Peripheral peripheral : peripherals)
        {
            Peripheral representative = null;
            for (Peripheral rep : groups.keySet())
            {
                if (areStructurallyEqual(peripheral, rep))
                {
                    representative = rep;
                    break;
                }
            }

            if (representative == null)
            {
                representative = peripheral;
                groups.put(representative, new ArrayList<>());
            }

            groups.get(representative).add(peripheral);
        }

        return groups;
    }

    /**
     * Check if two peripherals are structurally equal (same register layout).
     * Uses the equals() method on registers which compares structure but ignores reset values.
     */
    private boolean areStructurallyEqual(Peripheral p1, Peripheral p2)
    {
        if (p1.registers.size() != p2.registers.size())
        {
            return false;
        }
        
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
        
        Map<Peripheral, String> candidateNames = new HashMap<>();
        for (Map.Entry<Peripheral, List<Peripheral>> entry : structuralGroups.entrySet())
        {
            Peripheral representative = entry.getKey();
            List<Peripheral> group = entry.getValue();
            
            String candidateName;
            if (group.size() == 1)
            {
                candidateName = representative.name;
            }
            else
            {
                String commonPrefix = findCommonPrefix(group);
                
                if (commonPrefix != null && commonPrefix.length() >= 3 &&
                    !commonPrefix.matches(".*\\d$"))
                {
                    candidateName = commonPrefix;
                }
                else
                {
                    candidateName = representative.name;
                }
            }
            
            candidateNames.put(representative, candidateName);
        }
        
        Map<String, List<Peripheral>> nameCollisions = new HashMap<>();
        for (Map.Entry<Peripheral, String> entry : candidateNames.entrySet())
        {
            nameCollisions.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }

        for (Map.Entry<Peripheral, String> entry : candidateNames.entrySet())
        {
            Peripheral representative = entry.getKey();
            String candidateName = entry.getValue();

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

        String prefix = peripherals.get(0).name;
        for (int i = 1; i < peripherals.size(); i++)
        {
            prefix = commonPrefix(prefix, peripherals.get(i).name);
        }
        
        if (prefix.length() <= 2)
        {
            return peripherals.get(0).name;
        }
        
        String stripped = stripInstanceSuffix(prefix);
        
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
        
        int endIdx = name.length();
        while (endIdx > 0 && Character.isDigit(name.charAt(endIdx - 1)))
        {
            endIdx--;
        }
        
        if (endIdx < name.length() && endIdx > 0 && Character.isLetter(name.charAt(endIdx - 1)))
        {
            if (endIdx >= 2 && Character.isLetter(name.charAt(endIdx - 2)))
            {
                char lastChar = name.charAt(endIdx - 1);
                char prevChar = name.charAt(endIdx - 2);
                
                if (Character.isUpperCase(lastChar) && Character.isUpperCase(prevChar))
                {
                    endIdx--;
                }
            }
        }
        
        String result = name.substring(0, endIdx);
        
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
        }
    }
}