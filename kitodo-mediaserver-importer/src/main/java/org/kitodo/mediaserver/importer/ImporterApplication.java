/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * LICENSE file that was distributed with this source code.
 */

package org.kitodo.mediaserver.importer;

import org.kitodo.mediaserver.core.db.repositories.IdentifierRepository;
import org.kitodo.mediaserver.core.db.repositories.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Starter of the importer application.
 */
@SpringBootApplication
@EnableJpaRepositories( basePackageClasses={UserRepository.class,IdentifierRepository.class})
public class ImporterApplication {

    /**
     * Starts the importer application.
     *
     * @param args external arguments
     */
    public static void main(String[] args) {

        SpringApplication app = new SpringApplicationBuilder(ImporterApplication.class)
                .properties(
                        "spring.config.name:"
                                + "default,"
                                + "local,"
                                + "application,"
                                + "logging")
                .build();

        app.run(args);
    }
}

