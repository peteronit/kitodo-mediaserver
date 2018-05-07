package org.kitodo.mediaserver.importer.validators;

import org.kitodo.mediaserver.importer.configuration.XmlTransformerConfig;

import javax.naming.ConfigurationException;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class XsltPathIsSetToExistingFileOrResourceValidator
        implements ConstraintValidator<XsltPathIsSetToExistingFileOrResource,XmlTransformerConfig> {

    public void initialize(XsltPathIsSetToExistingFileOrResource constraintAnnotation) {
    }



    @Override
    public boolean isValid(XmlTransformerConfig config,
                           ConstraintValidatorContext constraintValidatorContext) {
        try {
            config.MakeSureXsltFileIsConfigured();
        } catch (ConfigurationException e) {
            //todo this is not nice to just print out the stackTrace,
            // but there is currently no easy way to add detail information to the validator message
            e.printStackTrace();
            return false;
        }
        return true;
        }
    }
