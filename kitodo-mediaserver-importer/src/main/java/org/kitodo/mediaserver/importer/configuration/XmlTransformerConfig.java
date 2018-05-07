package org.kitodo.mediaserver.importer.configuration;


import org.kitodo.mediaserver.importer.validators.XsltPathIsSetToExistingFileOrResource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.naming.ConfigurationException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * property configuration for the xmlTransformer bean used for XSL transformation of metsMods .xml files
 * its properties are usually configured under "application"
 */
@ConfigurationProperties(prefix = "xmltransformer")
@Validated
@XsltPathIsSetToExistingFileOrResource
public class XmlTransformerConfig
{


    private String xsltFilePath;

    private boolean filePathIsAbsolute;


    public boolean getFilePathIsAbsolute() {
        return filePathIsAbsolute;
    }

    public void setFilePathIsAbsolute(boolean filePathIsAbsolute) {
        this.filePathIsAbsolute = filePathIsAbsolute;
    }



    public void setXsltFilePath(String xsltFilePath) {
        this.xsltFilePath = xsltFilePath;
    }

    public String getXsltFilePath() throws ConfigurationException {
        return filePathIsAbsolute ? xsltFilePath :  getResourcePath(xsltFilePath);
    }

    private String getResourcePath(String value) throws ConfigurationException {

        URL asResource = getClass().getClassLoader().getResource(value);
        if(asResource==null)throw new ConfigurationException(errorStart+"problem with xsltFilePath property:"
                +"cannot find file:'" + value + "' in the current classpath. try removing the 'classpath: keyword and providing the absolute path to the file'" );

        value= asResource.getFile();
        return value;
    }

    //this part used for validation only
    final String errorStart="Problem with the configuration Properties.  Property xsltFilePath:";

    public XmlTransformerConfig MakeSureXsltFileIsConfigured() throws ConfigurationException {
        String value = this.xsltFilePath;

        if (value == null) {
            throw new ConfigurationException(errorStart+"The required XSLT file is not set, "
                    + "please check your spring configuration.");
        }
        if (!filePathIsAbsolute && getClass().getClassLoader().getResource(value)==null) {
            throw new ConfigurationException(errorStart+ "The required XSLT resource '"
                    + value + "' does not exist.");
        }

        if(!filePathIsAbsolute && value != null )
        {
            //test if resource can be retrieved, this getResourcePath will throw a configurationException otherwise
            getResourcePath(value);
        }

        if (filePathIsAbsolute && !Files.isRegularFile(Paths.get(value))) {
            throw new ConfigurationException(errorStart+ "The required XSLT file '"
                    + value + "' does not exist.");
        }

    return this;
    }


}
