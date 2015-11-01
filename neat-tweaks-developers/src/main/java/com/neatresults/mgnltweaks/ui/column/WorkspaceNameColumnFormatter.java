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

import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.AbstractColumnDefinition;

import javax.inject.Inject;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Table;

/**
 * Formats a column's value as a date in a compact form.
 */
public class WorkspaceNameColumnFormatter extends AbstractColumnFormatter<AbstractColumnDefinition> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceNameColumnFormatter.class);

    @Inject
    public WorkspaceNameColumnFormatter(AbstractColumnDefinition definition) {
        super(definition);
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Item item = source.getItem(itemId);
        if (item instanceof JcrNodeAdapter) {
            try {
                return ((JcrNodeAdapter) item).getJcrItem().getSession().getWorkspace().getName();
            } catch (RepositoryException e) {
                log.error("Ooops, failed to retrieve workspace name for {} with {}", item, e.getMessage(), e);
            }
        }

        return null;
    }
}
