package org.kitodo.mediaserver.importer.batch;

import org.kitodo.mediaserver.core.db.entities.Work;
import org.kitodo.mediaserver.importer.WorkPojoWithMetaInfo;
import org.kitodo.mediaserver.importer.configuration.ImporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


//todo:PETER: the writer probably needs to be changed into a tasklet in a separate step
//to work well with chunksize>=2 (state consistency on errors)


/**
 * a writer for java batch wich in fact acts more like a tasklet
 * for each WorkMetaInfo object it moves files and subfolders to the
 * target folder accourding to configuration
 */
public class FileMovingWriter implements ItemWriter<WorkPojoWithMetaInfo> {

    private final Logger logger = LoggerFactory.getLogger(FileMovingWriter.class);
    private ImporterConfig importerConfig;

    public FileMovingWriter(ImporterConfig importerConfig) {
        this.importerConfig = importerConfig;

    }

    private static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * A writer which for each item moves contained Files from hotfolder to target folder
     * it explicitly moves subfolders given by the move-subfolders-ending-with configuration property
     * and the the Work ID (which is read from metsmods definition XMl file)
     * thus making sure these subfolders do exist
     * it then does the same for the Work definition file(means:metsmods file) itself
     * after that it moves the rest of the folder to target folder, just in case, there was something else useful inside
     * which was not in one of the subfolders listed by  move-subfolders-ending-with property.
     * then it deletes the WorkID-subfolder of the work in the hotfolder (if it existed because the chotfolder could contain
     * the Workdescription file directly like .../hotfolder/UATUB_717-0068.xml instead of .../hotfolder/UATUB_717-0068/UATUB_717-0068.xml)
     * TODO: within spring batch this FileMovingWriter is probably better implemented as a Tasklet instead
     *
     * @param items is a list of WorkMetaInfo objects, each of them represents information from a single Metsmods xml Work description
     *              and thus out subfolder in a target folder
     * @throws IOException in case something goes wrong when moving files to target folder
     */
    //TODO: firgure out what are the real suitabe args for the @Transactional() annotation here, while it is clearer with database writing, it is not obvious yet how transactions should be handled in FileMovingWriter and if the existing parameters are really suitable for proper transaction handling
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void write(List<? extends WorkPojoWithMetaInfo> items) throws IOException {

        for (WorkPojoWithMetaInfo workPojoWithMetaInfo : items) {


            Path path = workPojoWithMetaInfo.getBaseFolderForWorkSubfolders();
            Work workPojo = workPojoWithMetaInfo.getWorkPojo();

            Stream<Path> subfolderContent = Files.list(path);

            List<Path> subfolderDirs = subfolderContent.filter(subpath ->
                    Files.isDirectory(subpath)
                            && importerConfig.isSubdirToMove(workPojo.getId(), subpath)    //.isWorkMetaXmlFile(subpath);
            ).collect(Collectors.toList());

            //todo: perhaps add files deleteifexist() targetfolder.<WorkId>
            //if subfolder for the WorkId in the target folder does not exist creates it, before moving any files
            File uncommittedTargetFolder = new File(importerConfig.getUncommittedTargetWorkBaseFolder(workPojo.getId()).toString());
            if (!importerConfig.isDryRun()) {
                logger.info("Work Id: "
                        + workPojo.getId() + " creating (in case it does not exit) '"
                        + uncommittedTargetFolder.getAbsolutePath()
                        + "' now.");
                uncommittedTargetFolder.mkdirs();
            }

            if (subfolderDirs.isEmpty())
                logger.info("Work Id: " + workPojo.getId()
                        + " no subdirectories to move in the imported folder '"
                        + workPojoWithMetaInfo.getBaseFolderForWorkSubfolders() + "'");
            else {
                for (Path subfolder : subfolderDirs) {
                    Path fromFile = subfolder;
                    Path toFile = importerConfig.getUncommittedTargetWorkBaseFolder(
                            workPojo.getId()).resolve(subfolder.getFileName());

                    MoveFileOrSubfolder(fromFile, toFile, workPojo.getId());
                }
            }
            //move the workmetainfo .xml file itself to uncommittedTarget folder logging the operation
            MoveFileOrSubfolder(workPojoWithMetaInfo.getPathToWorkMetaInfoXmlFile(),
                    importerConfig.getWorkMetafilePathInUncommittedTargetFolder(workPojo.getId()),
                    workPojo.getId());

            //checks if Work's folder is the hotfolder itself or a subfolder within it
            // like .../hotfolder/UATUB_717-0068.xml or .../hotfolder/UATUB_717-0068/UATUB_717-0068.xml
            // in the latter case we can move the whole contents of this work's subfolder to the target subfolder.
            Path worksFolder = workPojoWithMetaInfo.getBaseFolderForWorkSubfolders();
            Path hotFolder = Paths.get(importerConfig.getHotFolderPath());
            if (!Files.isSameFile(hotFolder, worksFolder)) {
                //so there is works own subfolder
                // in the hotfolder move it and merge is contents
                // with the things we have already moved  to the target folder, logging it
                //check if there are
                List<Path> contents = Files.list(worksFolder).collect(Collectors.toList());

                if (contents.size() > 0) {
                    logger.info("Work Id: " + workPojo.getId()
                            + " the target folder '" + importerConfig.getUncommittedTargetWorkBaseFolder(workPojo.getId())
                            + "' already exists "
                            + "starting to Merge " + contents.size() + " subfolders or files still left in the import folder into it");
                }
                // just call the folder Move in any case to meke sure the Works subfolder in the hotfolder is removed even if it is empty now
                MoveFileOrSubfolder(worksFolder,
                        importerConfig.getUncommittedTargetWorkBaseFolder(workPojo.getId()),
                        workPojo.getId());
            } else {
                logger.info("Work Id " + workPojo.getId()
                        + " the  Workdescription Xml file ('" + workPojoWithMetaInfo.getPathToWorkMetaInfoXmlFile()
                        + "') for this work was located directly under the hotfolder" +
                        " this means that the Work's folder is the same as the hotfolder itself," +
                        " thus only this xml file itself and the  subfolders named like <workId>_tif, <workId>_txt, etc (see move-subfolders-ending-with conf property)" +
                        " can be moved to target folder, but any other files or folders belonging to this work can not be distinguished " +
                        " from files or subfolders belonging to other WorkIds, and will not be moved to target folder."
                );
            }

            //deleting old files in targetsubfolder (assuming they are older versions)
            Path targetsubfolderPath = importerConfig.getTartgetWorkBaseFolder(workPojo.getId());
            if (Files.exists(targetsubfolderPath)) {
                logger.info("Work Id: " + workPojo.getId()
                        + " folder '" + targetsubfolderPath + "' already exists. deleting old files");
                deleteFolder(targetsubfolderPath.toFile());

            }
            // moving the result from the uncommitted forder to the target folder
            logger.info("Work Id: " + workPojo.getId() + " moving '"
                    + importerConfig.getUncommittedTargetWorkBaseFolder(workPojo.getId())
                    + "' to '" + targetsubfolderPath + "'");
            MoveFileOrSubfolder(
                    importerConfig.getUncommittedTargetWorkBaseFolder(workPojo.getId()),
                    targetsubfolderPath,
                    workPojo.getId());

        }
    }

