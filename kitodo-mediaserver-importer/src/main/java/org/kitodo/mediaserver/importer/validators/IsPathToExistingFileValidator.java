package org.kitodo.mediaserver.importer.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IsPathToExistingFileValidator
        implements ConstraintValidator<IsPathToExistingFile, String> {

    @Override
    public void initialize(final IsPathToExistingFile constraintAnnotation) {
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {


        return !((value == null) || !Files.isRegularFile(Paths.get(value)));
    }
}
