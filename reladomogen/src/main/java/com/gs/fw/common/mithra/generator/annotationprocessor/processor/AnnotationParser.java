package com.gs.fw.common.mithra.generator.annotationprocessor.processor;

import com.gs.fw.common.mithra.generator.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.interfaces.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.AsOfAttribute;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.*;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Index;
import com.gs.fw.common.mithra.generator.annotationprocessor.annotations.object.Properties;
import com.gs.fw.common.mithra.generator.filesystem.FauxFile;
import com.gs.fw.common.mithra.generator.filesystem.FauxFileSystem;
import com.gs.fw.common.mithra.generator.metamodel.*;
import com.gs.fw.common.mithra.generator.util.*;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.zip.CRC32;

import static org.javacc.parser.JavaCCGlobals.fileName;

public class AnnotationParser implements MithraObjectTypeParser
{
    private final File fakeClassList;
    private final Trees trees;
    private CRC32 crc32 = new CRC32();

    private static final int IO_THREADS = 1;
    private AwaitingThreadExecutor executor;
    private Throwable executorError;
    private ChopAndStickResource chopAndStickResource = new ChopAndStickResource(new Semaphore(Runtime.getRuntime().availableProcessors()),
            new Semaphore(IO_THREADS), new SerialResource());

    private Map<String, MithraObjectTypeWrapper> mithraObjects = new ConcurrentHashMap<String, MithraObjectTypeWrapper>();
    private List<MithraObjectTypeWrapper> sortedMithraObjects;
    private Map<String, MithraEmbeddedValueObjectTypeWrapper> mithraEmbeddedValueObjects = new ConcurrentHashMap<String, MithraEmbeddedValueObjectTypeWrapper>();
    private List<MithraEmbeddedValueObjectTypeWrapper> sortedMithraEmbeddedValueObjects;
    private Map<String, MithraEnumerationTypeWrapper> mithraEnumerations = new ConcurrentHashMap<String, MithraEnumerationTypeWrapper>();
    private List<MithraEnumerationTypeWrapper> sortedMithraEnumerations;
    private Map<String, MithraInterfaceType> mithraInterfaces = new ConcurrentHashMap<String, MithraInterfaceType>();

    private boolean generateFileHeaders = false;
    private boolean ignoreNonGeneratedAbstractClasses = false;
    private boolean ignoreTransactionalMethods = false;
    private boolean ignorePackageNamingConvention = false;
    private boolean defaultFinalGetters = false;
    private boolean forceOffHeap = false;

    private ThreadLocal<FullFileBuffer> fullFileBufferThreadLocal = new ThreadLocal<FullFileBuffer>();

    private Logger logger;

    private Types typeUtils;
    private Elements elementUtils;
    private ReladomoListSpecWrapper reladomoListSpec;
    private FauxFileSystem fauxFileSystem;

    public AnnotationParser(Types typeUtils, Elements elementUtils, Trees trees, ReladomoListSpecWrapper reladomoListSpec, File fakeClassList)
    {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.reladomoListSpec = reladomoListSpec;
        this.fakeClassList = fakeClassList;
        this.trees = trees;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void setForceOffHeap(boolean forceOffHeap)
    {
        this.forceOffHeap = forceOffHeap;
    }

    @Override
    public void setDefaultFinalGetters(boolean defaultFinalGetters)
    {
        this.defaultFinalGetters = defaultFinalGetters;
    }

    @Override
    public void setFauxFileSystem(FauxFileSystem fauxFileSystem)
    {
        this.fauxFileSystem = fauxFileSystem;
    }

    public Map<String, MithraObjectTypeWrapper> getMithraObjects()
    {
        return this.mithraObjects;
    }

    public Map<String, MithraEmbeddedValueObjectTypeWrapper> getMithraEmbeddedValueObjects()
    {
        return this.mithraEmbeddedValueObjects;
    }

    public Map<String, MithraEnumerationTypeWrapper> getMithraEnumerations()
    {
        return this.mithraEnumerations;
    }

    @Override
    public String getChecksum()
    {
        return null;
    }

    public Map<String, MithraInterfaceType> getMithraInterfaces()
    {
        return mithraInterfaces;
    }

    public String parse() throws MithraGeneratorException
    {
        try
        {
            parseAnnotations();
            FauxFile file = this.fauxFileSystem.newFile(fakeClassList.getPath());
            return file.getPath();
        } catch (Throwable e)
        {
            throw new MithraGeneratorException(e);
        }
    }

    public void parseAnnotations()
    {
        try
        {
            //todo : fix logger
            //this.logger.debug(obj.getClass().getName() + ": " + MithraType.class.getName());

            long start = System.currentTimeMillis();
            int normalObjects = this.parseMithraObjects();
            int pureObjects = this.parseMithraPureObjects();
            int tempObjects = this.parseMithraTempObjects();
            int embeddedObjects = this.parseMithraEmbeddedValueObjects();
            int enumerations = this.parseMithraEnumerations();
            int mithraInterfaceObjects = this.parseMithraInterfaceObjects();
            //todo : use messager
            String msg = ": parsed ";
            msg = concatParsed(msg, normalObjects, "normal");
            msg = concatParsed(msg, pureObjects, "pure");
            msg = concatParsed(msg, tempObjects, "temp");
            msg = concatParsed(msg, embeddedObjects, "embedded");
            msg = concatParsed(msg, enumerations, "enumeration");
            msg = concatParsed(msg, mithraInterfaceObjects, "mithraInterface");
            msg += " Mithra objects in " + (System.currentTimeMillis() - start) + " ms.";
            this.logger.info(msg);
        } catch (MithraGeneratorParserException e)
        {
            throw new MithraGeneratorException("Unable to parse " + fileName, e);
        } catch (IOException e)
        {
            throw new MithraGeneratorException("Unable to read file " + fileName, e);
        }
    }

    private int parseMithraObjects() throws FileNotFoundException
    {
        final ObjectResource[] objectResources = reladomoListSpec.resources();
        chopAndStickResource.resetSerialResource();
        for (int i = 0; i < objectResources.length; i++)
        {
            ObjectResource resource = objectResources[i];
            final ObjectResourceWrapper objectResource = new ObjectResourceWrapper(reladomoListSpec, resource,
                    elementUtils, typeUtils, trees);
            getExecutor().submit(new GeneratorTask(i)
            {
                public void run()
                {
                    MithraObject mithraObject = processReladomoObject(objectResource);

                    String name = objectResource.getName();
                    String objectFileName = objectResource.getSpecName();

                    boolean isGenerateInterfaces = !objectResource.isGenerateInterfacesSet() ? reladomoListSpec.isGenerateInterfacesSet() : objectResource.generateInterfaces();
                    boolean enableOffHeap = !objectResource.isEnableOffHeapSet() ? reladomoListSpec.isEnableOffHeapSet() || forceOffHeap : objectResource.enableOffHeap();
                    MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, null, isGenerateInterfaces, ignorePackageNamingConvention, AnnotationParser.this.logger);
                    wrapper.setGenerateFileHeaders(generateFileHeaders);
                    wrapper.setReplicated(objectResource.replicated());
                    wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                    wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                    wrapper.setReadOnlyInterfaces(objectResource.isReadOnlyInterfacesSet() ? objectResource.readOnlyInterfaces() : reladomoListSpec.readOnlyInterfaces());
                    wrapper.setDefaultFinalGetters(defaultFinalGetters);
                    wrapper.setEnableOffHeap(enableOffHeap);
                    mithraObjects.put(name, wrapper);
                }
            });
        }
        waitForExecutorWithCheck();
        return objectResources.length;
    }

