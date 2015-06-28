package com.neatresults.mgnltweaks;

import org.apache.commons.lang3.StringUtils;

/**
 * Just some neat utility methods.
 */
public class NeatUtil {

    public static String templateIdToPath(String templateId) {
        if (StringUtils.contains(templateId, ":")) {
            return "/modules/" + StringUtils.substringBefore(templateId, ":") + "/templates/" + StringUtils.substringAfter(templateId, ":");
        }
        return null;
    }

}
