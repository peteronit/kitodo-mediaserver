package org.kitodo.mediaserver.importer.core;

import org.kitodo.mediaserver.core.api.IMetsReader;
import org.kitodo.mediaserver.importer.WorkMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/// implements IMetsReader Interface using Reader/processor/transformer used by the batch infrastructure
public class IMetsReaderImpl implements IMetsReader {

    @Autowired
    private MetsModsReader metsModsReader;
    @Autowired
    private MetsModsFileProcessor metsModsFileProcessor;

    @Override
    public List<String> read(File mets, Map.Entry<String, String>... parameter)
            throws TransformerException, IOException {


        WorkMetaInfo work = metsModsReader.setSingleFilePath(mets).read();
        metsModsFileProcessor.setTransformParameters(parameter);

        return metsModsFileProcessor.processToGetResultAsLines(work).collect(Collectors.toList());


    }


}
