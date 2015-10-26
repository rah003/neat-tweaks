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
package com.neatresults.mgnltweaks;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.iterator.FilteringNodeIterator;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
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
    private List<ModuleName> preferredModules = new ArrayList<ModuleName>();

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
            List<Node> orderedModules = orderChildNodes(modules);
            // break out order for preferred modules and put those on top of the list
            for (ModuleName name : preferredModules) {
                for (Node n : orderedModules) {
                    if (n.getName().equals(name.getName())) {
                        orderedModules.remove(n);
                        orderedModules.add(0, n);
                        break;
                    }
                }
            }
            // by order, place all at the end of the list
            // this is for modules
            for (Node n : orderedModules) {
                modules.orderBefore(n.getName(), null);
                List<Node> ordered = orderChildNodes(n);
                // by order, place all at the end of the list
                // this is for apps, templates, and other nodes under each module
                for (Node o : ordered) {
                    n.orderBefore(o.getName(), null);
                }

            }
            // save
            configSession.save();
        } catch (RepositoryException e) {

        }
    }

    private List<Node> orderChildNodes(Node parent) throws RepositoryException {
        NodeIterator iter = new FilteringNodeIterator(parent.getNodes(), new AbstractPredicate<Node>() {

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
        return Ordering.from(comparator).sortedCopy(NodeUtil.asIterable(iter));
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

    public List<ModuleName> getPreferredModules() {
        return preferredModules;
    }

    public void setPreferredModules(List<ModuleName> preferredModules) {
        this.preferredModules = preferredModules;
    }

}
