package org.kitodo.mediaserver.importer.batch;


import org.kitodo.mediaserver.core.db.entities.Identifier;
import org.kitodo.mediaserver.core.db.repositories.IdentifierRepository;
import org.kitodo.mediaserver.core.db.repositories.WorkRepository;
import org.kitodo.mediaserver.importer.WorkPojoWithMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;


//todo: add more logging

/**
 * gets a JPA ItemWriter that
 * writes Out a list of imported Work Objects(including their) Identifiers into Db
 * needs two spring JPA CRUD repositories to be injected like this:
 */
public class JpaWorkPojoWithMetaInfoWriter implements ItemWriter<WorkPojoWithMetaInfo> {
    WorkRepository workRepository;
    IdentifierRepository identifierRepository;
    private final Logger logger = LoggerFactory.getLogger(JpaWorkPojoWithMetaInfoWriter.class);

    /**
     * @param workRepository needed to be set to able able to save Work pojo objects
     * @return the main object itself to be able so chain the setXX call as in a fluent interface
     */
    public JpaWorkPojoWithMetaInfoWriter setWorkRepository(WorkRepository workRepository) {
        this.workRepository = workRepository;
        return this;
    }

    /**
     * @param identifierRepository needed to be set to able able to save Identifier pojo objects
     * @return the main object itself to be able so chain the setXX call as in a fluent interface
     */
    public JpaWorkPojoWithMetaInfoWriter setIdentifierRepository(IdentifierRepository identifierRepository) {
        this.identifierRepository = identifierRepository;
        return this;
    }

    /**
     * writes Work and Identifiers Pojos to database  for each item
     * it only uses the item.getWorkPojo() returned pure Work Pojo object for each Items
     *
     * @param items WorkPojoWithMetaInfo to be written into Database
     */
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void write(List<? extends WorkPojoWithMetaInfo> items) {

        if (items == null || items.size() == 0) return;
        items.stream()
                .map(item -> item.getWorkPojo())
                .forEach(workPojo -> {

                    //remove Work row from Db if it already exists
                    //this will also remove all associate Identifier entries
                    // through the cascade relationship inside the database
                    // created by the  cascade = CascadeType.PERSIST) annotation inside Work Pojo
                    if (workRepository.existsById(workPojo.getId())) {

                        logger.info("Work Id: " + workPojo.getId()
                                + "  Work with this id already exists in the database"
                                + " removing the prexisting Work database Entry and all its associated identifiers");
                        workRepository.deleteById(workPojo.getId());
                    }
                    //this is a workaround for the chicken and egg problem when trying to save
                    //a Work with Identifiers attached to it into a database
                    // it would fail because there are either no associated Identifiers or Work objects already in the Db
                    // so we need to separate them in memory setting setIdentifiers(null) and save the Identifiers Separately
                    // once the Work Object just eased of related Identifiers have been saved
                    Set<Identifier> separatedIdentifiersOfWork = workPojo.getIdentifiers();
                    workPojo.setIdentifiers(null);
                    logger.info("Work Id: " + workPojo.getId()
                            + "  saving the Work to Database");

                    workRepository.save(workPojo);

                    logger.debug("Work Id: " + workPojo.getId()
                            + "  saving all its identifiers to Database");
                    identifierRepository.saveAll(separatedIdentifiersOfWork);
                    logger.debug("Work Id: " + workPojo.getId()
                            + "COMPLETED saving the Work and all its identifiers to Databasee");
                });

    }
}