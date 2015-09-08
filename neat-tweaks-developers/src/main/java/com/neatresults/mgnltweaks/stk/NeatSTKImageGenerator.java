/**
 *
 * Copyright 2015 by Jan Haderka <jan.haderka@neatresults.com>
 *
 * This file is part of neat-tweaks module.
 *
 * Neat-tweaks is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * Neat-tweaks is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with neat-tweaks.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://www.gnu.org/licenses/gpl.txt>
 *
 * Should you require distribution under alternative license in order to
 * use neat-tweaks commercially, please contact owner at the address above.
 *
 */
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
