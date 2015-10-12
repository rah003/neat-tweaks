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