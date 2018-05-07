package org.kitodo.mediaserver.importer.core;

import org.kitodo.mediaserver.importer.configuration.XmlTransformerConfig;
import org.springframework.beans.factory.BeanCreationException;

import javax.naming.ConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

public class MetsModsFileXslTransformerFactory {

    public Transformer newTransformer(XmlTransformerConfig xmlTransformerConfig) {
        TransformerFactory tf = TransformerFactory.newInstance();

        StreamSource xslt = null;
        try {
           /* xslt filename existence validation is done by the @IsPathToExistingFile annotation
              in the XmlTransformerConfig.java so no need for it here
                only need to check if the xmltransformerConfig is set
           */

                String errortext="cannot instantiate Transformer with the ";

                if (xmlTransformerConfig == null) {
                    throw new ConfigurationException( errortext+"XmlTransformerConfig: null");
                }
            String tt= xmlTransformerConfig.getXsltFilePath();
            xslt = new StreamSource(xmlTransformerConfig.getXsltFilePath());
            return tf.newTransformer(xslt);
        } catch (Exception se) {
            throw new BeanCreationException("MetsTransformer(xslt Transformer)", "Failed to create a xslTransformer bean", se);
        }
    }
}