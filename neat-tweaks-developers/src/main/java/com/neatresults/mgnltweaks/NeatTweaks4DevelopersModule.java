package com.neatresults.mgnltweaks;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.repository.RepositoryConstants;

import java.util.Comparator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.google.common.collect.Ordering;

/**
 * NeatTweaks4DevelopersModule class.
 */
public class NeatTweaks4DevelopersModule implements ModuleLifecycle {

    private boolean showSubtreeOnlyInHelper = true;

    @Override
    public void start(ModuleLifecycleContext ctx) {
        orderModules();
    }

    /**
     * This method keeps all nodes in memory while ordering ... do not use the same for anything bigger than 100 or so items.
     */
    protected void orderModules() {
        try {
            Session configSession = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
            Node modules = configSession.getNode("/modules");
            // get all child nodes
            NodeIterator iter = new FilteringNodeIterator(modules.getNodes(), new AbstractPredicate<Node>() {

                @Override
                public boolean evaluateTyped(Node t) {
                    try {
                        String name = t.getName();
                        return !name.startsWith("jcr:") && !name.startsWith("rep:");
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Comparator<Node> comparator = new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    try {
                        return o1.getName().compareTo(o2.getName());
                    } catch (RepositoryException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
            // order them
            List<Node> ordered = Ordering.from(comparator).sortedCopy(NodeUtil.asIterable(iter));
            // by order, place all at the end of the list
            for (Node n : ordered) {
                modules.orderBefore(n.getName(), null);
            }
            // save
            configSession.save();
        } catch (RepositoryException e) {

        }
    }

    @Override
    public void stop(ModuleLifecycleContext ctx) {

    }

    public boolean isShowSubtreeOnlyInHelper() {
        return showSubtreeOnlyInHelper;
    }

    public void setShowSubtreeOnlyInHelper(boolean showSubtreeOnlyInHelper) {
        this.showSubtreeOnlyInHelper = showSubtreeOnlyInHelper;
    }

}
