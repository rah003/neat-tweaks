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
package com.neatresults.mgnltweaks.ui.contentapp.browser;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnectorDefinition;
import info.magnolia.ui.workbench.tree.HierarchicalJcrContainer;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootableHierarchicalJcrContainer extends HierarchicalJcrContainer {

    private static final Logger log = LoggerFactory.getLogger(RootableHierarchicalJcrContainer.class);
    private String pagePath;

    public RootableHierarchicalJcrContainer(JcrContentConnectorDefinition definition, final String pagePath) {
        super(definition);
        this.pagePath = pagePath;
    }

    @Override
    protected boolean isNodeVisible(final Node node) throws RepositoryException {
        boolean isNodeVisible = super.isNodeVisible(node);
        if (StringUtils.isNotBlank(getPagePath())) {
            isNodeVisible &= !NodeTypes.Page.NAME.equals(node.getPrimaryNodeType().getName()) || getPagePath().startsWith(node.getPath());
        }
        return isNodeVisible;
    }

    @Override
    public Collection<Item> getRootItemIds() throws RepositoryException {
        if (StringUtils.isNotBlank(getPagePath())) {
            ArrayList<Item> rootIds = new ArrayList<>();
            rootIds.add(getRootNode());
            return rootIds;
        }
        return getChildren(getRootNode());
    }

    @Override
    public boolean isRoot(Item item) throws RepositoryException {
        if (item != null) {
            try {
                int rootDepth = getRootNode().getDepth();
                if (StringUtils.isBlank(getPagePath())) {
                    rootDepth++;
                }
                return item.getDepth() <= rootDepth;
            } catch (RepositoryException e) {
                handleRepositoryException(log, "Cannot determine depth of jcr item", e);
            }
        }
        return true;
    }

    protected Session getSession() throws RepositoryException {
        return MgnlContext.getJCRSession(getWorkspace());
    }

    protected Node getRootNode() throws RepositoryException {
        if (StringUtils.isNotBlank(getPagePath())) {
            return getSession().getNode(getPagePath());
        }
        return getSession().getNode(getConfiguration().getRootPath());
    }

    public String getPagePath() {
        return pagePath;
    }

    public void setPagePath(final String pagePath) {
        this.pagePath = pagePath;
    }
}
