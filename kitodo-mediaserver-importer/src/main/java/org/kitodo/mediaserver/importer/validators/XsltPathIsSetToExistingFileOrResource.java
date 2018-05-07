package org.kitodo.mediaserver.importer.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = { XsltPathIsSetToExistingFileOrResourceValidator.class })
public @interface XsltPathIsSetToExistingFileOrResource {

    String message() default "xsltfilepath property must be set to a path to an existing file "
            + "if filepathisabsolute is true, or to path to an existing internal resource if filepathisabsolute is false";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
