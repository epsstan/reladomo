package com.gs.fw.common.mithra.generator.annotationprocessor;

import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.StringFileObject;
import com.gs.fw.common.mithra.generator.annotationprocessor.compiler.TestJavaCompiler;
import com.gs.fw.common.mithra.generator.annotationprocessor.processor.LegacyAnnotationProcessor;
import com.gs.fw.common.mithra.generator.annotationprocessor.processor.ReladomoAnnotationProcessor;
import com.gs.fw.common.mithra.generator.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestReladomoAnnotationProcessor
{
    private File targetGeneratedSrcDir;
    private File userSrcDir;
    private File targetClassesDir;
    private File tempDir;

    @Before
    public void setup() throws IOException
    {
        tempDir = new File(System.getProperty("java.io.tmpdir"));
        tempDir.mkdirs();

        userSrcDir = new File(tempDir, "src/main/java");
        userSrcDir.mkdirs();

        targetGeneratedSrcDir = new File(tempDir, "target/generated-src");
        targetGeneratedSrcDir.mkdirs();

        targetClassesDir = new File(tempDir, "target/classes");
        targetClassesDir.mkdirs();
    }

    @After
    public void cleanup()
    {
        FileUtils.deleteDir(tempDir);
    }

    private StringFileObject addFileToUserSrcDir(String srcPath, String targetPath, String className) throws URISyntaxException, IOException
    {
        String resourceText = FileUtils.readFileAsString(loadFile(srcPath));
        File file = new File(userSrcDir, targetPath);
        FileUtils.createFile(resourceText, file);
        StringFileObject classListFileObject = new StringFileObject(className, resourceText);
        return classListFileObject;
    }

    private List<JavaFileObject> stageTestFiles() throws URISyntaxException, IOException
    {
        List<JavaFileObject> javaFileObjectList = new ArrayList<JavaFileObject>();
        for (String name : new String[] {"CustomerAccountSpec", "CustomerSpec",
                "CustomerDomainListSpec", "ExampleGeneratorsSpec", "TimestampProvider"})
        {
            javaFileObjectList.add(addFileToUserSrcDir(
                    "/annotations_set1/" +  name + ".java",
                    "annotations_set1/" +  name + ".java",
                    name)
            );
        }
        return javaFileObjectList;
    }

    private File loadFile(String classpathResourcePath) throws URISyntaxException
    {
        URL resource = TestReladomoAnnotationProcessor.class.getResource(classpathResourcePath);
        return new File(resource.toURI());
    }

    @Test
    public void testGeneration() throws URISyntaxException, IOException
    {
        List<JavaFileObject> compilationUnits = stageTestFiles();

        ReladomoAnnotationProcessor processor = new ReladomoAnnotationProcessor();
        TestJavaCompiler compiler = new TestJavaCompiler(compilationUnits, processor, userSrcDir, targetGeneratedSrcDir, targetClassesDir);
        Boolean compilationStatus = compiler.compile();
        assertEquals(true, compilationStatus);
    }

    private List<JavaFileObject> readSrcFiles() throws URISyntaxException, IOException
    {
        File file1 = new File(userSrcDir, "simplebank/specs/MyClassListSpec.java");
        File file2 = new File(userSrcDir, "simplebank/domain/Customer.java");
        File file3 = new File(userSrcDir, "simplebank/domain/CustomerAccount.java");

        StringFileObject obj1 = new StringFileObject("MyClassListSpec", FileUtils.readFileAsString(file1));
        StringFileObject obj2 = new StringFileObject("Customer", FileUtils.readFileAsString(file2));
        StringFileObject obj3 = new StringFileObject("CustomerAccount", FileUtils.readFileAsString(file3));

        List<JavaFileObject> javaFileObjectList = new ArrayList<JavaFileObject>();
        javaFileObjectList.add(obj1);
        javaFileObjectList.add(obj2);
        javaFileObjectList.add(obj3);

        return javaFileObjectList;
    }

    @Test
    public void testConsecutiveGeneration() throws URISyntaxException, IOException
    {
        List<JavaFileObject> compilationUnits = stageTestFiles();

        //compile for the first time
        long generatedCustomerModificationTime = compile(compilationUnits);

        //clean the classes and generated srcs from the previous compile
        FileUtils.deleteFilesInDir(targetClassesDir);
        FileUtils.deleteFilesInDir(targetGeneratedSrcDir);

        //compile for the second time - include the new src files generated from the first compilatiom
        compilationUnits = readSrcFiles();

        long generatedCustomerModificationTime2 = compile(compilationUnits);

        assertEquals(generatedCustomerModificationTime, generatedCustomerModificationTime2);
    }

    private long compile(List<JavaFileObject> compilationUnits)
    {
        LegacyAnnotationProcessor processor = new LegacyAnnotationProcessor();
        TestJavaCompiler compiler = new TestJavaCompiler(compilationUnits, processor, userSrcDir, targetGeneratedSrcDir, targetClassesDir);
        Boolean compilationStatus = compiler.compile();
        assertEquals(true, compilationStatus);

        File generatedCustomer = new File(userSrcDir, "simplebank/domain/Customer.java");
        generatedCustomer.exists();
        return  generatedCustomer.lastModified();
    }
}
