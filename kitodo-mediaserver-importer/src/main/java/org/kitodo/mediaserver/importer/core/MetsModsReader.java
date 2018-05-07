package org.kitodo.mediaserver.importer.core;

import org.kitodo.mediaserver.importer.WorkMetaInfo;
import org.kitodo.mediaserver.importer.configuration.ImporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;

import javax.naming.ConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reader of signature list IDs from the hotfolder,
 * see more details in the read() method description
 */
public class MetsModsReader implements ItemReader<WorkMetaInfo> {

    private final Logger logger = LoggerFactory.getLogger(MetsModsReader.class);

    private ImporterConfig importerConfig;
    private LinkedList<Path> metsFilesLeft = new LinkedList<>();
    private ArrayList<Path> originalFileList = new ArrayList<>();
    private Path singleFilePath = null;

    public MetsModsReader(ImporterConfig importerConfig) throws ConfigurationException {
        String errortext = "Scanner: Can not instantiate MetsModsReader with the ";

         /* getHotFolderPath, targetFolder,etc existence validation is done
            by the @IsPathToExistingFolder annotation
              in the ImporterConfig.java so no need for it here
                only need to check if the importerConfig is set
         */

        if (importerConfig == null) {
            throw new ConfigurationException(errortext + "importerConfig: null");
        }
        this.importerConfig = importerConfig;

    }


    /**
     * gets the path to a WorkMetaFile (xml file containig definition of a work) whether it is
     * contained in a subfolder (given by the @path) or the file given by the @path, returns null if none is the case
     * checks if given path points to the WorkMetaFile
     * or to a subfolder containing such file. in the latter case the name of the subfolder and
     * name of the contained file must match (since they are equal to WorkId)
     * @param path file or folder to perform the check on
     * @return path the  WorkMetaFile(xml file containig definition of a work), it is either
     * the same as the path argument or a file with the same name in the subfolder, or NULL if
     * the @path does not point to WorkMetaFile or a subfolder with the same name containig it
     * @throws IOException in case we had problems accessing the subfolder or the file
     */
    private Path getWorkMetaFilePath(Path path) throws IOException {

        if (Files.isDirectory(path)){
            //ok so this path is a subfolder then its name must  be our WorkId
            //and we effectively  checking if it contains an .xml(or whatever extensions are given by configuration)
            // file with same workid
            //like: work12345/work12345.xml
            List<Path> subfolderfiles = Files.list(path).filter(subpath ->
                        !Files.isDirectory(subpath)
                         &&  importerConfig.isWorkMetaXmlFile( path.getFileName().toString() , subpath)
                        ).collect(Collectors.toList());
            if (subfolderfiles.isEmpty()) return null;
            else if (subfolderfiles.size() > 1)
                throw new RuntimeException("Scanner: Can continue import from folder:'" + path.getParent() + "' because"
                        + "because is subfolder:'" + path + "' contains more than one .xml (or other file extensions configured under 'workMetaFileEndings:' property) file with the filename matching the name of the containing folder."
                        + "the problematic files are:" + subfolderfiles.toArray());
            else return subfolderfiles.get(0);
        }
        //it is not a subfolder and we do not pass any WorkId (only ""
        //so we are effectively checking if 'path' ends with ".xml" or whatever endings are given in the configuration
        else if(importerConfig.isWorkMetaXmlFile("",path)) return path;
        return null;
    }

    /**
     * get the next  WorkMetaFile from the hotfolder or matching subfolder, only rescans the folders in the filesystem
     * if we have processed all (non-stuck) entries were read by the last hotfolder Scan.
     * @return a path to the WorkMetaFile (xml file containig definition of a work) or null if no more files
     * in hotfolder (or the hotfolder only contains stuck files which can not be removed, see detailed comments in the function body)
     * @throws IOException if there were problems listing the hotfolder its subfolders or containerd files
     */
    private Path getNextWorkMetaFile() throws IOException {

        //try to rescan hotfolder if no more files in our list
        if (metsFilesLeft.isEmpty()) {
            logger.info("Scanner: List of cached hotfolder Entries is empty: rescanning hotfolder: '" + importerConfig.getHotFolderPath()+ "'");

            ArrayList<Path> newFiles = new ArrayList<>();
            newFiles.addAll(Files.list(Paths.get(importerConfig.getHotFolderPath()))
                    .map(path -> {
                        try {
                            return getWorkMetaFilePath(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(path -> path != null)
                    .collect(Collectors.toList()));

            //check if all files which where in the directory on the last read are still in the directory
            //if they are our last batch chunk run failed  to  remove them (for whatever reasons)
            //these files have been processed on and are  unremovable from our perspective so stop repeatedly
            //trying to process them. for that we clear the list just read files this will stop the processing
            if (originalFileList.containsAll(newFiles)) {
                newFiles.clear();
            }
            //set the originalFileList to Currently read fileList so we can compare to it on the next Directory read
            originalFileList.clear();
            originalFileList.addAll(newFiles);
            metsFilesLeft.addAll(newFiles);

        }
        // we have tried to reload from the hotfolder
        // but the metsFilesLeft ist still empty so the hotfolder is Really empty now(or only contains untremovable files:see above)
        // so return null to stop the batch process
        if (metsFilesLeft.isEmpty()){
            logger.info("Scanner: Reporting all done. No more Work found in hotfolder after rescanning hotfolder: '" + importerConfig.getHotFolderPath()+ "' it is still empty (or only containg stuck elements that can not be removed)" );
            return null;
        }

        return metsFilesLeft.pop();
    }

    /**
     *
          made especially
        to be able to wo work on a single items basis outside of batch processing for the IMetsReader Interface
      *
     * @param file
     * @return returns the object itself to be able to cain function call like in:
     * return (new MetsModsReader()).setSingleFilePath("test.xml");
     */
    public MetsModsReader setSingleFilePath(File file) {
        if (file == null) {
            throw new IllegalArgumentException("The mets file argument is null");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("The mets file " + file.getAbsolutePath() + " is not a file");
        }

        singleFilePath = file.toPath();

        return this;

    }

    /**
     *
     * gets the next WorkMetafile (xml file containing definition of a work) from the hotfolder

     * @return null if no more files to process so that  the batch processing will be ended on null
     * @throws IOException if there was a problem scanning files in the Hotfolder directory
     */
    @Override
       public WorkMetaInfo read() throws IOException {
        Path readPath = singleFilePath;
        singleFilePath=null; //we will read it so reset the singleFilePath for the case the
                             // read will be called again to read in normal mode from hotFolder without
                             // using setSingleFilePath
        if(readPath ==null) {
            readPath =  getNextWorkMetaFile();
        }
        if (readPath != null) {
            logger.info("Scanner: Reading WorkDefintionInfo from  MetsMods file: '" + readPath+ "'");
            return new WorkMetaInfo(readPath);//new StreamSource(metsmodsStub);
        } else return null;
    }

}
