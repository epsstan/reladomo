package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import java.io.File;

// Helper class that represents a file that needs to be created
public class CandidateFile
{
    private String packageName;
    private String className;

    public CandidateFile(String packageName, String className)
    {
        this.packageName = packageName;
        this.className = className;
    }

    public boolean existsInDir(File dir)
    {
        return fullPath(dir).exists();
    }

    public File packageDir(File base)
    {
        String withSlashes = packageName.replaceAll("\\.", File.separator);
        return new File(base, withSlashes);
    }

    public File fullPath(File dir)
    {
        String withSlashes = packageName.replaceAll("\\.", File.separator);
        String child = withSlashes + File.separatorChar + className;
        return new File(dir, child);
    }
}
