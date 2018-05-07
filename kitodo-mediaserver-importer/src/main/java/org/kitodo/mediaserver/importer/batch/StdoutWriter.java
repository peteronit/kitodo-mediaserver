package org.kitodo.mediaserver.importer.batch;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

// only for batch  debugging purposses
public class StdoutWriter<T> implements ItemWriter<T> {


    @Override
    public void write(List<? extends T> items) throws Exception {
        if (items == null) return;
        for (T item : items) {
            System.out.println(item);
        }
    }

}