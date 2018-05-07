package org.kitodo.mediaserver.importer.configuration;

import org.kitodo.mediaserver.importer.validators.IsPathToExistingFolder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * the main property configuration of the importer
 * containing all the properties we need
 * the configuration properties are  usually set in the four .yaml files:
 * in the Core project:
 * "default"
 * and "local"
 * and under Importer project
 "application"
 and "logging"
 */
@ConfigurationProperties(prefix = "importer")
@Validated
public class ImporterConfig {

    private boolean dryRun;


    private boolean recreateBatchInternalTablesOnEachStart;

    private List<String> moveSubfoldersEndingWith = new ArrayList<String>();

    @NotEmpty
    private List<String> workMetafileEndings = new ArrayList<String>();

    @NotNull
    @IsPathToExistingFolder
    private String uncommittedTargetFolder;

    @NotEmpty
    @NotNull
    private String idetifierPropertiesStartWith;


    @NotEmpty
    @NotNull
    private String mainIdentifierTypeToBecomeWorksKey;

    @NotEmpty
    @NotNull
    private String   mainTitleForWorkFromProperty;


    @NotNull
    @IsPathToExistingFolder
    private String targetFolder;

    @NotNull
    @IsPathToExistingFolder
    private String hotFolderPath;



    private static boolean EndsWithIgnoreCase(Path path, List<String> oneOfThese) {
        return oneOfThese.stream().anyMatch(ending -> EndsWithIgnoreCase(path, ending));
    }

    private static boolean EndsWithIgnoreCase(Path path, String ending) {
        return path.toString().toLowerCase().endsWith(ending.toLowerCase());
    }
    public String getIdetifierPropertiesStartWith() {
        return idetifierPropertiesStartWith;
    }

    public void setIdetifierPropertiesStartWith(String idetifierPropertiesStartWith) {
        this.idetifierPropertiesStartWith = idetifierPropertiesStartWith;
    }

    public String getMainIdentifierTypeToBecomeWorksKey() {
        return mainIdentifierTypeToBecomeWorksKey;
    }

    public void setMainIdentifierTypeToBecomeWorksKey(String mainIdentifierTypeToBecomeWorksKey) {
        this.mainIdentifierTypeToBecomeWorksKey = mainIdentifierTypeToBecomeWorksKey;
    }

    public String getMainTitleForWorkFromProperty() {
        return mainTitleForWorkFromProperty;
    }

    public void setMainTitleForWorkFromProperty(String mainTitleForWorkFromProperty) {
        this.mainTitleForWorkFromProperty = mainTitleForWorkFromProperty;
    }

    /**
     * #if dry-run is true the importer never really moves files it is meant for debugging
     *
     * @return
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * #if dry-run is true the importer never really moves files it is meant for debugging
     * it is set in the .yml configuration
     *
     * @param dryrun
     */
    public void setDryRun(boolean dryrun) {
        this.dryRun = dryrun;
    }

    /**
     * see the javadoc for the BatchTablesConfiguration class or the comments in the configuration
     * for more details
     * @return the state of the Property from the configuration
     */
    public boolean isRecreateBatchInternalTablesOnEachStart() {
        return recreateBatchInternalTablesOnEachStart;
    }

    public void setRecreateBatchInternalTablesOnEachStart(boolean recreateBatchInternalTablesOnEachStart) {
        this.recreateBatchInternalTablesOnEachStart = recreateBatchInternalTablesOnEachStart;
    }




    public List<String> getMoveSubfoldersEndingWith() {
        return moveSubfoldersEndingWith;
    }

    public List<String> getWorkMetafileEndings() {
        return workMetafileEndings;
    }

    private String getWorkMetaFileTargetEnding() {
        /// since workMetafileEndings is @notEmpty we can skip the additional checks here
        return workMetafileEndings.get(0);
    }

    public String getUncommittedTargetFolder() {
        return uncommittedTargetFolder;
    }

    public void setUncommittedTargetFolder(String uncommittedTargetFolder) {
        this.uncommittedTargetFolder = uncommittedTargetFolder;
    }
    public String getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder = targetFolder;
    }
    public String getHotFolderPath() {
        return hotFolderPath;
    }

    public void setHotFolderPath(String hotFolderPath) {
        this.hotFolderPath = hotFolderPath;
    }

    public Path getUncommittedTargetWorkBaseFolder(String workId) {
        return Paths.get(getUncommittedTargetFolder(), workId);
    }
    public Path getTartgetWorkBaseFolder(String workId) {
        return Paths.get(getTargetFolder(), workId);
    }

    public Path getWorkMetafilePathInUncommittedTargetFolder(String workId) {
        return Paths.get(getUncommittedTargetWorkBaseFolder(workId).toString(), workId + getWorkMetaFileTargetEnding());
    }

    /**
     * reads the list of extensions for WorkMetaFile from configuration and adds the @workId in front of each of it
     * like: .xml -> work12346.xml then checks if (the passed @path parameter)
     * ends with this value: like /bla/hotfolder/work12346.xml
     *
     * @param workId to be used in the check
     * @param path   to a file or folder name to be checked
     * @return true if check is passed or false otherwise
     */
    public boolean isWorkMetaXmlFile(String workId, Path path) {
        return EndsWithIgnoreCase(path,
                getWorkMetafileEndings()
                        .stream()
                        .map(ext -> workId + ext)
                        .collect(Collectors.toList())
        );
    }

    /**
     * reads the list of extensions for MoveSubfoldersEndingWith from configutration and adds the @workId in front of each of it
     * like: .xml -> work12346.xml then checks if (the passed @path parameter)
     * ends with this value: like /bla/hotfolder/work12346.xml
     *
     * @param workId to be used in the check
     * @param path   to a file or folder name to be checked
     * @return true if check is passed or false otherwise
     */
    public boolean isSubdirToMove(String workId, Path path) {
        List<String> candidates = getMoveSubfoldersEndingWith()
                .stream()
                .map(ext -> workId + ext)
                .collect(Collectors.toList());
        return EndsWithIgnoreCase(path,candidates);
    }

}