    private int parseMithraPureObjects()
    {
        /*
        List<MithraPureObjectResourceType> mithraPureObjectList = mithraType.getMithraPureObjectResources();
        if (!mithraPureObjectList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0; i< mithraPureObjectList.size();i++)
            {
                final MithraPureObjectResourceType mithraPureObjectResourceType = mithraPureObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraPureObjectResourceType.getName();
                        MithraBaseObjectType mithraObject = parseMithraObject(objectName, mithraObjects, fileProvider, this, "pure");
                        if (mithraObject != null)
                        {
                            String objectFileName = objectName + ".xml";
                            boolean enableOffHeap = !mithraPureObjectResourceType.isEnableOffHeapSet() ? mithraType.isEnableOffHeap() || forceOffHeap : mithraPureObjectResourceType.isEnableOffHeap();
                            MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, importSource, false, ignorePackageNamingConvention, MithraXMLObjectTypeParser.this.logger);
                            wrapper.setGenerateFileHeaders(generateFileHeaders);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                            wrapper.setPure(true);
                            wrapper.setEnableOffHeap(enableOffHeap);
                            mithraObjects.put(mithraPureObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraPureObjectList.size();
        */
        return 0;
    }

    private int parseMithraTempObjects()
    {
        /*
        List<MithraTempObjectResourceType> mithraTempObjectList = mithraType.getMithraTempObjectResources();
        if (mithraTempObjectList.size() > 0)
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0; i<mithraTempObjectList.size();i++)
            {
                final MithraTempObjectResourceType mithraTempObjectResourceType = mithraTempObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraTempObjectResourceType.getName();
                        MithraBaseObjectType mithraObject = parseMithraObject(objectName, mithraObjects, fileProvider, this, "temp");
                        if (mithraObject != null)
                        {
                            String objectFileName = objectName + ".xml";
                            MithraObjectTypeWrapper wrapper = new MithraObjectTypeWrapper(mithraObject, objectFileName, importSource, false, ignorePackageNamingConvention, MithraXMLObjectTypeParser.this.logger);
                            wrapper.setGenerateFileHeaders(generateFileHeaders);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            wrapper.setIgnoreTransactionalMethods(ignoreTransactionalMethods);
                            wrapper.setTemporary(true);
                            mithraObjects.put(mithraTempObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraTempObjectList.size();
        */
        return 0;
    }

    private int parseMithraEmbeddedValueObjects()
    {
        /*
        List<MithraEmbeddedValueObjectResourceType> mithraEmbeddedValueObjectList = mithraType.getMithraEmbeddedValueObjectResources();
        if (!mithraEmbeddedValueObjectList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0;i<mithraEmbeddedValueObjectList.size();i++)
            {
                final MithraEmbeddedValueObjectResourceType mithraEmbeddedValueObjectResourceType = mithraEmbeddedValueObjectList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String objectName = mithraEmbeddedValueObjectResourceType.getName();
                        MithraEmbeddedValueObjectType evo = (MithraEmbeddedValueObjectType) parseMithraBaseObject(objectName, mithraEmbeddedValueObjects, fileProvider, this);
                        if (evo != null)
                        {
                            String objectFileName = objectName + ".xml";
                            MithraEmbeddedValueObjectTypeWrapper wrapper = new MithraEmbeddedValueObjectTypeWrapper(evo, objectFileName, importSource);
                            wrapper.setIgnoreNonGeneratedAbstractClasses(ignoreNonGeneratedAbstractClasses);
                            mithraEmbeddedValueObjects.put(mithraEmbeddedValueObjectResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraEmbeddedValueObjectList.size();
        */
        return 0;
    }

