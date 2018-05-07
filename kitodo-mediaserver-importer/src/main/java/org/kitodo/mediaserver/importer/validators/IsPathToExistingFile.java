package org.kitodo.mediaserver.importer.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = {IsPathToExistingFileValidator.class})
@Documented
public @interface IsPathToExistingFile {

    String message() default "value must be a path to an existing File, but a File with this path could not be found";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
