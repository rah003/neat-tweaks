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
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.contentapp.ContentSubAppView;
import info.magnolia.ui.contentapp.browser.BrowserLocation;
import info.magnolia.ui.contentapp.browser.BrowserPresenter;
import info.magnolia.ui.contentapp.browser.BrowserSubApp;
import info.magnolia.ui.vaadin.integration.contentconnector.ContentConnector;
import info.magnolia.ui.workbench.WorkbenchPresenter;

import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detail BrowserSubApp used to open more instances of browser in the app, just like if they were details.
 */
public class DetailBrowserSubApp extends BrowserSubApp {

    private static final Logger log = LoggerFactory.getLogger(DetailBrowserSubApp.class);
    private String caption;
    private WorkbenchPresenter workbenchPresenter;
    private String rootPath;
    private EventBus subAppEventBus;

    @Inject
    public DetailBrowserSubApp(ActionExecutor actionExecutor, final SubAppContext subAppContext, final ContentSubAppView view, final BrowserPresenter browser, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            @Named(AdmincentralEventBus.NAME) EventBus adminCentralEventBus, ContentConnector contentConnector, AvailabilityChecker checker) {
        super(actionExecutor, subAppContext, view, browser, subAppEventBus, adminCentralEventBus, contentConnector, checker);
        this.subAppEventBus = subAppEventBus;
        Field field;
        try {
            field = browser.getClass().getDeclaredField("workbenchPresenter");
            field.setAccessible(true);
            workbenchPresenter = (WorkbenchPresenter) field.get(browser);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            log.error("Ooops, your security config might be too tight. Failed to force access to workbench presenter with {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsLocation(Location location) {
        BrowserLocation itemLocation = BrowserLocation.wrap(location);
        String currentPath = getCurrentLocation().getNodePath();
        return currentPath.equals(itemLocation.getNodePath());
    }

    @Override
    public String getCaption() {
        return this.caption;
    }

    @Override
    protected void restoreBrowser(BrowserLocation location) {
        super.restoreBrowser(location);
        caption = StringUtils.reverse(StringUtils.abbreviate(StringUtils.reverse(location.getNodePath()), 20));

        final Object item = contentConnector.getItemIdByUrlFragment(location.getNodePath());
        // expand our item
        workbenchPresenter.expand(item);
    }

    @Override
    public void locationChanged(final Location location) {
        super.locationChanged(location);
        if (rootPath == null && location instanceof RerootBrowserLocation) {
            rootPath = ((RerootBrowserLocation) location).getNodePath();
            subAppEventBus.fireEvent(new ContainerPathChangedEvent(rootPath));
        }
        if (rootPath == null && location instanceof QueryableBrowserLocation) {
            rootPath = ((QueryableBrowserLocation) location).getNodePath();
            subAppEventBus.fireEvent(new ContainerPathChangedEvent(rootPath));
        }
    }
}