    private int parseMithraEnumerations()
    {
        /*
        List<MithraEnumerationResourceType> mithraEnumerationList = (List<MithraEnumerationResourceType>) mithraType.getMithraEnumerationResources();
        if (!mithraEnumerationList.isEmpty())
        {
            chopAndStickResource.resetSerialResource();
            for (int i=0;i<mithraEnumerationList.size();i++)
            {
                final MithraEnumerationResourceType mithraEnumerationResourceType = mithraEnumerationList.get(i);
                getExecutor().submit(new GeneratorTask(i)
                {
                    public void run()
                    {
                        String enumerationName = mithraEnumerationResourceType.getName();
                        MithraEnumerationType enumeration = (MithraEnumerationType) parseMithraBaseObject(enumerationName, mithraEnumerations, fileProvider, this);
                        if (enumeration != null)
                        {
                            String enumerationFileName = enumerationName + ".xml";
                            MithraEnumerationTypeWrapper wrapper = new MithraEnumerationTypeWrapper(enumeration, enumerationFileName, importSource);
                            mithraEnumerations.put(mithraEnumerationResourceType.getName(), wrapper);
                        }
                    }
                });
            }
            waitForExecutorWithCheck();
        }
        return mithraEnumerationList.size();
        */
        return 0;
    }

    private MithraInterfaceType parseMithraInterfaceType()
    {
        /*
        MithraInterfaceType mithraObject = (MithraInterfaceType) parseMithraType(objectName, objectMap, fileProvider, task);
        mithraObject.setReadOnlyInterfaces(isReadOnlyInterfaces);
        mithraObject.setImportedSource(importedSource);
        checkClassName(objectName, mithraObject.getClassName());
        mithraObject.postInitialize(objectName);
        return mithraObject;
        */
        return null;
    }

    private MithraBaseObjectType parseMithraObject()
    {
        //return parseMithraBaseObject(objectName, objectMap, fileProvider, task);
        return null;
    }

    private void checkClassName(String objectName, String className)
    {
        if (objectName.contains("/"))
        {
            objectName = objectName.substring(objectName.lastIndexOf('/') + 1);
        }
        if (!objectName.equals(className))
        {
            throw new MithraGeneratorException("XML filename: '" + objectName + "' must match class name specified: '" + className + "'");
        }
    }

    private int parseMithraInterfaceObjects() throws FileNotFoundException
    {
        InterfaceResource[] interfaceResources = reladomoListSpec.interfaces();
        chopAndStickResource.resetSerialResource();
        for (int i = 0; i < interfaceResources.length; i++)
        {
            InterfaceResource resource = interfaceResources[i];
            final InterfaceResourceWrapper interfaceResource = new InterfaceResourceWrapper(reladomoListSpec, resource, typeUtils);
            getExecutor().submit(new GeneratorTask(i)
            {
                public void run()
                {
                    String name = interfaceResource.getName();
                    MithraInterfaceType mithraObject = processReladomoInterface(interfaceResource);
                    if (mithraObject != null)
                    {
                        mithraInterfaces.put(name, mithraObject);
                    }
                }
            });
        }
        waitForExecutorWithCheck();
        return interfaceResources.length;
    }

    private MithraInterfaceType processReladomoInterface(InterfaceResourceWrapper interfaceResource)
    {
        ReladomoInterface spec = interfaceResource.getReladomoInterface();
        MithraInterfaceType mithraInterfaceType = new MithraInterfaceType();
        mithraInterfaceType.setPackageName(spec.packageName());
        mithraInterfaceType.setReadOnlyInterfaces(interfaceResource.readOnlyInterfaces());
        mithraInterfaceType.setSourceFileName(interfaceResource.getSpecName());
        mithraInterfaceType.setImportedSource(interfaceResource.getSpecName());

        setImports(interfaceResource, mithraInterfaceType);
        setSuperInterfaces(interfaceResource, mithraInterfaceType);

        List<AsOfAttributeInterfaceType> asOfAttributeTypes = new ArrayList<AsOfAttributeInterfaceType>();
        List<AttributeInterfaceType> attributeTypes = new ArrayList<AttributeInterfaceType>();
        List<RelationshipInterfaceType> relationshipTypes = new ArrayList<RelationshipInterfaceType>();

        // todo : maybe convert this to use the visitor pattern ?
        ElementsByType elementsByType = partitionInterfaceElements(interfaceResource.getEnclosedElements());
        gatherInterfaceAttributes(asOfAttributeTypes, attributeTypes, elementsByType.attributes);
        gatherInterfaceRelationships(relationshipTypes, elementsByType.relationships);

        setInterfaceSourceAttribute(mithraInterfaceType, elementsByType);

        return mithraInterfaceType;
    }

    private void setInterfaceSourceAttribute(MithraInterfaceType mithraInterfaceType, ElementsByType elementsByType)
    {
        if (!elementsByType.sourceAttributes.isEmpty())
        {
            Element element = elementsByType.sourceAttributes.get(0);
            SourceAttributeInterfaceType sourceAttributeInterfaceType = makeInterfaceSourceAttribute(element, element.getSimpleName().toString());
            mithraInterfaceType.setSourceAttribute(sourceAttributeInterfaceType);
        }
    }

