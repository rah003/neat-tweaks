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
package com.neatresults.mgnltweaks.ui.column;

import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.column.NTColumnFormatter.Definition;
import com.vaadin.ui.Table;

/**
 * Simple column formatter displaying primary node types for nodes and property type for properties. Useful mainly for debugging and administration.
 */
public class NTColumnFormatter extends AbstractColumnFormatter<Definition> implements ColumnFormatter {

    private static final Logger log = LoggerFactory.getLogger(NTColumnFormatter.class);

    @Inject
    public NTColumnFormatter(Definition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        final Item jcrItem = getJcrItem(source, itemId);
        try {
            if (jcrItem == null) {
                return StringUtils.EMPTY;
            } else if (jcrItem.isNode()) {
                Node node = (Node) jcrItem;
                return "[" + node.getPrimaryNodeType().getName() + "]";
            } else {
                javax.jcr.Property property = (javax.jcr.Property) jcrItem;
                return PropertyType.nameFromValue(property.getType());
            }
        } catch (RepositoryException e) {
            log.warn("Unable to get the displayed node type for the {}", itemId, e);
            return StringUtils.EMPTY;
        }
    }

    /**
     * Definition for the formatter.
     */
    public static class Definition extends AbstractColumnDefinition {
        public Definition() {
            setFormatterClass(NTColumnFormatter.class);
        }
    }
}
