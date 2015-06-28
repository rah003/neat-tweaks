package com.neatresults.mgnltweaks.stk;

import info.magnolia.imaging.OutputFormat;
import info.magnolia.imaging.ParameterProvider;
import info.magnolia.module.templatingkit.imaging.generation.STKImageGenerator;
import info.magnolia.module.templatingkit.imaging.generation.STKParameter;

import org.apache.commons.lang3.StringUtils;

/**
 * STK generator that prefers extension/format in the generator over that of existing file.
 */
public class NeatSTKImageGenerator extends STKImageGenerator {
    @Override
    public OutputFormat getOutputFormat(ParameterProvider<STKParameter> params) {
        final STKParameter parameter = params.getParameter();
        OutputFormat format;
        try {
            format = getOutputFormat().clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone the output format to produce a dynamic format.", e);
        }
        String extension = parameter.getExtension();
        if (format.getFormatName() == null) {
            format.setFormatName(extension);
        }

        if ("gif".equals(StringUtils.lowerCase(extension))) {
            format.setCompressionType("lzw");
        } else {
            format.setCompressionType(null);
        }
        return format;
    }

}
