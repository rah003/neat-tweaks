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
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.event.ContentChangedEvent;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.contentapp.browser.ConfiguredBrowserSubAppDescriptor;
import info.magnolia.ui.vaadin.integration.contentconnector.ConfiguredJcrContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnectorDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeItemId;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.neatresults.mgnltweaks.NeatTweaks4DevelopersModule;
import com.neatresults.mgnltweaks.ui.contentapp.browser.RerootBrowserLocation;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.NativeButton;

/**
 * Methods used to generate content of various columns.
 */
public class ColumnFormatterUtils {

    private static final Logger log = LoggerFactory.getLogger(ColumnFormatterUtils.class);

    public static Object createLinkButton(String title, String workspace, String path, SubAppContext subAppContext, EventBus adminEventBus, EventBus eventBus, NeatTweaks4DevelopersModule module) {
        try {
            Session session = MgnlContext.getJCRSession(workspace);
            String appName = null;
            if ("website".equals(workspace)) {
                appName = "pages";
                Node node = session.getNode(path);
                if (!NodeTypes.Page.NAME.equals(node.getPrimaryNodeType().getName())) {
                    node = NodeUtil.getNearestAncestorOfType(node, NodeTypes.Page.NAME);
                }
                path = node.getPath();
                return createButton(title, appName, "browser", path, path, "", adminEventBus, eventBus, module);
            } else if ("resources".equals(workspace)) {
                appName = "resources";
                return createButton(title, appName, "browser", path, path, "", adminEventBus, eventBus, module);
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
                return createButton(title, appName, "helperBrowser", path, new JcrNodeItemId(node.getIdentifier(), workspace), rootPath, adminEventBus, eventBus, module);
            }
        } catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
        }
        // need to return title (original value) as path might have been transformed already
        return title;
    }

    private static Button createButton(final String title, final String appName, final String subAppName, final String path, final Object itemId, final String rootPath, EventBus adminEventBus, EventBus eventBus, NeatTweaks4DevelopersModule module) {
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
                } else {
                    // open app (subapp)
                    Location location = new RerootBrowserLocation(appName, subAppName, workPath, module.isShowSubtreeOnlyInHelper());
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
}
