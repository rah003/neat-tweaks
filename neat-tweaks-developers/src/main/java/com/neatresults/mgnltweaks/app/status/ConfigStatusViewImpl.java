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

import info.magnolia.event.EventBus;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.event.AdmincentralEventBus;
import info.magnolia.ui.api.location.Location;
import info.magnolia.ui.api.location.LocationChangedEvent;
import info.magnolia.ui.vaadin.layout.SmallAppLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import com.neatresults.mgnltweaks.ui.contentapp.browser.RerootBrowserLocation;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;

/**
 * View implementation for the mail verify subapp.
 */
public class ConfigStatusViewImpl implements ConfigStatusView {

    private SmallAppLayout root = new SmallAppLayout();
    private Listener listener;
    private SimpleTranslator translator;
    private Item dataSource;

    private Map<String, Object> dataBindings = new HashMap<String, Object>();
    private EventBus adminEventBus;

    @Inject
    public ConfigStatusViewImpl(SimpleTranslator translator, @Named(AdmincentralEventBus.NAME) final EventBus adminEventBus) {
        this.translator = translator;
        this.adminEventBus = adminEventBus;
    }

    @Override
    public void build() {
        Component totalExtends = buildAndBind(ConfigStatusView.EXTENDS_COUNT, translator.translate("neatconfiguration.app.status.extendscount"));
        Component absoluteExtends = buildAndBind(ConfigStatusView.ABS_EXTENDS_COUNT, translator.translate("neatconfiguration.app.status.absoluteextendscount"));
        Component relativeExtends = buildAndBind(ConfigStatusView.REL_EXTENDS_COUNT, translator.translate("neatconfiguration.app.status.relativeextendscount"));
        Component overrideExtends = buildAndBind(ConfigStatusView.OVR_EXTENDS_COUNT, translator.translate("neatconfiguration.app.status.overrideextendscount"));
        Component unresolvedExtends = buildAndBind(ConfigStatusView.EXTENDS_FAIL_COUNT, translator.translate("neatconfiguration.app.status.extendsfailcount"));
        Component unresolvedExtendsList = buildAndBindList(ConfigStatusView.EXTENDS_FAIL_LIST, translator.translate("neatconfiguration.app.status.extendsfaillist"));
        unresolvedExtendsList.addStyleName("neat-extends-list");

        // top title
        FormLayout layout = new FormLayout();
        Label sectionTitle = new Label(translator.translate("neatconfiguration.app.status.top.title"));
        sectionTitle.addStyleName("section-title");
        layout.addComponent(sectionTitle);
        root.addSection(layout);

        // extends
        layout = new FormLayout();
        layout.addComponent(createFieldsetTitle(translator.translate("neatconfiguration.app.status.extends.title")));
        layout.addComponent(totalExtends);
        layout.addComponent(absoluteExtends);
        layout.addComponent(relativeExtends);
        layout.addComponent(overrideExtends);
        layout.addComponent(unresolvedExtends);
        layout.addComponent(unresolvedExtendsList);
        root.addSection(layout);

        // refresh
        layout = new FormLayout();
        Button refreshButton = new Button(translator.translate("neatconfiguration.app.status.refresh.caption"));
        refreshButton.addStyleName("v-button-smallapp");
        refreshButton.addStyleName("commit");
        refreshButton.addClickListener(new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                getListener().refreshData();
                refresh();
            }
        });
        layout.addComponent(refreshButton);
        root.addSection(layout);
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    @Override
    public void setDataSource(Item item) {
        this.dataSource = item;
        refresh();
    }

    private void refresh() {
        for (Entry<String, Object> entry : dataBindings.entrySet()) {
            if (entry.getValue() instanceof Container.Viewer) {
                Container.Viewer field = (Container.Viewer) entry.getValue();
                Property<?> property = dataSource.getItemProperty(entry.getKey());
                Container c = (Container) property.getValue();
                field.getContainerDataSource().removeAllItems();
                field.setContainerDataSource(c);
            } else {
                Property.Viewer field = (Property.Viewer) entry.getValue();
                Property<?> property = dataSource.getItemProperty(entry.getKey());
                field.setPropertyDataSource(property);
            }
        }
    }

    protected Component buildAndBind(String key, String caption) {
        Label field = new Label();
        field.setCaption(caption);
        dataBindings.put(key, field);
        return field;
    }

    protected Component buildAndBindList(String key, String caption) {
        ListSelect field = new ListSelect();
        field.setCaption(caption);
        field.setNullSelectionAllowed(false);
        field.addValueChangeListener((ValueChangeEvent event) -> {
            String path = (String) event.getProperty().getValue();
            // open app (subapp)
            Location location = new RerootBrowserLocation("neatconfiguration", "helperBrowser", path, false);
            adminEventBus.fireEvent(new LocationChangedEvent(location));

        });
        dataBindings.put(key, field);
        return field;
    }

    protected Component createFieldsetTitle(String title) {
        Label fieldsetTitle = new Label(title);
        fieldsetTitle.addStyleName("fieldset-title");
        return fieldsetTitle;
    }

}
