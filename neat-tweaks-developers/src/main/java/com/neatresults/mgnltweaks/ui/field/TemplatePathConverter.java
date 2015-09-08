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
