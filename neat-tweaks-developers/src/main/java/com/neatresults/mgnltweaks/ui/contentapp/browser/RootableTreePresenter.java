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

import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.vaadin.integration.contentconnector.JcrContentConnector;
import info.magnolia.ui.workbench.container.AbstractJcrContainer;
import info.magnolia.ui.workbench.tree.TreePresenter;
import info.magnolia.ui.workbench.tree.TreeView;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Tree presenter able to dynamically change the root.
 */
public class RootableTreePresenter extends TreePresenter {

    private final SubAppContext subAppContext;

    @Inject
    public RootableTreePresenter(TreeView view, ComponentProvider componentProvider, SubAppContext subAppContext, @Named(SubAppEventBus.NAME) EventBus eventBus) {
        super(view, componentProvider);
        this.subAppContext = subAppContext;
        eventBus.addHandler(ContainerPathChangedEvent.class, new ContainerPathChangedEvent.Handler() {
            @Override
            public void onPathChanged(final ContainerPathChangedEvent event) {
                if (container != null) {
                    ((RootableHierarchicalJcrContainer) container).setPagePath(event.getNewPath());
                    refresh();
                }
            }
        });
    }

    @Override
    protected AbstractJcrContainer createContainer() {
        Location location = subAppContext.getLocation();
        if (location instanceof RerootBrowserLocation) {
            RerootBrowserLocation browserLocation = (RerootBrowserLocation) location;
            if (browserLocation.isShowPageOnly()) {
                return new RootableHierarchicalJcrContainer(((JcrContentConnector) contentConnector).getContentConnectorDefinition(), browserLocation.getNodePath());
            }
        }
        return new RootableHierarchicalJcrContainer(((JcrContentConnector) contentConnector).getContentConnectorDefinition(), null);
    }

}