package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

// Helper class that represents a file that has already been generated
public class GeneratedFile extends CandidateFile
{
    URI fileUri;

    public GeneratedFile(String packageName, String className, URI fileUri)
    {
        super(packageName, className);
        this.fileUri = fileUri;
    }

    public File fullPath()
    {
        return new File(fileUri);
    }

    public void relocateTo(File targetDir) throws IOException
    {
        File srcPath = fullPath();
        File targetPath = fullPath(targetDir);

        File packageDir = packageDir(targetDir);
        //todo : check dir creation status
        packageDir.mkdirs();

        byte[] srcBytes = readFile(srcPath);
        copyIfChanged(srcBytes, targetPath);

        //srcPath.delete();

    }

    public void copyIfChanged(byte[] src, File outFile) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(outFile);
        fout.write(src);
        fout.close();
    }

    private byte[] readFile(File file) throws IOException
    {
        int length = (int) file.length();
        FileInputStream fis = new FileInputStream(file);
        byte[] result = new byte[length];
        int pos = 0;
        while (pos < length)
        {
            pos += fis.read(result, pos, length - pos);
        }
        fis.close();
        return result;
    }
}