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
package com.neatresults.mgnltweaks.ui.field;

import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;

import com.neatresults.mgnltweaks.NeatUtil;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

import info.magnolia.jcr.RuntimeRepositoryException;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.transformer.basic.BasicTransformer;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.DefaultPropertyUtil;
import info.magnolia.ui.vaadin.integration.jcr.JcrPropertyAdapter;

/**
 * Makes JcrPropertyAdapter ignore script property.
 *
 * @param <T> Type.
 */
public class PropertyIdTransformer<T> extends BasicTransformer<T> {

    public PropertyIdTransformer(Item relatedFormItem, ConfiguredFieldDefinition definition, Class<T> type) {
        super(relatedFormItem, definition, type);
    }

    @Override
    protected <T> Property<T> getOrCreateProperty(Class<T> type, boolean checkTypes) {
        String propertyName = definePropertyName();
        Property<T> property = relatedFormItem.getItemProperty(propertyName);
        if (property == null) {
            if (relatedFormItem instanceof JcrPropertyAdapter) {
                JcrPropertyAdapter jcrProp = (JcrPropertyAdapter) relatedFormItem;
                try {
                    String value = (String) PropertyUtil.getPropertyValueObject(jcrProp.getJcrItem().getParent(), jcrProp.getJcrItem().getName());
                    value = NeatUtil.templateIdToPath(value);
                    Class aType = value.getClass();
                    // no way we can add property to a property, so assume property is the one we are looking for
                    return new DefaultProperty(aType, value);
                } catch (RepositoryException e) {
                    // ignore
                    throw new RuntimeRepositoryException(e);
                }
            }
            property = new DefaultProperty<T>(type, null);
            // here it will fail when propertyName is something else that value or jcrName
            relatedFormItem.addItemProperty(propertyName, property);
        } else if (checkTypes && !type.isAssignableFrom(property.getType())) {
            // solve MGNLUI-2494
            // as we have type inconsistency (type of the jcr value is diff. of the definition one), try to convert the jcr type to the type coming from the definition.
            // get the value as String
            String stringValue = ((property.getValue() != null && StringUtils.isNotBlank(property.getValue().toString()))
                    ? property.getValue().toString()
                            : null);
            T value = null;
            try {
                // Convert the String value to the desired type.
                value = (T) DefaultPropertyUtil.createTypedValue(type, stringValue);
            } catch (Exception e) {
                // Ignore. In case of exception, set a null value.
            }
            property = new DefaultProperty<T>(type, value);
            // This will replace the previous property (with the wrong type) with the new one (correctly typed).
            relatedFormItem.addItemProperty(propertyName, property);
        }

        return property;
    }
}