    private void setSuperInterfaces(InterfaceResourceWrapper interfaceResource, MithraInterfaceType mithraInterfaceType)
    {
        Class[] superInterfaces = interfaceResource.superInterfaces();
        if (superInterfaces != null)
        {
            List<String> superInterfaceNames = new ArrayList<String>();
            for (Class clazz : superInterfaces)
            {
                superInterfaceNames.add(clazz.getSimpleName());
            }
            mithraInterfaceType.setSuperInterfaces(superInterfaceNames);
        }
    }

    private void setImports(InterfaceResourceWrapper interfaceResource, MithraInterfaceType mithraInterfaceType)
    {
        String[] imports = interfaceResource.imports();
        if (imports != null)
        {
            mithraInterfaceType.setImports(Arrays.asList(imports));
        }
    }

    private Object parseMithraType()
    {
        /*
        Object mithraObject = null;
        this.logger.info("Reading " + objectName);
        if (!fileProvider.excludeObject(objectName))
        {
            if (objectMap.containsKey(objectName))
            {
                throw new MithraGeneratorException("Attempted to add object " + objectName + " twice");
            }
            String objectFileName = objectName + ".xml";
            InputStream objectFileIs = null;
            boolean serialAquired = false;
            try
            {
                MithraGeneratorImport.FileInputStreamWithSize streamWithSize = fileProvider.getFileInputStream(objectFileName);
                FullFileBuffer ffb = getFullFileBuffer();
                chopAndStickResource.acquireIoResource();
                try
                {
                    ffb.bufferFile(streamWithSize.getInputStream(), (int) streamWithSize.getSize());
                }
                finally
                {
                    chopAndStickResource.releaseIoResource();
                }
                task.acquireSerialResource();
                serialAquired = true;
                try
                {
                    ffb.updateCrc(crc32);
                }
                finally
                {
                    task.releaseSerialResource();
                }
                chopAndStickResource.acquireCpuResource();
                try
                {
                    objectFileIs = streamWithSize.getInputStream();
                    mithraObject = new MithraGeneratorUnmarshaller().parse(ffb.getBufferedInputStream(), objectFileName);
                }
                finally
                {
                    chopAndStickResource.releaseCpuResource();
                }
            }
            catch (FileNotFoundException e)
            {
                throw new MithraGeneratorException("Unable to find " + objectFileName, e);
            }
            catch (MithraGeneratorParserException e)
            {
                throw new MithraGeneratorException("Unable to parse " + objectFileName+" "+e.getMessage(), e);
            }
            catch (IOException e)
            {
                throw new MithraGeneratorException("Unable to read x" + objectFileName, e);
            }
            finally
            {
                if (!serialAquired)
                {
                    task.acquireSerialResource();
                    task.releaseSerialResource();
                }
                closeIs(objectFileIs);
            }
        }
        else
        {
            this.logger.info("Skipping " + objectName + ", excluded");
        }
        return mithraObject;
        */
        return null;
    }

    private MithraBaseObjectType parseMithraBaseObject()
    {
        /*
        MithraBaseObjectType mithraObject = (MithraBaseObjectType) parseMithraType(objectName, objectMap, fileProvider, task);
        checkClassName(objectName, mithraObject.getClassName());
        return mithraObject;
        */

        return null;
    }


    private String concatParsed(String msg, int count, String type)
    {
        if (count > 0)
        {
            msg += count + " " + type + ", ";
        }
        return msg;
    }

    private void waitForExecutorWithCheck()
    {
        getExecutor().waitUntilDone();
        if (executorError != null)
        {
            throw new MithraGeneratorException("exception while generating", executorError);
        }
    }

    private void closeIs(InputStream is)
    {
        if (is != null)
        {
            try
            {
                is.close();
            } catch (IOException e)
            {
                throw new MithraGeneratorException("Exception closing InputStream", e);
            }
        }
    }

    public ChopAndStickResource getChopAndStickResource()
    {
        return chopAndStickResource;
    }

    public AwaitingThreadExecutor getExecutor()
    {
        if (executor == null)
        {
            executor = new AwaitingThreadExecutor(Runtime.getRuntime().availableProcessors() + IO_THREADS, "Mithra Generator");
            executor.setExceptionHandler(new AutoShutdownThreadExecutor.ExceptionHandler()
            {
                public void handleException(AutoShutdownThreadExecutor executor, Runnable target, Throwable exception)
                {
                    executor.shutdownNow();
                    AnnotationParser.this.logger.error("Error in runnable target. Shutting down queue " + exception.getClass().getName() + " :" + exception.getMessage());
                    executorError = exception;
                }
            });
        }
        return executor;
    }

    private class ParentClassComparator implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            MithraObjectTypeWrapper left = (MithraObjectTypeWrapper) o1;
            MithraObjectTypeWrapper right = (MithraObjectTypeWrapper) o2;

