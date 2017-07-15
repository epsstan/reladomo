
/*
 Copyright 2016 Goldman Sachs.
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
 */

package com.gs.fw.common.mithra.generator;

import com.gs.fw.common.mithra.generator.writer.GeneratedFileManager;
import org.apache.tools.ant.BuildException;

import java.io.FileNotFoundException;

public class MithraGenerator extends AbstractMithraGenerator
{
    private CoreMithraGenerator coreGenerator;

    public MithraGenerator()
    {
        this(new CoreMithraGenerator());
    }

    public MithraGenerator(CoreMithraGenerator gen)
    {
        super(gen);
        this.coreGenerator = gen;
    }

    public boolean isGenerateImported()
    {
        return this.coreGenerator.isGenerateImported();
    }

    public void setGenerateImported(boolean generateImported)
    {
        this.coreGenerator.setGenerateImported(generateImported);
    }

    public void setGenerateConcreteClasses(boolean generateConcreteClasses)
    {
        this.coreGenerator.setGenerateConcreteClasses(generateConcreteClasses);
    }

    public void setWarnAboutConcreteClasses(boolean warnAboutConreteClasses)
    {
        this.coreGenerator.setWarnAboutConcreteClasses(warnAboutConreteClasses);
    }

    public void setGenerateGscListMethod(boolean generateGscListMethod)
    {
        this.coreGenerator.setGenerateGscListMethod(generateGscListMethod);
    }

    @Deprecated
    public void setGenerateLegacyCaramel(boolean generateLegacyCaramel)
    {
        this.coreGenerator.setGenerateLegacyCaramel(generateLegacyCaramel);
    }

    public void setCodeFormat(String format)
    {
        this.coreGenerator.setCodeFormat(format);
    }

    public void execute() throws BuildException
	{
        this.coreGenerator.execute();
	}

    public void setGenerateFileHeaders(boolean generateFileHeaders)
    {
        this.coreGenerator.setGenerateFileHeaders(generateFileHeaders);
    }

    public void setMithraObjectTypeParser(MithraObjectTypeParser mithraObjectTypeParser)
    {
        this.coreGenerator.setMithraObjectTypeParser(mithraObjectTypeParser);
    }

    public void setGeneratedFileManager(GeneratedFileManager generatedFileManager)
    {
        this.coreGenerator.setGeneratedFileManager(generatedFileManager);
    }

    public void parseAndValidate() throws FileNotFoundException
    {
        this.coreGenerator.parseAndValidate();
    }

    public static void main(String[] args)
    {
        MithraGenerator gen = new MithraGenerator() {
            public void log(String s)
            {
                System.out.println(s);
            }

            public void log(String s, int i)
            {
                System.out.println(s);
            }
        };

        /*
        MithraObjectTypeParser parser = new MithraXMLObjectTypeParser("H:/projects/Mithra/Mithra/xml/mithra/test/MithraClassList.xml");

        gen.setMithraObjectTypeParser(parser);
        gen.setGeneratedDir("H:/temp/Mithra/src");
        gen.setNonGeneratedDir("H:/temp/Mithra/src");
        gen.setGenerateGscListMethod(true);
        gen.setCodeFormat(CoreMithraGenerator.FORMAT_FAST);
        gen.setDefaultFinalGetters(true);

        long startTime = System.currentTimeMillis();

        MithraGeneratorImport generatorImport = new MithraGeneratorImport();
        generatorImport.setDir("H:/projects/Mithra/Mithra/xml/mithra/test/");
        generatorImport.setFilename("MithraClassListToImport.xml");
        gen.addConfiguredMithraImport(generatorImport);

        generatorImport = new MithraGeneratorImport();
        generatorImport.setDir("H:/projects/Mithra/Mithra/xml/mithra/test/testmithraimport");
        generatorImport.setFilename("MithraTestImportClassList.xml");
        gen.addConfiguredMithraImport(generatorImport);

        gen.execute();
        System.out.println("time: "+(System.currentTimeMillis() - startTime));
        */

        MithraObjectTypeParser parser = new MithraXMLObjectTypeParser("/tmp/new-atp/xmls/SimpleBankClassList.xml");

        gen.setMithraObjectTypeParser(parser);
        gen.setGeneratedDir("/tmp/new-atp/abstract-src");
        gen.setNonGeneratedDir("/tmp/new-atp/concrete-src");
        gen.setGenerateGscListMethod(true);
        gen.setCodeFormat(CoreMithraGenerator.FORMAT_FAST);

        /*
        MithraGeneratorImport generatorImport = new MithraGeneratorImport();
        //generatorImport.setDir("/Users/stanle/github_ws/reladomo/reladomo/src/test/reladomo-xml");
        generatorImport.setDir("/Users/stanle/github_ws/reladomo_stanle.git/reladomo/src/test/reladomo-xml/");
        generatorImport.setFilename("MithraClassListToImport.xml");
        gen.addConfiguredMithraImport(generatorImport);

        generatorImport = new MithraGeneratorImport();
        //generatorImport.setDir("H:/projects/Mithra/Mithra/xml/mithra/test/testmithraimport");
        generatorImport.setDir("/Users/stanle/github_ws/reladomo_stanle.git/reladomo/src/test/reladomo-xml/testmithraimport");
        generatorImport.setFilename("MithraTestImportClassList.xml");
        gen.addConfiguredMithraImport(generatorImport);
        */

        gen.execute();


    }
}