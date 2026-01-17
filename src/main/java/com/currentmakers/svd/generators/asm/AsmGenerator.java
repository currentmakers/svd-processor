package com.currentmakers.svd.generators.asm;

import com.currentmakers.svd.generators.GenerationOptions;
import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Peripheral;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
        for(Device device : devices)
        {
            generateDevice(device);
        }
    }

    private void generateDevice(Device device) throws IOException
    {
        // Create device directory
        String deviceDirName = device.name.toLowerCase();
        File deviceDir = new File(rootDirectory, deviceDirName);
        if(!deviceDir.exists())
        {
            deviceDir.mkdirs();
        }

        // Generate peripheral headers (only for representative peripherals)
        for(Peripheral peripheral : device.peripherals)
        {
            PeripheralEquates peripheralEquates = new PeripheralEquates(peripheral);
            String fileName = peripheral.name.toLowerCase() + ".s";
            writeFile(new File(deviceDir, fileName), peripheralEquates.generate());
        }

        // Generate main device header (umbrella)
        DeviceEquates deviceEquates = new DeviceEquates(device);
        String deviceHeaderName = device.name.toLowerCase() + ".s";
        writeFile(new File(deviceDir, deviceHeaderName), deviceEquates.generate());
        String commonContent = new CommonGenerator().generate();
        writeFile(new File(deviceDir, "../common.s"), commonContent);
    }


    private void writeFile(File file, String content) throws IOException
    {
        try(FileWriter writer = new FileWriter(file))
        {
            writer.write(content);
            writer.write("\n.ltorg\n");
        }
    }
}