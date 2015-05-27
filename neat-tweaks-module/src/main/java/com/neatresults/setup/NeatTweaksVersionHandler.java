package com.neatresults.setup;

import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.CreateNodeTask;
import info.magnolia.module.delta.MoveNodeTask;
import info.magnolia.module.delta.Task;
import info.magnolia.module.resources.setup.InstallTextResourceTask;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is optional and lets you manager the versions of your module,
 * by registering "deltas" to maintain the module's configuration, or other type of content.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
public class NeatTweaksVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = new ArrayList<Task>(super.getExtraInstallTasks(installContext));
        tasks.add(new InstallTextResourceTask("/admincentral/custom_theme.css"));
        tasks.add(new CreateNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps", "neatconfiguration", NodeTypes.ContentNode.NAME));
        tasks.add(new MoveNodeTask("", "/modules/ui-admincentral/config/appLauncherLayout/groups/manage/apps/configuration", "/modules/ui-admincentral/config/appLauncherLayout/groups/tools/apps/configuration", false));
        return tasks;
    }
}