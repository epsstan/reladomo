package com.gs.fw.common.mithra.generator.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils
{
    public static void createFile(byte[] src, File outFile) throws IOException
    {
        FileOutputStream fout = new FileOutputStream(outFile);
        fout.write(src);
        fout.close();
    }

    public static void createFile(String src, File outFile) throws IOException
    {
        createParentDirs(outFile);
        createFile(src.getBytes(), outFile);
    }

    public static void copyFile(File src, File dest) throws IOException
    {
        byte[] bytes = readFile(src);
        createFile(bytes, dest);
    }

    public static String readFileAsString(File file) throws IOException
    {
        return new String(readFile(file));
    }

    public static byte[] readFile(File file) throws IOException
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

    public static void createParentDirs(File file)
    {
        String prefix = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(File.separator));
        new File(prefix).mkdirs();
    }

    public static void deleteDir(File file)
    {
        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                deleteDir(child);
            }
        }
        file.delete();
    }

    public static void deleteFilesInDir(File file)
    {
        for (File child : file.listFiles())
        {
            deleteDir(child);
        }
    }
}
