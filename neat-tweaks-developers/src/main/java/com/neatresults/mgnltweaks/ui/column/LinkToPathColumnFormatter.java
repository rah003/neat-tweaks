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

import info.magnolia.context.MgnlContext;
import info.magnolia.event.EventBus;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.api.location.LocationController;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.contentapp.browser.ConfiguredBrowserSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;
import info.magnolia.ui.workbench.column.AbstractColumnFormatter;
import info.magnolia.ui.workbench.column.definition.ColumnDefinition;
import info.magnolia.ui.workbench.column.definition.ColumnFormatter;
import info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.ui.contentapp.browser.RerootBrowserLocation;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.Page;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Table;

/**
 * Simple column formatter displaying primary node types for nodes and property type for properties. Useful mainly for debugging and administration.
 */
public class LinkToPathColumnFormatter extends AbstractColumnFormatter<ColumnDefinition> implements ColumnFormatter {

    private static final Logger log = LoggerFactory.getLogger(LinkToPathColumnFormatter.class);
    private LocationController locationController;
    private EventBus adminEventBus;
    private EventBus eventBus;
    private SubAppContext subAppContext;

    @Inject
    public LinkToPathColumnFormatter(Definition definition, LocationController locationController, @Named(AdmincentralEventBus.NAME) final EventBus adminEventBus, @Named(SubAppEventBus.NAME) final EventBus eventBus, SubAppContext subAppContext) {
        super(definition);
        this.locationController = locationController;
        this.adminEventBus = adminEventBus;
        this.eventBus = eventBus;
        this.subAppContext = subAppContext;
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
                    return createLinkButton(path, "templates", StringUtils.substringBefore(path, ".ftl"));
                } else if ("dialog".equals(propName) || "dialogName".equals(propName)) {
                    String title = path;
                    String[] parts = path.split(":");
                    path = "/modules/" + parts[0] + "/dialogs/" + parts[1];
                    return createLinkButton(title, "config", path);
                } else if ("id".equals(propName)) {
                    String title = path;
                    String[] parts = path.split(":");
                    path = "/modules/" + parts[0] + "/templates/" + parts[1];
                    return createLinkButton(title, "config", path);
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
                    return createLinkButton(title, "config", path);
                }
            }
            return path;
        }

        return null;
    }

    private Object createLinkButton(String title, String workspace, String path) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            String appName = null;
            if ("templates".equals(workspace) && session.nodeExists(StringUtils.substringBeforeLast(path, "."))) {
                appName = "inplace-templating";
                return createButton(title, appName, "detail", path, new JcrNodeItemId(session.getNode(StringUtils.substringBeforeLast(path, ".")).getIdentifier(), workspace), "");
            } else if ("config".equals(workspace) && session.nodeExists(path)) {
                String rootPath = "";
                if (subAppContext.getSubAppDescriptor() instanceof ConfiguredBrowserSubAppDescriptor) {
                    ContentConnectorDefinition connector = ((ConfiguredBrowserSubAppDescriptor) subAppContext.getSubAppDescriptor()).getContentConnector();
                    if (connector instanceof ConfiguredJcrContentConnectorDefinition) {
                        rootPath = ((ConfiguredJcrContentConnectorDefinition) connector).getRootPath();
                    }
                }

                appName = subAppContext.getAppContext().getName();
                Node node = session.getNode(path);
                // remove relative segments from path
                path = node.getPath();
                return createButton(title, appName, "helperBrowser", path, new JcrNodeItemId(node.getIdentifier(), workspace), rootPath);
            }
        } catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        // need to return title (original value) as path might have been transformed already
        return title;
    }

    private Button createButton(final String title, final String appName, final String subAppName, final String path, final Object itemId, final String rootPath) {
        Button selectButton = new NativeButton();
        selectButton.addStyleName("neatmagnoliabutton");
        selectButton.setCaption(title);
        selectButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                String workPath = path;
                if (StringUtils.isNotBlank(rootPath) && !"/".equals(rootPath)) {
                    workPath = StringUtils.removeStart(workPath, rootPath);
                }
                if ("browser".equals(subAppName)) {
                    Location location = new BrowserLocation(appName, subAppName, workPath + ":treeview:");
                    adminEventBus.fireEvent(new LocationChangedEvent(location));
                    // FYI: sucks on so many levels, but for some reason vaadin/mangolia will not scroll properly to new location within same browser w/o slight delay introduced by popup window below
                    String message = "window.alert('stand by for beam up to new location')";
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    Page.getCurrent().getJavaScript().execute(message);

                } else {
                    // open app (subapp)
                    Location location = new RerootBrowserLocation(appName, subAppName, workPath, true);
                    adminEventBus.fireEvent(new LocationChangedEvent(location));
                    // expand selected node
                    try {
                        ContentChangedEvent cce = new ContentChangedEvent(JcrItemUtil.getItemId(RepositoryConstants.CONFIG, path), true);
                        eventBus.fireEvent(cce);
                    } catch (RepositoryException e) {
                        log.error("Ooops, failed to retrieve node at path {} and open it while trying to open definition with {}", path, e.getMessage(), e);
                    }
                }
            }
        });
        return selectButton;
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