            Map<String, MithraObjectTypeWrapper> mithraObjects = AnnotationParser.this.getMithraObjects();
            int result = left.getHierarchyDepth(mithraObjects) - right.getHierarchyDepth(mithraObjects);
            if (result == 0)
            {
                result = left.getClassName().compareTo(right.getClassName());
            }
            return result;
        }

    }

    public abstract class GeneratorTask implements Runnable
    {
        private int resourceNumber;

        public GeneratorTask(int resourceNumber)
        {
            this.resourceNumber = resourceNumber;
        }

        public void acquireSerialResource()
        {
            getChopAndStickResource().acquireSerialResource(resourceNumber);
        }

        public void releaseSerialResource()
        {
            getChopAndStickResource().releaseSerialResource();
        }
    }

    private void createSortedList()
    {
        this.sortedMithraObjects = new ArrayList<MithraObjectTypeWrapper>(this.mithraObjects.values());
        Collections.sort(this.sortedMithraObjects, new ParentClassComparator());
    }

    private void createSortedEmbeddedValueObjectList()
    {
        this.sortedMithraEmbeddedValueObjects = new ArrayList<MithraEmbeddedValueObjectTypeWrapper>(this.mithraEmbeddedValueObjects.values());
    }

    public List<MithraEmbeddedValueObjectTypeWrapper> getSortedMithraEmbeddedValueObjects()
    {
        return sortedMithraEmbeddedValueObjects;
    }

    private void createSortedEnumerationsList()
    {
        this.sortedMithraEnumerations = new ArrayList<MithraEnumerationTypeWrapper>(this.mithraEnumerations.values());
    }

    private FullFileBuffer getFullFileBuffer()
    {
        FullFileBuffer result = fullFileBufferThreadLocal.get();
        if (result == null)
        {
            result = new FullFileBuffer();
            fullFileBufferThreadLocal.set(result);
        }
        return result;
    }

    static class ElementsByType
    {
        final List<Element> sourceAttributes;
        final List<Element> attributes;
        final List<Element> relationships;
        final List<Element> indices;

        public ElementsByType()
        {
            this.sourceAttributes = new ArrayList<Element>();
            this.attributes = new ArrayList<Element>();
            this.relationships = new ArrayList<Element>();
            this.indices = new ArrayList<Element>();
        }

        public void addSourceAttribute(Element element)
        {
            this.sourceAttributes.add(element);
        }
        public void addAttribute(Element element)
        {
            this.attributes.add(element);
        }
        public void addRelationship(Element element)
        {
            this.relationships.add(element);
        }
        public void addIndex(Element element)
        {
            this.indices.add(element);
        }
    }

    private MithraObject processReladomoObject(ObjectResourceWrapper objectResource)
    {
        ReladomoObject reladomoObject = objectResource.getReladomoObject();

        MithraObject mithraObject = new MithraObject();
        mithraObject.setPackageName(reladomoObject.packageName());
        mithraObject.setClassName(objectResource.getName());
        mithraObject.setDefaultTable(reladomoObject.defaultTableName());
        mithraObject.setObjectType(reladomoObject.objectType().getType());

        setSuperClass(reladomoObject, mithraObject);
        setSuperClassType(reladomoObject, mithraObject);
        setUpdateListener(reladomoObject, mithraObject);
        setDatedTransactionalTemporalDirector(reladomoObject, mithraObject);

        List<AsOfAttributeType> asOfAttributeTypes = new ArrayList<AsOfAttributeType>();
        List<AttributeType> attributeTypes = new ArrayList<AttributeType>();
        List<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
        List<IndexType> indexTypes = new ArrayList<IndexType>();

        // todo : add source attribute
        // todo : maybe convert this to use the visitor pattern ?
        ElementsByType elementsByType = partitionElements(objectResource.getEnclosedElements());
        gatherAttributes(asOfAttributeTypes, attributeTypes, elementsByType.attributes);
        gatherRelationships(relationshipTypes, elementsByType.relationships);
        gatherIndices(indexTypes, elementsByType.indices);

        mithraObject.setAsOfAttributes(asOfAttributeTypes);
        mithraObject.setAttributes(attributeTypes);
        mithraObject.setRelationships(relationshipTypes);
        return mithraObject;
    }

    private void setUpdateListener(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one listener class can be specified
        Class[] updateListenerClasses = reladomoObject.updateListenerClass();
        if (updateListenerClasses != null && updateListenerClasses.length == 1)
        {
            mithraObject.setUpdateListener(updateListenerClasses[0].getCanonicalName());
        }
    }

    private void setDatedTransactionalTemporalDirector(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one listener class can be specified
        Class[] directorClasses = reladomoObject.datedTransactionalTemporalDirector();
        if (directorClasses != null && directorClasses.length == 1)
        {
            mithraObject.setDatedTransactionalTemporalDirector(directorClasses[0].getCanonicalName());
        }
    }

    private void setSuperClass(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one superclass can be specified
        SuperClass[] superClasses = reladomoObject.superClass();
        if (superClasses != null && superClasses.length == 1)
        {
            SuperClass superClass = superClasses[0];
            SuperClassAttributeType superClassAttributeType = new SuperClassAttributeType();
            superClassAttributeType.setName(superClass.name());
            superClassAttributeType.setGenerated(superClass.generated());
            mithraObject.setSuperClass(superClassAttributeType);
        }
    }

    private void setSuperClassType(ReladomoObject reladomoObject, MithraObject mithraObject)
    {
        //todo : validate that only one superclass can be specified
        SuperClassType.Enums[] superClassTypes = reladomoObject.superClassType();
        if (superClassTypes != null && superClassTypes.length == 1)
        {
            mithraObject.setSuperClassType(superClassTypes[0].getType());
        }
    }

    private void gatherRelationships(List<RelationshipType> relationshipTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            relationshipTypes.add(makeRelationship(element, name));
        }
    }

    private void gatherIndices(List<IndexType> indexTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            indexTypes.add(makeIndexType(element, name));
        }
    }

    private void gatherAttributes(List<AsOfAttributeType> asOfAttributeTypes, List<AttributeType> attributeTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            if (isAsOfAttribute(element))
            {
                addAsOfAttribute(element, name, asOfAttributeTypes);
            } else
            {
                addAttribute(element, name, attributeTypes);
            }
        }
    }

    private ElementsByType partitionElements(List<? extends Element> elements)
    {
        ElementsByType elementsByType = new ElementsByType();
        for (Element element : elements)
        {
            if (isAttribute(element))
            {
                elementsByType.addAttribute(element);
            }
            else if (isRelationship(element))
            {
                elementsByType.addRelationship(element);
            }
            else if (isIndex(element))
            {
                elementsByType.addIndex(element);
            }
        }
        return elementsByType;
    }

    private boolean isAttribute(Element element)
    {
        for (Class clazz : new Class[]{
                BigDecimalAttribute.class, ByteArrayAttribute.class, ByteAttribute.class,
                CharAttribute.class, DoubleAttribute.class, FloatAttribute.class,
                IntAttribute.class, LongAttribute.class, StringAttribute.class})
        {
            if (element.getAnnotation(clazz) != null)
            {
                return true;
            }
        }
        return false;
    }

    private ElementsByType partitionInterfaceElements(List<? extends Element> elements)
    {
        ElementsByType elementsByType = new ElementsByType();
        for (Element element : elements)
        {
            if (isInterfaceAttribute(element))
            {
                elementsByType.addAttribute(element);
            }
            else if (isInterfaceRelationship(element))
            {
                elementsByType.addRelationship(element);
            }
            else if (isInterfaceSourceAttribute(element))
            {
                elementsByType.addSourceAttribute(element);
            }
        }
        return elementsByType;
    }

    private void gatherInterfaceAttributes(List<AsOfAttributeInterfaceType> asOfAttributeTypes, List<AttributeInterfaceType> attributeTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            if (isInterfaceAsOfAttribute(element))
            {
                addInterfaceAsOfAttribute(element, name, asOfAttributeTypes);
            }
            else if (isInterfaceAttribute(element))
            {
                addInterfaceAttribute(element, name, attributeTypes);
            }
        }
    }

    private SourceAttributeInterfaceType makeInterfaceSourceAttribute(Element element, String name)
    {
        InterfaceSourceAttribute spec = element.getAnnotation(InterfaceSourceAttribute.class);
        SourceAttributeInterfaceType sourceAttributeInterfaceType = new SourceAttributeInterfaceType();
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String typeName = returnType.toString();
        typeName = typeName.substring(typeName.lastIndexOf(".")+1);
        sourceAttributeInterfaceType.setName(name);
        sourceAttributeInterfaceType.setJavaType(typeName);
        return sourceAttributeInterfaceType;
    }

    private void addInterfaceAsOfAttribute(Element element, String name, List<AsOfAttributeInterfaceType> asOfAttributeTypes)
    {
        InterfaceAsOfAttribute asOfAttribute = element.getAnnotation(InterfaceAsOfAttribute.class);
        if (asOfAttribute != null)
        {
            AsOfAttributeInterfaceType attributeType = makeInterfaceAsOfAttribute(asOfAttribute, name);
            asOfAttributeTypes.add(attributeType);
        }
    }

    private AsOfAttributeInterfaceType makeInterfaceAsOfAttribute(InterfaceAsOfAttribute spec, String name)
    {
        AsOfAttributeInterfaceType asOfAttributeType = new AsOfAttributeInterfaceType();
        asOfAttributeType.setName(name);
        asOfAttributeType.setInfinityDate(spec.infinityDate());
        asOfAttributeType.setInfinityIsNull(spec.infinityIsNull());
        asOfAttributeType.setIsProcessingDate(spec.isProcessingDate());
        asOfAttributeType.setToIsInclusive(spec.toIsInclusive());
        asOfAttributeType.setTimezoneConversion(spec.timezoneConversion().getType());
        return asOfAttributeType;
    }

    private void addInterfaceAttribute(Element element, String name, List<AttributeInterfaceType> attributeTypes)
    {
        InterfaceAttribute spec = element.getAnnotation(InterfaceAttribute.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        AttributeInterfaceType attributeType = new AttributeInterfaceType();
        attributeType.setName(name);
        String typeName = returnType.toString();
        typeName = typeName.substring(typeName.lastIndexOf(".")+1);
        attributeType.setJavaType(typeName);
        attributeTypes.add(attributeType);
    }

    private void gatherInterfaceRelationships(List<RelationshipInterfaceType> relationshipTypes, List<Element> elements)
    {
        for (Element element : elements)
        {
            String name = element.getSimpleName().toString();
            relationshipTypes.add(makeInterfaceRelationship(element, name));
        }
    }

    private RelationshipInterfaceType makeInterfaceRelationship(Element element, String name)
    {
        InterfaceRelationship spec = element.getAnnotation(InterfaceRelationship.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String relatedObject = returnType.asElement().getSimpleName().toString().replaceAll("Spec", "");

        RelationshipInterfaceType relationshipInterfaceType = new RelationshipInterfaceType();
        relationshipInterfaceType.setName(name);
        relationshipInterfaceType.setCardinality(spec.cardinality().getType());
        relationshipInterfaceType.setParameters(spec.parameters());
        relationshipInterfaceType.setRelatedObject(relatedObject);
        return relationshipInterfaceType;
    }

    private boolean isInterfaceAttribute(Element element)
    {
        return element.getAnnotation(InterfaceAttribute.class) != null;
    }

    private boolean isInterfaceRelationship(Element element)
    {
        return element.getAnnotation(InterfaceRelationship.class) != null;
    }

    private boolean isInterfaceAsOfAttribute(Element element)
    {
        return element.getAnnotation(InterfaceAsOfAttribute.class) != null;
    }

    private boolean isInterfaceSourceAttribute(Element element)
    {
        return element.getAnnotation(InterfaceSourceAttribute.class) != null;
    }

    private boolean isRelationship(Element element)
    {
        return element.getAnnotation(Relationship.class) != null;
    }

    private boolean isIndex(Element element)
    {
        return element.getAnnotation(Index.class) != null;
    }

    private boolean isAsOfAttribute(Element element)
    {
        return element.getAnnotation(AsOfAttribute.class) != null;
    }

    private void addAsOfAttribute(Element element, String name, List<AsOfAttributeType> asOfAttributeTypes)
    {
        AsOfAttribute asOfAttribute = element.getAnnotation(AsOfAttribute.class);
        if (asOfAttribute != null)
        {
            AsOfAttributeType attributeType = makeAsOfAttribute(asOfAttribute, name);
            asOfAttributeTypes.add(attributeType);
            Properties properties = element.getAnnotation(Properties.class);
            if (properties != null)
            {
                List<PropertyType> propertyTypes = extractProperties(properties);
                attributeType.setProperties(propertyTypes);
            }
        }
    }

    private void addAttribute(Element element, String name, List<AttributeType> attributeTypes)
    {
        AttributeType attributeType = makeTypedAttribute(element, name);
        if (attributeType == null)
        {
            // todo : generate validation error
        } else
        {
            setPrimaryKeyStrategy(element, attributeType);
            attributeTypes.add(attributeType);

            Properties properties = element.getAnnotation(Properties.class);
            if (properties != null)
            {
                List<PropertyType> propertyTypes = extractProperties(properties);
                attributeType.setProperties(propertyTypes);
            }
        }
    }

    private AttributeType makeTypedAttribute(Element reladomoObjectSpecElement, String name)
    {
        ByteAttribute byteAttribute = reladomoObjectSpecElement.getAnnotation(ByteAttribute.class);
        if (byteAttribute != null)
        {
            return makeByteAttribute(byteAttribute, name);
        }
        CharAttribute charAttribute = reladomoObjectSpecElement.getAnnotation(CharAttribute.class);
        if (charAttribute != null)
        {
            return makeCharAttribute(charAttribute, name);
        }
        IntAttribute intAttribute = reladomoObjectSpecElement.getAnnotation(IntAttribute.class);
        if (intAttribute != null)
        {
            return makeIntAttribute(intAttribute, name);
        }
        LongAttribute longAttribute = reladomoObjectSpecElement.getAnnotation(LongAttribute.class);
        if (longAttribute != null)
        {
            return makeLongAttribute(longAttribute, name);
        }
        FloatAttribute floatAttribute = reladomoObjectSpecElement.getAnnotation(FloatAttribute.class);
        if (floatAttribute != null)
        {
            return makeFloatAttribute(floatAttribute, name);
        }
        DoubleAttribute doubleAttribute = reladomoObjectSpecElement.getAnnotation(DoubleAttribute.class);
        if (doubleAttribute != null)
        {
            return makeDoubleAttribute(doubleAttribute, name);
        }
        BigDecimalAttribute bigDecimalAttribute = reladomoObjectSpecElement.getAnnotation(BigDecimalAttribute.class);
        if (bigDecimalAttribute != null)
        {
            return makeBigDecimalAttribute(bigDecimalAttribute, name);
        }
        StringAttribute stringAttribute = reladomoObjectSpecElement.getAnnotation(StringAttribute.class);
        if (stringAttribute != null)
        {
            return makeStringAttribute(stringAttribute, name);
        }
        ByteArrayAttribute byteArrayAttribute = reladomoObjectSpecElement.getAnnotation(ByteArrayAttribute.class);
        if (byteArrayAttribute != null)
        {
            return makeByteArrayAttribute(byteArrayAttribute, name);
        }
        return null;
    }

    private AttributeType makeCharAttribute(CharAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setDefaultIfNull(spec.defaultIfNull());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("char");

        return attributeType;
    }

    private AttributeType makeByteAttribute(ByteAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setDefaultIfNull(spec.defaultIfNull());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("byte");

        return attributeType;
    }

    private AttributeType makeByteArrayAttribute(ByteArrayAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("byte[]");

        return attributeType;
    }

    private AttributeType makeStringAttribute(StringAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("String");
        attributeType.setMaxLength(spec.maxLength());
        attributeType.setTruncate(spec.truncate());
        attributeType.setTrim(spec.trim());
        attributeType.setPoolable(spec.poolable());

        return attributeType;
    }

    private void setPrimaryKeyStrategy(Element element, AttributeType attributeType)
    {
        PrimaryKey primaryKey = element.getAnnotation(PrimaryKey.class);
        if (primaryKey == null)
        {
            attributeType.setPrimaryKey(false);
            return;
        }
        attributeType.setPrimaryKey(true);
        attributeType.setMutablePrimaryKey(primaryKey.mutable());
        boolean strategyAdded = addPKStrategy(element, attributeType);
        if (!strategyAdded)
        {
            //todo : generate validation error
        }
    }

    private boolean addPKStrategy(Element element, AttributeType attributeType)
    {
        SimulatedSequencePKStrategy simulatedSequencePKStrategy = element.getAnnotation(SimulatedSequencePKStrategy.class);
        if (simulatedSequencePKStrategy != null)
        {
            SimulatedSequenceType simulatedSequenceType = new SimulatedSequenceType();
            simulatedSequenceType.setBatchSize(simulatedSequencePKStrategy.batchSize());
            simulatedSequenceType.setInitialValue(simulatedSequencePKStrategy.intialValue());
            simulatedSequenceType.setIncrementSize(simulatedSequencePKStrategy.incrementSize());
            simulatedSequenceType.setSequenceName(simulatedSequencePKStrategy.sequenceName());
            simulatedSequenceType.setSequenceObjectFactoryName(simulatedSequencePKStrategy.sequenceObjectFactoryName());
            simulatedSequenceType.setHasSourceAttribute(simulatedSequencePKStrategy.hasSourceAttribute());

            attributeType.setSimulatedSequence(simulatedSequenceType);
            attributeType.setPrimaryKeyGeneratorStrategy(PrimaryKeyGeneratorStrategyType.SimulatedSequence);
            return true;
        }
        MaxPKStrategy maxPKStrategy = element.getAnnotation(MaxPKStrategy.class);
        if (maxPKStrategy != null)
        {
            attributeType.setPrimaryKeyGeneratorStrategy(PrimaryKeyGeneratorStrategyType.Max);
            return true;
        }
        return false;
    }

    private AttributeType makeIntAttribute(IntAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("int");
        attributeType.setUseForOptimisticLocking(spec.useForOptimisticLocking());

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeLongAttribute(LongAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("long");
        attributeType.setUseForOptimisticLocking(spec.useForOptimisticLocking());

        //todo : is properties applicable ?
        return attributeType;
    }

    //((AnnotationInvocationHandler) ((Proxy) spec).getInvocationHandler()).getMemberMethods()[0]
    private AttributeType makeDoubleAttribute(DoubleAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("double");

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeFloatAttribute(FloatAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());
        attributeType.setDefaultIfNull(spec.defaultIfNull());

        //specific attributes
        attributeType.setJavaType("float");

        //todo : is properties applicable ?
        return attributeType;
    }

    private AttributeType makeBigDecimalAttribute(BigDecimalAttribute spec, String name)
    {
        AttributeType attributeType = new AttributeType();
        //generic attributes
        attributeType.setName(name);
        attributeType.setColumnName(spec.columnName());
        attributeType.setReadonly(spec.readonly());
        attributeType.setNullable(spec.nullable());
        attributeType.setInPlaceUpdate(spec.inPlaceUpdate());
        attributeType.setFinalGetter(spec.finalGetter());

        //specific attributes
        attributeType.setJavaType("BigDecimal");
        attributeType.setPrecision(spec.precision());
        attributeType.setScale(spec.scale());

        //todo : is properties applicable ?
        return attributeType;
    }

    private AsOfAttributeType makeAsOfAttribute(AsOfAttribute spec, String name)
    {
        AsOfAttributeType asOfAttributeType = new AsOfAttributeType();
        asOfAttributeType.setName(name);
        asOfAttributeType.setSetAsString(spec.setAsString());
        asOfAttributeType.setFromColumnName(spec.fromColumnName());
        asOfAttributeType.setToColumnName(spec.toColumnName());
        asOfAttributeType.setFinalGetter(spec.finalGetters());
        asOfAttributeType.setDefaultIfNotSpecified(spec.defaultIfNotSpecified());
        asOfAttributeType.setFutureExpiringRowsExist(spec.futureExpiringRowsExist());
        asOfAttributeType.setInfinityDate(spec.infinityDate());
        asOfAttributeType.setInfinityIsNull(spec.infinityIsNull());
        asOfAttributeType.setIsProcessingDate(spec.isProcessingDate());
        asOfAttributeType.setToIsInclusive(spec.toIsInclusive());
        asOfAttributeType.setTimezoneConversion(spec.timezoneConversion().getType());
        asOfAttributeType.setTimestampPrecision(spec.timestampPrecision().getType());
        asOfAttributeType.setPoolable(spec.poolable());
        return asOfAttributeType;
    }

    private RelationshipType makeRelationship(Element element, String name)
    {
        Relationship spec = element.getAnnotation(Relationship.class);
        Type returnType = ((Symbol.MethodSymbol) element).getReturnType();
        String relatedObject = returnType.asElement().getSimpleName().toString().replaceAll("Spec", "");

        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setName(name);
        relationshipType.setRelatedIsDependent(spec.relatedIsDependent());
        relationshipType.setCardinality(spec.cardinality().getType());
        relationshipType.setRelatedObject(relatedObject);
        relationshipType._setValue(spec.contract());
        relationshipType.setForeignKey(spec.foreignKeyType().getType());
        relationshipType.setDirectReference(spec.directReference());
        relationshipType.setFinalGetter(spec.finalGetter());
        if (!spec.reverseRelationshipName().trim().isEmpty())
        {
            relationshipType.setReverseRelationshipName(spec.reverseRelationshipName());
        }
        if (!spec.orderBy().trim().isEmpty())
        {
            relationshipType.setOrderBy(spec.orderBy());
        }
        if (!spec.parameters().isEmpty())
        {
            relationshipType.setParameters(spec.parameters());
        }
        if (!spec.returnType().isEmpty())
        {
            relationshipType.setReturnType(spec.returnType());
        }
        return relationshipType;
    }

    private IndexType makeIndexType(Element element, String name)
    {
        Index spec = element.getAnnotation(Index.class);
        IndexType indexType = new IndexType();
        indexType.setName(name);
        indexType.setUnique(spec.unique());
        indexType._setValue(spec.attributes());
        return indexType;
    }

    private List<PropertyType> extractProperties(Properties spec)
    {
        List<PropertyType> propertyTypes = new ArrayList<PropertyType>();
        for (Property property : spec.value())
        {
            PropertyType propType = new PropertyType();
            propType.setKey("\"" + property.key() + "\"");
            propType.setValue("\"" + property.value() + "\"");
            propertyTypes.add(propType);
        }
        return propertyTypes;
    }
}
