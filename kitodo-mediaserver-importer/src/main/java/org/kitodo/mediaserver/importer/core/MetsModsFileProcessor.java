package org.kitodo.mediaserver.importer.core;

import org.kitodo.mediaserver.importer.WorkMetaInfo;
import org.kitodo.mediaserver.importer.WorkMetaInfoMapper;
import org.kitodo.mediaserver.importer.WorkPojoWithMetaInfo;
import org.kitodo.mediaserver.importer.batch.JpaWorkPojoWithMetaInfoWriter;
import org.kitodo.mediaserver.importer.configuration.ImporterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class MetsModsFileProcessor implements ItemProcessor<WorkMetaInfo, WorkPojoWithMetaInfo> {

    private final Logger logger = LoggerFactory.getLogger(JpaWorkPojoWithMetaInfoWriter.class);
    private Transformer transformer;
    private ImporterConfig importerConfig;

    public MetsModsFileProcessor(Transformer transformer, ImporterConfig importerConfig) {
        super();
        this.transformer = transformer;
        this.importerConfig = importerConfig;
    }

    @Override
    public WorkPojoWithMetaInfo process(WorkMetaInfo workMetaInfo) throws Exception {

        logger.info("Processor: parsing metsMods WorkDefinition file");

        Stream<String> linesReturnedByXsl = processToGetResultAsLines(workMetaInfo);

        //TODO: this is all to complicated: the whole
        //takes the information from the original workMetaInfo object
        //and parses each read line line into its property lists
        //the resulting WorkMetaInfo Object contains all properties parsed from the lines
        //then this object is passed into WorkMetaInfoMapper constructor
        //and the latter maps the information from property lists into its own named properties
        //like getId() it also creates a true Work Pojo (including its Identifier Pojo Objects)
        // form this mapped information and returns WorkPojoWithMetaInfo object containig the Pojo
        // and some additional information(actually a file Path from where the file was read) needed
        // when moving files later
        WorkPojoWithMetaInfo workPojoWithMetaInfo = new WorkMetaInfoMapper( workMetaInfo , importerConfig )
                .setPropsFromLines(linesReturnedByXsl)
                .getWorkPojoWithMetaInfo();

        if (workPojoWithMetaInfo.getWorkPojo().getId() == "")
            throw new Exception("WorkMetaInfo.parseProps:(DEBUGGING REMINDER) Id of Parsed xml data after" +
                    " transformation is empty");
        return workPojoWithMetaInfo;
    }

    /// made especially to be able to wo work on a single items basis outside of batch processing for the IMetsReader Interface
    public void setTransformParameters(Map.Entry<String, String>... parameter) {
        transformer.clearParameters();
        Arrays.stream(parameter)
                .forEach(param -> transformer.setParameter(param.getKey(), param.getValue()));
    }

    /// made especially to be able to wo work on a single items basis outside of batch processing for the IMetsReader Interface
    public Stream<String> processToGetResultAsLines(WorkMetaInfo work) throws TransformerException {
        StringWriter writer = new StringWriter();
        transformer.transform(work.getWorkMetaInfoAsStreamSource(), new StreamResult(writer));

        return new BufferedReader(new StringReader(writer.toString()))
                .lines().filter(item -> !item.trim().isEmpty());
    }

}
