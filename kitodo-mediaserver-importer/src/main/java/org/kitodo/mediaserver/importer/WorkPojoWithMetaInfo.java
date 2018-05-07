package org.kitodo.mediaserver.importer;

import org.kitodo.mediaserver.core.db.entities.Identifier;
import org.kitodo.mediaserver.core.db.entities.Work;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;

public class WorkPojoWithMetaInfo
{
    Work workPojo;
    private Path pathToWorkMetaInfoXmlFile;

    public Work getWorkPojo() {
        return workPojo;
    }

    public WorkPojoWithMetaInfo(String id, String title,Path  pathToWorkMetaInfoXmlFile, String targetPathForWorkPjo)
    {
       this.pathToWorkMetaInfoXmlFile =pathToWorkMetaInfoXmlFile;
       workPojo =  new Work  (id,title);
       workPojo.setEnabled(true);
       workPojo.setPath(targetPathForWorkPjo);

    }
    public WorkPojoWithMetaInfo AddIdentifiers(Properties allIds)
    {
        workPojo.setIdentifiers(new HashSet<Identifier>());
        allIds.forEach((key,val)->
        {
            workPojo.getIdentifiers().add( new Identifier((String)val, (String )key,workPojo));

        });
        return this;
    }

    /**
     *
     * @return  returns the path folder in which subfolders like 124566_tif or 123456_txt for this work
     * are located, it is closely bound to the getPathToWorkMetaInfoXmlFile()
     * but can be adjusted inside this getter.
     */
    public Path getBaseFolderForWorkSubfolders() {
        return getPathToFolderWhereMetsXmlFileIsFound();
    }

    /**
     *
     * @return returns Path to the Folder where .xml file containig the work information is located
     * usually  is hotfolder/WorkId/
     * this path is taken from the information supplied by the  setPathToWorkMetaInfoXmlFile() on initialization
     */
    private Path getPathToFolderWhereMetsXmlFileIsFound(){
        //the assumption is that error checking for the existence of the  pathToWorkMetaInfoXmlFile
        // file was done in the constructor, so we do not really need to recheck is here
        return pathToWorkMetaInfoXmlFile.getParent();
    }
    /** @returns path to WorkMetaInfo .xml file containig the information on current Work */
    public Path getPathToWorkMetaInfoXmlFile() {
        return pathToWorkMetaInfoXmlFile;
    }




}
