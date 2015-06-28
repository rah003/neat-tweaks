package com.neatresults.mgnltweaks.ui.contentapp.browser;

import info.magnolia.event.EventBus;
import info.magnolia.ui.api.action.ActionExecutor;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppEventBus;
import info.magnolia.ui.api.availability.AvailabilityChecker;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.event.ContentChangedEvent;
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

/**
 * Detail BrowserSubApp used to open more instances of browser in the app, just like if they were details.
 */
public class DetailBrowserSubApp extends BrowserSubApp {

    private String caption;
    private EventBus adminCentralEventBus;
    private ContentChangedEvent invokeLater = null;
    private WorkbenchPresenter workbenchPresenter;

    @Inject
    public DetailBrowserSubApp(ActionExecutor actionExecutor, final SubAppContext subAppContext, final ContentSubAppView view, final BrowserPresenter browser, final @Named(SubAppEventBus.NAME) EventBus subAppEventBus,
            @Named(AdmincentralEventBus.NAME) EventBus adminCentralEventBus, ContentConnector contentConnector, AvailabilityChecker checker) {
        super(actionExecutor, subAppContext, view, browser, subAppEventBus, adminCentralEventBus, contentConnector, checker);
        this.adminCentralEventBus = adminCentralEventBus;
        Field field;
        try {
            field = browser.getClass().getDeclaredField("workbenchPresenter");
            field.setAccessible(true);
            workbenchPresenter = (WorkbenchPresenter) field.get(browser);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
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
        // bring it to view manually - something is screwed up in magnolia itself
        // workbenchPresenter.select(item); not working either
    }

}
