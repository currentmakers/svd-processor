package com.currentmakers.svd;

import com.currentmakers.svd.generators.GenerationOptions;
import com.currentmakers.svd.generators.asm.AsmGenerator;
import com.currentmakers.svd.generators.c.CGenerator;
import com.currentmakers.svd.generators.forth.ForthGenerator;
import com.currentmakers.svd.parser.Device;
import com.currentmakers.svd.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            CommandLineArgs cmdArgs = parseCommandLine(args);
            
            if (cmdArgs == null)
            {
                printUsage();
                System.exit(1);
            }

            // Parse all SVD files
            System.out.println("Parsing SVD files...");
            List<Device> devices = new ArrayList<>();
            for (File svdFile : cmdArgs.svdFiles)
            {
                System.out.println("  - " + svdFile.getName());
                Parser parser = new Parser(svdFile);
                devices.add(parser.device);
            }

            // Generate code based on language
            System.out.println("\nGenerating " + cmdArgs.language + " headers...");
            switch (cmdArgs.language.toLowerCase())
            {
                case "c":
                    GenerationOptions cOptions = new GenerationOptions();
                    CGenerator cgenerator = new CGenerator(cmdArgs.outputDir, cOptions, devices);
                    cgenerator.generate();
                    break;
                case "asm":
                    GenerationOptions asmOptions = new GenerationOptions();
                    AsmGenerator asmGenerator = new AsmGenerator(cmdArgs.outputDir, asmOptions, devices);
                    asmGenerator.generate();
                    break;
                case "forth":
                    GenerationOptions forthOptions = new GenerationOptions();
                    ForthGenerator forthGenerator = new ForthGenerator(cmdArgs.outputDir, forthOptions, devices);
                    forthGenerator.generate();
                    break;
                default:
                    System.err.println("Error: Unsupported language '" + cmdArgs.language + "'");
                    System.exit(1);
            }

            System.out.println("\nGeneration completed successfully!");
            System.out.println("Output directory: " + cmdArgs.outputDir.getAbsolutePath());
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static CommandLineArgs parseCommandLine(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            return null;
        }

        CommandLineArgs result = new CommandLineArgs();
        result.language = "c"; // Default language
        List<String> svdPaths = new ArrayList<>();

        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];

            switch (arg)
            {
                case "-l", "--language":
                    if (i + 1 >= args.length)
                    {
                        System.err.println("Error: Missing value for " + arg);
                        return null;
                    }
                    result.language = args[++i];
                    break;

                case "-o", "--output":
                    if (i + 1 >= args.length)
                    {
                        System.err.println("Error: Missing value for " + arg);
                        return null;
                    }
                    result.outputDir = new File(args[++i]);
                    break;

                case "-d", "--directory":
                    if (i + 1 >= args.length)
                    {
                        System.err.println("Error: Missing value for " + arg);
                        return null;
                    }
                    File dir = new File(args[++i]);
                    if (!dir.isDirectory())
                    {
                        System.err.println("Error: Not a directory: " + dir.getAbsolutePath());
                        return null;
                    }
                    // Find all .svd files in the directory
                    List<String> svdFiles = findFiles(dir);
                    svdPaths.addAll(svdFiles);
                    break;

                case "-f", "--file":
                    if (i + 1 >= args.length)
                    {
                        System.err.println("Error: Missing value for " + arg);
                        return null;
                    }
                    svdPaths.add(args[++i]);
                    break;

                case "-h", "--help":
                    return null;

                default:
                    // Assume it's an SVD file path if no flag is specified
                    if (!arg.startsWith("-"))
                    {
                        svdPaths.add(arg);
                    }
                    else
                    {
                        System.err.println("Error: Unknown option: " + arg);
                        return null;
                    }
                    break;
            }
        }

        // Validate required arguments
        if (result.outputDir == null)
        {
            System.err.println("Error: Output directory is required (-o/--output)");
            return null;
        }

        if (svdPaths.isEmpty())
        {
            System.err.println("Error: At least one SVD file or directory is required");
            return null;
        }

        // Convert SVD paths to File objects
        result.svdFiles = new ArrayList<>();
        for (String path : svdPaths)
        {
            File file = new File(path);
            if (!file.exists())
            {
                System.err.println("Error: File not found: " + path);
                return null;
            }
            if (!file.isFile())
            {
                System.err.println("Error: Not a file: " + path);
                return null;
            }
            result.svdFiles.add(file);
        }

        // Create output directory if it doesn't exist
        if (!result.outputDir.exists())
        {
            result.outputDir.mkdirs();
        }

        return result;
    }

    private static List<String> findFiles(File dir) throws IOException {
        try (Stream<Path> pathStream = Files.find(dir.toPath(), Integer.MAX_VALUE, (path, attrs) -> {
            // Check if it's a file AND ends with the extension
            return attrs.isRegularFile() &&
                    path.getFileName().toString().endsWith(".svd");
        })) {
            return pathStream
                    .map(p -> p.toAbsolutePath().toString())
                    .toList();
        }
    }

    private static void printUsage()
    {
        System.out.println("SVD Header Generator");
        System.out.println();
        System.out.println("Usage: java -jar svd-processor.jar [options] <svd-files>");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -l, --language <lang>     Target language (default: c)");
        System.out.println("                            Supported: c");
        System.out.println();
        System.out.println("  -o, --output <dir>        Output directory (required)");
        System.out.println();
        System.out.println("  -d, --directory <dir>     Directory containing SVD files");
        System.out.println("                            (processes all .svd files in directory)");
        System.out.println();
        System.out.println("  -f, --file <file>         Single SVD file to process");
        System.out.println("                            (can be specified multiple times)");
        System.out.println();
        System.out.println("  -h, --help                Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  # Process a single SVD file");
        System.out.println("  java -jar svd-processor.jar -o output -f STM32F407.svd");
        System.out.println();
        System.out.println("  # Process all SVD files in a directory");
        System.out.println("  java -jar svd-processor.jar -o output -d svd_files/");
        System.out.println();
        System.out.println("  # Process multiple specific files");
        System.out.println("  java -jar svd-processor.jar -o output -f STM32F407.svd -f STM32F103.svd");
        System.out.println();
        System.out.println("  # Specify language explicitly");
        System.out.println("  java -jar svd-processor.jar -l c -o output -d svd_files/");
        System.out.println();
        System.out.println("  # Short form (files without flags)");
        System.out.println("  java -jar svd-processor.jar -o output STM32F407.svd STM32F103.svd");
    }

    private static class CommandLineArgs
    {
        String language;
        File outputDir;
        List<File> svdFiles;
    }
}
