package org.kitodo.mediaserver.importer;

import org.kitodo.mediaserver.importer.configuration.ImporterConfig;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TODO: perhaps need to look at other properties named "title" and now only one property getMainTitleForWorkFromProperty()which is typically set to "main_label", as candidates for the work main title in case "main_label" is not present. Ask Per.
 *
 * encapsulates the  Mapping logic from string properties read from metsmods.xml
 * to actual Work and Identifier Pojos and other important information like Works Id, or title.
 * it contains the converter that can be user to produce the augmented WorkPojoObjects
 *
 */
public class WorkMetaInfoMapper
{
    WorkMetaInfo workMetaInfo;
    ImporterConfig importerConfig;

    public WorkMetaInfoMapper(WorkMetaInfo workMetaInfo, ImporterConfig importerConfig)
    {
        this.workMetaInfo=workMetaInfo;
        this.importerConfig = importerConfig;
    }

    public String getId() {
        return workMetaInfo.getAllIds().getProperty(importerConfig.getMainIdentifierTypeToBecomeWorksKey());
    }

    private String getTitle() {
        return workMetaInfo.getProps().getProperty(importerConfig.getMainTitleForWorkFromProperty());
    }

    public WorkMetaInfoMapper setPropsFromLines(Stream<String> linesReturnedByXsl)
    {
        this.workMetaInfo=  linesReturnedByXsl.reduce(workMetaInfo, (acc, line) -> parseProps(acc, line), WorkMetaInfo::Combine);
        return this;

    }

    //TODO: this property interpretation might need a cleanup and perhaps more flexibility
    private WorkMetaInfo parseProps(WorkMetaInfo def, String line) {

        String key = line.trim().split(":")[0];
        String val = Arrays.stream(line.split(":")).skip(1).collect(Collectors.joining(":"));
        //System.out.println("++parser key:"+ key+ ",val:"+ val);

        if (key.startsWith(importerConfig.getIdetifierPropertiesStartWith()) && val != null && val.trim().length() != 0) {
            def.getAllIds().setProperty(key.split("\\.").length < 2
                            ? "default"
                            : Arrays.stream(key.split("\\.")).skip(1).collect(Collectors.joining("\\."))
                    , val);
        } else {
            if (def.getProps().containsKey(key)) {
                if (key == importerConfig.getMainTitleForWorkFromProperty()
                            && def.getProps().getProperty(key).length() < val.length())
                    def.getProps().setProperty(key, val);
                //   else throw new  Exception("trying to add value '"
                //         + val
                //       +"'  parsePerson: only main_label or identifyer can have multiple values");
            } else if (!"".equals(key)) def.getProps().setProperty(key, val);
        }
        return def;

    }



    /**
     *  a converter which creates an augmented Work Pojo Object
     * the next stage in a lifecyle. the new Object contains the Pojo Work and its Identifiers all initialized
     * from the information in the props and ids variables,
     * as well as the pathToWorkMetaInfoXmlFile which is needed for file management
     *  TODO: there is to little time to create a cleaner separation of all this pre/post-parsing and Path manipulation objects but it needs to be done in future
     * @return the new object
     */
    public WorkPojoWithMetaInfo getWorkPojoWithMetaInfo(){
        WorkPojoWithMetaInfo enchancedPojo = new WorkPojoWithMetaInfo(getId()
                , getTitle()
                , workMetaInfo.getPathToWorkMetaInfoXmlFile()
                , importerConfig.getTartgetWorkBaseFolder(getId()).toString())

                .AddIdentifiers(workMetaInfo.getAllIds());
        return enchancedPojo;
    }
}
