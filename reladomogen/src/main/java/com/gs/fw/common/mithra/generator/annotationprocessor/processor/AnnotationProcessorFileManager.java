package com.gs.fw.common.mithra.generator.annotationprocessor.processor;


import com.gs.fw.common.mithra.generator.filesystem.GeneratedFileManager;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AnnotationProcessorFileManager implements GeneratedFileManager
{
    private final Filer filer;
    private final File userSrcDir;
    private Options options;
    private List<GeneratedFile> generatedFiles = new ArrayList<GeneratedFile>();

    public AnnotationProcessorFileManager(Filer filer, File userSrcDir)
    {
        this.filer = filer;
        this.userSrcDir = userSrcDir;
    }

    public List<GeneratedFile> getGeneratedFiles()
    {
        return generatedFiles;
    }

    @Override
    public void setOptions(Options options)
    {
        this.options = options;
    }

    @Override
    public boolean shouldCreateFile(boolean replaceIfExists, String packageName, String className, String fileSuffix)
    {
        return true;
    }

    @Override
    public void writeFile(boolean replaceIfExists, String packageName, String className, String fileSuffix, byte[] fileData, AtomicInteger count) throws IOException
    {
        if (replaceIfExists)
        {
            createFile(packageName, className, fileSuffix, fileData);
        }
        else
        {
            createFileIfNotInUserSrcDir(packageName, className, fileSuffix, fileData);
        }
    }

    @Override
    public byte[] readFileInGeneratedDir(String relativePath) throws IOException
    {
        return new byte[0];
    }

    private GeneratedFile createFile(String packageName, String className, String fileSuffix, byte[] fileData) throws IOException
    {
        //todo : fix / in package name at call site
        String fixedPackageName = packageName.replaceAll("/", ".");
        String fixedClassName = className.replaceAll("\\.", "");
        String fixedFileSuffix = fileSuffix.replaceAll("\\.java", "");
        String fileName = fixedPackageName.isEmpty() ? fixedClassName + fixedFileSuffix: fixedPackageName + "." + fixedClassName + fixedFileSuffix;

        if (!fileSuffix.contains(".log"))
        {
            JavaFileObject sourceFile = filer.createSourceFile(fileName);
            Writer writer = sourceFile.openWriter();
            writer.write(new String(fileData));
            writer.close();
            return new GeneratedFile(packageName, className + fileSuffix, sourceFile.toUri());
        }
        else
        {
            filer.createResource(StandardLocation.CLASS_OUTPUT, "", fileName);
            return null;
        }
    }

    private void createFileIfNotInUserSrcDir(String packageName, String className, String fileSuffix, byte[] fileData) throws IOException
    {
        CandidateFile candidateFile = new CandidateFile(packageName, className + fileSuffix);
        if (candidateFile.existsInDir(this.userSrcDir))
        {
            //todo : log to filer
            return;
        }
        GeneratedFile generatedFile = createFile(packageName, className, fileSuffix, fileData);
        this.generatedFiles.add(generatedFile);
    }


}
