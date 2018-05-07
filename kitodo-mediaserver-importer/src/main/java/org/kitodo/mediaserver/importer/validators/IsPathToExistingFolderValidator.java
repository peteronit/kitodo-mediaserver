package org.kitodo.mediaserver.importer.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IsPathToExistingFolderValidator
        implements ConstraintValidator<IsPathToExistingFolder, String> {

    @Override
    public void initialize(final IsPathToExistingFolder constraintAnnotation) {
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {


        return !((value == null) || (!Files.isDirectory(Paths.get(value))));

    }
}

