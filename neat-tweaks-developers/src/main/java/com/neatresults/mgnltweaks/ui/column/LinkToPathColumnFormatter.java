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
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.NeatTweaks4DevelopersModule;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Table;

/**
 * Simple column formatter displaying primary node types for nodes and property type for properties. Useful mainly for debugging and administration.
 */
public class LinkToPathColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> implements ColumnFormatter {

    private static final Logger log = LoggerFactory.getLogger(LinkToPathColumnFormatter.class);
    private EventBus adminEventBus;
    private EventBus eventBus;
    private SubAppContext subAppContext;
    private NeatTweaks4DevelopersModule module;

    @Inject
    public LinkToPathColumnFormatter(Definition definition, @Named(AdmincentralEventBus.NAME) final EventBus adminEventBus, @Named(SubAppEventBus.NAME) final EventBus eventBus, SubAppContext subAppContext, NeatTweaks4DevelopersModule module) {
        super(definition);
        this.adminEventBus = adminEventBus;
        this.eventBus = eventBus;
        this.subAppContext = subAppContext;
        this.module = module;
    }

    @Override
    public Object generateCell(Table source, Object itemId, Object columnId) {
        Item item = source.getItem(itemId);
        Property prop = (item == null) ? null : item.getItemProperty(columnId);

        // Need to check prop.getValue() before prop.getType() to avoid npe if value is null.
        if (prop != null && prop.getValue() != null && prop.getType().equals(String.class)) {
            String path = (String) prop.getValue();

            if (StringUtils.isNotBlank(path) && item instanceof JcrPropertyAdapter) {
                JcrPropertyAdapter jcrProp = (JcrPropertyAdapter) item;
                String propName = null;
                try {
                    propName = jcrProp.getJcrItem().getName();
                } catch (RepositoryException e) {
                    log.debug(e.getMessage(), e);
                }
                // template ID

                if ("templateScript".equals(propName) && path.startsWith("/") && path.endsWith(".ftl")) {
                    return createLinkButton(path, "resources", path, subAppContext, adminEventBus, eventBus, module);
                } else if ("dialog".equals(propName) || "dialogName".equals(propName)) {
                    String title = path;
                    String[] parts = path.split(":");
                    path = "/modules/" + parts[0] + "/dialogs/" + parts[1];
                    return createLinkButton(title, "config", path, subAppContext, adminEventBus, eventBus, module);
                } else if ("id".equals(propName)) {
                    String title = path;
                    String[] parts = path.split(":");
                    path = "/modules/" + parts[0] + "/templates/" + parts[1];
                    return createLinkButton(title, "config", path, subAppContext, adminEventBus, eventBus, module);
                } else if ("extends".equals(propName)) {
                    String title = path;
                    if (!path.startsWith("/")) {
                        try {
                            path = jcrProp.getJcrItem().getParent().getPath() + "/" + path;
                        } catch (RepositoryException e) {
                            log.debug(e.getMessage(), e);
                            // switch back to original path in case it doesn't exist to prevent accidental conversion of relative to absolute
                            path = title;
                        }
                    }
                    return createLinkButton(title, "config", path, subAppContext, adminEventBus, eventBus, module);
                }
            }
            return path;
        }
        if (prop != null && prop.getValue() != null) {
            return "" + prop.getValue();
        }

        return null;
    }

    /**
     * Definition for the formatter.
     */
    public static class Definition extends MetaDataColumnDefinition {
        public Definition() {
            setFormatterClass(LinkToPathColumnFormatter.class);
        }
    }

}
