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

import static com.neatresults.mgnltweaks.ui.column.ColumnFormatterUtils.createLinkButton;
import info.magnolia.event.EventBus;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.NeatTweaks4DevelopersModule;
import com.vaadin.ui.Table;

/**
 * Displays the path of the item.
 */
public class ListViewLinkToPathColumnFormatter extends AbstractColumnFormatter<MetaDataColumnDefinition> {

    private static final Logger log = LoggerFactory.getLogger(ListViewLinkToPathColumnFormatter.class);

    private EventBus adminEventBus;
    private EventBus eventBus;
    private SubAppContext subAppContext;
    private NeatTweaks4DevelopersModule module;

    @Inject
    public ListViewLinkToPathColumnFormatter(MetaDataColumnDefinition definition, @Named(AdmincentralEventBus.NAME) final EventBus adminEventBus, @Named(SubAppEventBus.NAME) final EventBus eventBus, SubAppContext subAppContext, NeatTweaks4DevelopersModule module) {
        super(definition);
        this.adminEventBus = adminEventBus;
        this.eventBus = eventBus;
        this.subAppContext = subAppContext;
        this.module = module;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        // QueryableJcrContainer container = (QueryableJcrContainer) source.getContainerDataSource();
        // container.getReferencePath()
        final Item jcrItem = getJcrItem(source, itemId);
        if (jcrItem != null) {
            try {
                return createLinkButton(jcrItem.getPath(), jcrItem.getSession().getWorkspace().getName(), jcrItem.getPath(), subAppContext, adminEventBus, eventBus, module);
            } catch (RepositoryException re) {
                log.info("Failed to retrieve path for item '{}':", jcrItem.toString(), re);
            }
        }
        return null;

    }


}
