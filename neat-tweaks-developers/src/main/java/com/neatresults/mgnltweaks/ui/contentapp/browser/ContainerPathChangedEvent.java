package com.neatresults.mgnltweaks.ui.contentapp.browser;

import info.magnolia.event.Event;
import info.magnolia.event.EventHandler;

/**
 * Event to mark change of root path.
 */
public class ContainerPathChangedEvent implements Event<ContainerPathChangedEvent.Handler> {

    private final String newPath;

    public ContainerPathChangedEvent(final String newPath) {
        this.newPath = newPath;
    }

    public String getNewPath() {
        return newPath;
    }

    @Override
    public void dispatch(final Handler handler) {
        handler.onPathChanged(this);
    }

    /**
     * Handler.
     */
    public interface Handler extends EventHandler {
        void onPathChanged(ContainerPathChangedEvent event);
    }
}