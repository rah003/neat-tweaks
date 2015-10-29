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
package com.neatresults.mgnltweaks.app.status;

import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.repository.RepositoryConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;

/**
 * Presenter for the config status subapp.
 */
public class ConfigStatusPresenter implements ConfigStatusView.Listener {

    private ConfigStatusView view;
    protected Map<String, PropertysetItem> items = new HashMap<String, PropertysetItem>();
    // Object to transport data prepared in the presenter to the view
    protected Item viewData = new PropertysetItem();


    private static final Logger log = LoggerFactory.getLogger(ConfigStatusPresenter.class);

    @Inject
    public ConfigStatusPresenter(ConfigStatusView view) {
        this.view = view;
    }

    public ConfigStatusView start() {
        view.setListener(this);
        view.build();
        refreshData();
        view.setDataSource(viewData);
        return view;
    }

    @Override
    public void refreshData() {
        List<String> fails = new ArrayList<String>();
        final AtomicInteger totalCount = new AtomicInteger();
        final AtomicInteger absCount = new AtomicInteger();
        final AtomicInteger overrideCount = new AtomicInteger();
        try {
            Session session = MgnlContext.getJCRSession(RepositoryConstants.CONFIG);
            NodeIterator results = QueryUtil.search(RepositoryConstants.CONFIG, "select * from [nt:base] where extends is not null");
            filterData(results,
                    n -> {
                        try {
                            String path = n.getProperty("extends").getString();
                            if (StringUtils.startsWith(path, "/")) {
                                absCount.incrementAndGet();
                                return session.itemExists(path);
                            } else if ("override".equals(path)) {
                                overrideCount.incrementAndGet();
                                return true;
                            } else {
                                return session.itemExists(n.getPath() + "/" + path);
                            }
                        } catch (RepositoryException e) {
                            log.debug("Ooops, error while checking existence of extends target for {} with {}", n, e.getMessage(), e);
                            return false;
                        }
                    },
                    t -> totalCount.incrementAndGet(),
                    f -> {
                        try {
                            fails.add(f.getPath());
                        } catch (RepositoryException e) {
                            log.debug("Ooops, error while reporting misconfigured extends target for {} with {}", f, e.getMessage(), e);
                        }
                    });
        } catch (RepositoryException e) {
            log.debug("Ooops, error while searching for extends targets with {}", e.getMessage(), e);
        }
        sourceData(ConfigStatusView.EXTENDS_FAIL_COUNT, "" + fails.size());
        sourceData(ConfigStatusView.EXTENDS_FAIL_LIST, fails);
        sourceData(ConfigStatusView.EXTENDS_COUNT, "" + totalCount.get());
        sourceData(ConfigStatusView.ABS_EXTENDS_COUNT, "" + absCount.get());
        sourceData(ConfigStatusView.REL_EXTENDS_COUNT, "" + (totalCount.get() - absCount.get() - overrideCount.get()));
        sourceData(ConfigStatusView.OVR_EXTENDS_COUNT, "" + overrideCount.get());
    }

    protected void sourceData(String key, Object val) {
        if (viewData.getItemProperty(key) == null) {
            if (val instanceof String) {
                viewData.addItemProperty(key, new ObjectProperty<String>((String) val));
            } else {
                // Creates the options container and add given options to it
                final Container c = new IndexedContainer();
                if (val != null) {
                    c.addContainerProperty("name", String.class, "");
                    for (final Iterator<?> i = ((List) val).iterator(); i.hasNext();) {
                        Item item = c.addItem(i.next());
                    }
                }
                viewData.addItemProperty(key, new ObjectProperty<Container>(c));
            }
        } else {
            viewData.getItemProperty(key).setValue(val);
        }
    }

    private void filterData(NodeIterator iter, Predicate<Node> exists, Consumer<Node> total, Consumer<Node> fails) throws RepositoryException {
        while (iter.hasNext()) {
            Node n = iter.nextNode();
            total.accept(n);
            if (!exists.test(n)) {
                fails.accept(n);
            }
        }
    }
}
