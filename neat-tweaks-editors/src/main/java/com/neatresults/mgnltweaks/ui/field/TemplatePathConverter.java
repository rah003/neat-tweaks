package com.neatresults.mgnltweaks.ui.field;

import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TemplatePathConverter converts node path to link to freemarker template and back (mostly just adding/removing .ftl extension).
 */
public class TemplatePathConverter extends BaseIdentifierToPathConverter {

    private static final Logger log = LoggerFactory.getLogger(TemplatePathConverter.class);

    private String workspace;

    public void setWorkspace(String workspace) {
        super.setWorkspaceName(workspace);
        this.workspace = workspace;
    }

    @Override
    public String convertToModel(String path, Class<? extends String> targetType, Locale locale) throws ConversionException {
        // Null is required for the property to be removed if path is empty
        String res = null;
        if (StringUtils.isBlank(path)) {
            return res;
        }
        res = path + ".ftl";
        return res;
    }

    @Override
    public String convertToPresentation(String path, Class<? extends String> targetType, Locale locale) throws ConversionException {
        String res = StringUtils.EMPTY;
        if (StringUtils.isBlank(path)) {
            return res;
        }
        path = StringUtils.substringBeforeLast(path, ".ftl");
        res = path;
        return res;
    }
}
