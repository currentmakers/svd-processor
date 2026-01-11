package com.currentmakers.svd.generator.c;

import com.currentmakers.svd.parser.Device;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CGenerator
{
    private final File rootDirectory;
    private final GenerationOptions options;
    private final List<Device> devices;

    public CGenerator(File rootDirectory, GenerationOptions options, List<Device> devices)
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

        // Generate peripheral headers
        for (var peripheral : device.peripherals)
        {
            PeripheralHeader peripheralHeader = new PeripheralHeader(peripheral);
            String fileName = peripheral.name.toLowerCase() + ".h";
            writeFile(new File(deviceDir, fileName), peripheralHeader.generate());
        }

        // Generate main device header (umbrella)
        DeviceHeader deviceHeader = new DeviceHeader(device);
        String deviceHeaderName = device.name.toLowerCase() + ".h";
        writeFile(new File(deviceDir, deviceHeaderName), deviceHeader.generate());
    }

    private void writeFile(File file, String content) throws IOException
    {
        try (FileWriter writer = new FileWriter(file))
        {
            writer.write(content);
        }
    }
}
