package com.neatresults.mgnltweaks.ui.contentapp.browser;

import info.magnolia.ui.contentapp.browser.BrowserLocation;

/**
 * Browser location change event capable of reseting root path.
 */
public class RerootBrowserLocation extends BrowserLocation {

    private boolean showPageOnly;

    public RerootBrowserLocation(final String appName, final String subAppId, final String parameter) {
        this(appName, subAppId, parameter, false);
    }

    public RerootBrowserLocation(final String appName, final String subAppId, final String parameter, final boolean showPageOnly) {
        super(appName, subAppId, parameter);
        updateShowPageOnly(showPageOnly);
    }

    @Override
    protected void updateParameter() {
        super.updateParameter();
        if (isShowPageOnly()) {
            setParameter(getParameter() + decodeFragment(";setRoot"));
        }
    }

    public void updateShowPageOnly(final boolean showPageOnly) {
        this.showPageOnly = showPageOnly;
        updateParameter();
    }

    public boolean isShowPageOnly() {
        return showPageOnly;
    }
}