    /**
     * Moves file or subfolder with its contents, merging the contained items into target, if the target is a Folder and already exists
     *
     * @param fromFile       a file or dir to be moved
     * @param toFile         a path to the where file or dir to be moved to (to the fututre file or folder itself, not the containig folder)
     * @param workIdentifier is only used for logging, will be logged so we know which Work this files moved belonged to
     * @throws IOException well, if the movement failed
     */
    private void MoveFileOrSubfolder(Path fromFile, Path toFile, String workIdentifier) throws IOException {


        logger.info("Work Id: "
                + workIdentifier + " moving "
                + (Files.isDirectory(fromFile) ? "folder" : "file") + ":'" + fromFile
                + "' to '" + toFile + "'.");
        try {
            //checks if there might be a conflicting name file or folder under the targetname which ca not be merged
            if (Files.exists(toFile)
                    && ((Files.isDirectory(fromFile) && !Files.isDirectory(toFile))
                    || (!Files.isDirectory(fromFile) && Files.isDirectory(toFile)))
                    ) {
                throw new IOException("can not move file or folder '" + fromFile + "' to '" + toFile + "'"
                        + "there is a file name conflict: the '" + toFile + "' already exists"
                        + " and it is a "
                        + (Files.isDirectory(toFile) ? "directory" : "file")
                        + "whereas the '" + fromFile + "' is a"
                        + (Files.isDirectory(fromFile) ? "directory" : "file"));
            }

            //isDryRun is for debugging only, to ommit file movement operations itself
            if (!importerConfig.isDryRun()) {

                //if we need to move directory and it already exists on a target we need to perform merge
                // we iterate over all contents of our directory repeating  a recursive call for each element
                //after that we remove the source directory after making sure it is empty
                if (Files.isDirectory(fromFile) && Files.exists(toFile)) {


                    List<Path> contents = Files.list(fromFile).collect(Collectors.toList());
                    if (contents.size() > 0) {
                        //iterate over all items in folder and move each of them
                        for (Path srcFileOrFolder : contents) {
                            Path toFileOrFolder = importerConfig.getUncommittedTargetWorkBaseFolder(
                                    workIdentifier).resolve(srcFileOrFolder.getFileName());
                            try {

                                MoveFileOrSubfolder(srcFileOrFolder, toFileOrFolder, workIdentifier);

                            } catch (IOException folderElementEx) {
                                throw new IOException("can not move file or folder:'"
                                        + srcFileOrFolder + "' to '"
                                        + toFileOrFolder + "'. problem was:" + folderElementEx.getMessage(), folderElementEx);
                            }
                        }
                    }
                    //after the contents of the folder were moved delete the empty folder
                    logger.info("Work Id: " + workIdentifier
                            + " deleting the folder'" + fromFile + "'");
                    Files.delete(fromFile);

                } else {
                    //we do not need to perform merge of directories
                    //so just call the system to do it
                    Files.move(fromFile, toFile, StandardCopyOption.REPLACE_EXISTING);
                }


            }
        } catch (IOException e) {
            throw new IOException("can not move file or folder:'"
                    + fromFile + "' to '"
                    + toFile + "'. problem was:" + e.getMessage(), e);
        }
    }
}