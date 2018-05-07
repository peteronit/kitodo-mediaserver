package org.kitodo.mediaserver.importer;

import javax.xml.transform.stream.StreamSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;


public class WorkMetaInfo {

    private Path pathToWorkMetaInfoXmlFile;
    private StreamSource workMetaInfoAsStreamSource;


    private Properties props = new Properties();

    private Properties ids = new Properties();



    /**
    @returns path the Streamsource to .xml file containig the information on current Work
    the StreamSource is Lazy initialized
    */
    public StreamSource getWorkMetaInfoAsStreamSource() {
        if(workMetaInfoAsStreamSource ==null){
            workMetaInfoAsStreamSource = new StreamSource(pathToWorkMetaInfoXmlFile.toString());
        }
        return workMetaInfoAsStreamSource;
    }

    /** @returns path to WorkMetaInfo .xml file containig the information on current Work */
    public Path getPathToWorkMetaInfoXmlFile() {
        return pathToWorkMetaInfoXmlFile;
    }



    /**
     * this method is called by the public Constructor or the Combine() method useful operating on this object using streams
     * *  sets the path to .xml file containig the information on current Work
     * this function is used to initialize the WorkMetaInfo object
     * it also resets the the StreamSource for the file so that getWorkMetaInfoAsStreamSource()
     * will create a new Source when called
     * @returns the the WorkMetaInfo object itself so that initialization chaining ala
     * return (new WorkMetaInfo).setPathToWorkMetaInfoXmlFile(x) is possible
     * */

    private WorkMetaInfo setPathToWorkMetaInfoXmlFile(Path pathToWorkMetaInfoXmlFile) {
        if(pathToWorkMetaInfoXmlFile == null || !Files.exists(pathToWorkMetaInfoXmlFile)){
            throw new RuntimeException("Can not set pathToWorkMetaInfoXmlFile to '"
                    + pathToWorkMetaInfoXmlFile
                    +"' this file does not exist");
        }

        if ( this.workMetaInfoAsStreamSource != null && this.getPathToWorkMetaInfoXmlFile()!= pathToWorkMetaInfoXmlFile) {
            this.workMetaInfoAsStreamSource = null;
        }
        this.pathToWorkMetaInfoXmlFile = pathToWorkMetaInfoXmlFile;
        return this;
    }

    /**
     * public constructor initializes the WorkMetaInfo properly and doing error checking by calling setPathToWorkMetaInfoXmlFile()
     * @param pathToWorkMetaInfoXmlFile see setPathToWorkMetaInfoXmlFile(pathToWorkMetaInfoXmlFile)
    for details     */
    public WorkMetaInfo(Path pathToWorkMetaInfoXmlFile){
        setPathToWorkMetaInfoXmlFile(pathToWorkMetaInfoXmlFile);
    }


    /**
     * gets the list of already Id properties (which must be set before by parsing metsmods.xml )
     * @return
     */
    public Properties getAllIds() {
        return ids;
    }


    /**
     * gets the list of already  properties of the Work itsel (like title,etx )
     * (which must be set before by parsing metsmods.xml )
     * @return
     */
    public Properties getProps() {
        return props;
    }

    /**
     * used when properties are read and in the MetsModsFileProcessor using streams
     * it allows to combine two objects of this type into one new object
     * effectively adding a property to a WorkMetaInfo and returning the new object
     * this can happen i.e. in the stream.reduce() or stream.collect() call
     * @param other
     * @return
     */
    public WorkMetaInfo Combine(WorkMetaInfo other) {

        if(this.getPathToWorkMetaInfoXmlFile() !=null
               && other.getPathToWorkMetaInfoXmlFile() != null
               && this.getPathToWorkMetaInfoXmlFile() != other.getPathToWorkMetaInfoXmlFile()){
            throw new RuntimeException("Internal Error: cannot combine two objects." +
                    "WorkMetaInfo.getPathToWorkMetaInfoXmlFile() of combined Objects must be either Equal to the or null");
        }
        WorkMetaInfo combinedWorkMEtaInfo = new WorkMetaInfo( this.getPathToWorkMetaInfoXmlFile() != null
                ? this.getPathToWorkMetaInfoXmlFile() : other.getPathToWorkMetaInfoXmlFile() );

        combinedWorkMEtaInfo.props.putAll(this.props);
        combinedWorkMEtaInfo.props.putAll(other.props);
        combinedWorkMEtaInfo.ids.putAll(this.ids);
        combinedWorkMEtaInfo.ids.putAll(other.ids);
        return combinedWorkMEtaInfo;
    }

    @Override
    public String toString() {
        return "ids:" + ids.toString() + "\nprops:" + props.toString();
    }
}
