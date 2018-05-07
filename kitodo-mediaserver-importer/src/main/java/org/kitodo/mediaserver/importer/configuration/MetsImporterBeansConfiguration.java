package org.kitodo.mediaserver.importer.configuration;


import org.kitodo.mediaserver.core.api.IMetsReader;
import org.kitodo.mediaserver.importer.core.IMetsReaderImpl;
import org.kitodo.mediaserver.importer.core.MetsModsFileProcessor;
import org.kitodo.mediaserver.importer.core.MetsModsFileXslTransformerFactory;
import org.kitodo.mediaserver.importer.core.MetsModsReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.naming.ConfigurationException;
import javax.xml.transform.Transformer;

@Configuration
@EnableConfigurationProperties({ImporterConfig.class, XmlTransformerConfig.class})
public class MetsImporterBeansConfiguration {

    /**
     * the main property configuration of the importer
     *see inside its class for more details
     */
    @Autowired
    private ImporterConfig importerConfig;

    /**
     * property configuration for the xmlTransformer bean used for XSL transformation of metsMods .xml files
     * see inside its class for more details
     */
    @Autowired
    private XmlTransformerConfig xmlTransformerConfig;

    @Bean
    public MetsModsFileProcessor metsModsFileProcessor() throws ConfigurationException {
        MetsModsFileProcessor p = new MetsModsFileProcessor(metsTransformer() , importerConfig);
        return p;
    }

    @Bean
    public IMetsReader iMetsReaderImpl() {

        return new IMetsReaderImpl();
    }

    @Bean
    public Transformer metsTransformer() throws ConfigurationException {

        return (new MetsModsFileXslTransformerFactory())
                .newTransformer(xmlTransformerConfig.MakeSureXsltFileIsConfigured());
    }

    @Bean
    public MetsModsReader metsModsReader() throws ConfigurationException {
        MetsModsReader  metsModsReader= new MetsModsReader(importerConfig);
        return metsModsReader;
    }

}



