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
import info.magnolia.jcr.nodebuilder.NodeBuilder;
import static info.magnolia.jcr.nodebuilder.Ops.*;
import static info.magnolia.jcr.util.NodeTypes.Content;
import static info.magnolia.jcr.util.NodeTypes.ContentNode;
import static info.magnolia.jcr.util.NodeTypes.Folder;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryManager;
import org.apache.commons.lang3.StringUtils;
import javax.jcr.nodetype.NodeType;

this.'app-display-name'   = ctx.get("appName");
parentModule              = ctx.get("appLocation");
this.'app-group'          = ctx.get("appGroup");
this.'app-icon'           = ctx.get("appIcon");
this.'app-repository'     = ctx.get("appRepository");
this.'app-folder-support' = ctx.get("appFolderSupport");

// nothing you need to edit here most of the time, but feel free :D

// has to be valid jcr element name!
this.'app-name'              = StringUtils.remove(this.'app-display-name'.toLowerCase(), " ")
this.'app-workspace'         = this.'app-name'
this.'app-node-type'         = StringUtils.removeEnd(this.'app-name', "s")
this.'app-item-display-name' = StringUtils.removeEnd(this.'app-display-name', "s")
this.'app-item-name'         = StringUtils.removeEnd(this.'app-name', "s")
this.'app-item-name-capped'  = StringUtils.capitalize(this.'app-item-name')


// feeling adventurous? go ahead and redefine the structure. Good luck n' pls bring back all improvements
session = ctx.getJCRSession("config")

new NodeBuilder(session.getNode("/modules"),
    // create app
    getOrAddNode(parentModule, Content.NAME).then(
        getOrAddNode("apps", Content.NAME).then(
            addNode(this.'app-name', ContentNode.NAME).then(
                addProperty("class", "info.magnolia.ui.api.app.registry.ConfiguredAppDescriptor"),
                addProperty("appClass", "info.magnolia.ui.contentapp.ContentApp"),
                addProperty("icon", this.'app-icon'),
                addProperty("label", this.'app-display-name')
            )
        )
    ),
    // launcher
    getNode("ui-admincentral/config/appLauncherLayout/groups").then(
        getOrAddNode(this.'app-group', ContentNode.NAME).then(
            getNode("apps").then(
                addNode(this.'app-name', ContentNode.NAME)
            )
        )
    ),
    // browser subapp
    browser = getNode(parentModule + "/apps/" + this.'app-name').then(
        addNode("subApps", ContentNode.NAME).then(
            addNode("browser", ContentNode.NAME).then(
                addProperty("class", "info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor"),
                addProperty("subAppClass", "info.magnolia.ui.contentapp.browser.BrowserSubApp"),
                addProperty("label", this.'app-display-name'),
                // set content connector
                addNode("contentConnector", ContentNode.NAME).then(
                    addProperty("defaultOrder", "jcrName"),
                    addProperty("includeProperties", "false"),
                    addProperty("rootPath", "/"),
                    addProperty("workspace", this.'app-workspace'),
                    addNode("nodeTypes", ContentNode.NAME).then(
                        addNode("mainNodeType", ContentNode.NAME).then(
                            addProperty("icon", "icon-node-content"),
                            addProperty("name", this.'app-node-type'),
                            addProperty("strict", "true")
                        ),
                        ifTrue(this.'app-folder-support'). then(
                            addNode("folderNodeType", ContentNode.NAME).then(
                                addProperty("icon", "icon-folder-l"),
                                addProperty("name", Folder.NAME),
                                addProperty("strict", "true")
                            )
                        )
                    )
                ),
                addNode("imageProvider", ContentNode.NAME).then(
                    addProperty("class", "info.magnolia.ui.imageprovider.definition.ConfiguredImageProviderDefinition"),
                    addProperty("imageProviderClass", "info.magnolia.ui.imageprovider.DefaultImageProvider"),
                    addProperty("originalImageNodeName", "photo")
                ),
                // set workbench
                addNode("workbench", ContentNode.NAME).then(
                    addProperty("editable", "true"),
                    addProperty("dropConstraintClass", "info.magnolia.ui.workbench.tree.drop.AlwaysTrueDropConstraint"),
                    addNode("contentViews", ContentNode.NAME).then(
                        // add tree view
                        addNode("tree", ContentNode.NAME).then(
                            addProperty("class", "info.magnolia.ui.workbench.tree.TreePresenterDefinition"),
                            addNode("columns", ContentNode.NAME).then(
                                addNode("jcrName", ContentNode.NAME).then(
                                    addProperty("class", "info.magnolia.ui.workbench.column.definition.PropertyColumnDefinition"),
                                    addProperty("editable", "true"),
                                    addProperty("propertyName", "jcrName"),
                                    addProperty("sortable", "true"),
                                    addProperty("label", this.'app-item-display-name' + " name")
                                ),
                                addNode("status", ContentNode.NAME).then(
                                    addProperty("class", "info.magnolia.ui.workbench.column.definition.StatusColumnDefinition"),
                                    addProperty("formatterClass", "info.magnolia.ui.workbench.column.StatusColumnFormatter"),
                                    addProperty("label", "Status"),
                                    addProperty("displayInChooseDialog", "false"),
                                    addProperty("width", "45")
                                ),
                                addNode("moddate", ContentNode.NAME).then(
                                    addProperty("class", "info.magnolia.ui.workbench.column.definition.MetaDataColumnDefinition"),
                                    addProperty("displayInChooseDialog", "false"),
                                    addProperty("formatterClass", "info.magnolia.ui.workbench.column.DateColumnFormatter"),
                                    addProperty("label", "Modification date"),
                                    addProperty("width", "160"),
                                    addProperty("propertyName", "mgnl:lastModified"),
                                    addProperty("sortable", "true")
                                )
                            )
                        )
                    )
                ),
                // configure actions
                addNode("actions", ContentNode.NAME).then(
                    ifTrue(this.'app-folder-support').then(
                        // folder actions
                        addNode("addFolder", ContentNode.NAME).then(
                            addProperty("class", "info.magnolia.ui.framework.action.AddFolderActionDefinition"),
                            addProperty("icon", "icon-add-folder"),
                            addProperty("label", "Add folder"),
                            addNode("availability", ContentNode.NAME).then(
                                addProperty("root", "true"),
                                addProperty("writePermissionRequired", "true"),
                                addNode("nodeTypes", ContentNode.NAME).then(
                                    addProperty("folder", Folder.NAME)
                                ),
                                addNode("rules", ContentNode.NAME).then(
                                    addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                        addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                    )
                                )
                            )
                        ),
                        addNode("editFolder", ContentNode.NAME).then(
                            addProperty("class", "info.magnolia.ui.framework.action.OpenEditDialogActionDefinition"),
                            addProperty("dialogName", "ui-framework:folder"),
                            addProperty("icon", "icon-edit"),
                            addProperty("label", "Rename folder"),
                            addNode("availability", ContentNode.NAME).then(
                                addProperty("extends", "../../addFolder/availability"),
                            )
                        )
                    ),
                    // item actions
                    addNode("add" + this.'app-item-name-capped', ContentNode.NAME).then(
                        addProperty("appName", this.'app-name'),
                        addProperty("class", "info.magnolia.ui.contentapp.detail.action.CreateItemActionDefinition"),
                        addProperty("icon", "icon-add-item"),
                        addProperty("nodeType", this.'app-node-type'),
                        addProperty("subAppId", "detail"),
                        addProperty("label", "Add " + this.'app-item-name-capped'),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("root", "true"),
                            addProperty("writePermissionRequired", "true"),
                            addNode("nodeTypes", ContentNode.NAME).then(
                                addProperty("folder", Folder.NAME)
                            ),
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    ),
                    addNode("edit" + this.'app-item-name-capped', ContentNode.NAME).then(
                        addProperty("appName", this.'app-name'),
                        addProperty("class", "info.magnolia.ui.contentapp.detail.action.EditItemActionDefinition"),
                        addProperty("icon", "icon-edit"),
                        addProperty("subAppId", "detail"),
                        addProperty("label", "Edit " + this.'app-item-name-capped'),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("writePermissionRequired", "true"),
                            addNode("nodeTypes", ContentNode.NAME).then(
                                addProperty(this.'app-item-name', this.'app-node-type')
                            ),
                            addNode("rules", ContentNode.NAME).then(
                                addProperty("extends", "../../../add" + this.'app-item-name-capped' + "/availability/rules")
                            )
                        )
                    ),
                    addNode("rename" + this.'app-item-name-capped', ContentNode.NAME).then(
                        addProperty("class", "info.magnolia.ui.framework.action.OpenEditDialogActionDefinition"),
                        addProperty("icon", "icon-edit"),
                        addProperty("label", "Rename " + this.'app-item-name-capped'),
                        // rename of item is just same as rename of folder (as long as you don't change name property (jcrName by default)
                        addProperty("dialogName", "ui-framework:folder"),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("extends", "../../edit" + this.'app-item-name-capped' + "/availability")
                        )
                    ),
                    // generic
                    addNode("delete", ContentNode.NAME).then(
                        addProperty("class", "info.magnolia.ui.framework.action.MarkNodeAsDeletedActionDefinition"),
                        addProperty("icon", "icon-delete"),
                        addProperty("label", "Delete"),
                        addProperty("command", "markAsDeleted"),
                        addProperty("asynchronous", "true"),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("writePermissionRequired", "true"),
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    ),
                    addNode("activate", ContentNode.NAME).then(
                        addProperty("command", "activate"),
                        addProperty("catalog", "versioned"),
                        addProperty("class", "info.magnolia.ui.framework.action.ActivationActionDefinition"),
                        addProperty("icon", "icon-publish"),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("writePermissionRequired", "true")
                        )
                    ),
                    addNode("activateRecursive", ContentNode.NAME).then(
                        addProperty("extends", "../activate"),
                        addProperty("icon", "icon-publish-incl-sub"),
                        addProperty("recursive","true"),
                        addNode("availability", ContentNode.NAME).then(
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    ),
                    addNode("deactivate", ContentNode.NAME).then(
                        addProperty("extends", "../activate"),
                        addProperty("command", "deactivate"),
                        addProperty("icon", "icon-unpublish"),
                        addNode("availability", ContentNode.NAME).then(
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    ),
                    addNode("export", ContentNode.NAME).then(
                        addProperty("command", "export"),
                        addProperty("icon", "icon-export"),
                        addProperty("class", "info.magnolia.ui.framework.action.ExportActionDefinition"),
                        addNode("availability", ContentNode.NAME).then(
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    ),
                    addNode("import", ContentNode.NAME).then(
                        addProperty("class", "info.magnolia.ui.framework.action.OpenCreateDialogActionDefinition"),
                        addProperty("dialogName", "ui-admincentral:import"),
                        addProperty("icon", "icon-import"),
                        addNode("availability", ContentNode.NAME).then(
                            addProperty("root", "true"),
                            addProperty("writePermissionRequired", "true"),
                            addNode("rules", ContentNode.NAME).then(
                                addNode("IsNotDeletedRule", ContentNode.NAME).then(
                                    addProperty("implementationClass", "info.magnolia.ui.framework.availability.IsNotDeletedRule")
                                )
                            )
                        )
                    )
                ),
                // add action bar
                addNode("actionbar", ContentNode.NAME).then(
                    addProperty("defaultAction", "edit" + this.'app-item-name-capped'),
                    addNode("sections", ContentNode.NAME).then(
                        addNode("root", ContentNode.NAME).then(
                            addProperty("label", this.'app-display-name'),
                            addNode("availability", ContentNode.NAME).then(
                                addProperty("nodes", "false"),
                                addProperty("root", "true")
                            ),
                            addNode("groups", ContentNode.NAME).then(
                                addNode("addActions", ContentNode.NAME).then(
                                    addNode("items", ContentNode.NAME).then(
                                        addNode("add" + this.'app-item-name-capped', ContentNode.NAME),
                                        ifTrue(this.'app-folder-support').then(
                                            addNode("addFolder", ContentNode.NAME)
                                        )
                                    )
                                ),
                                addNode("editActions", ContentNode.NAME).then(
                                    addNode("items", ContentNode.NAME).then(
                                    )
                                ),
                                addNode("publishingActions", ContentNode.NAME).then(
                                    addNode("items", ContentNode.NAME).then(
                                        addNode("activate", ContentNode.NAME),
                                        addNode("activateRecursive", ContentNode.NAME),
                                        addNode("deactivate", ContentNode.NAME)
                                    )
                                ),
                                addNode("eximActions", ContentNode.NAME).then(
                                    addNode("items", ContentNode.NAME).then(
                                        addNode("export", ContentNode.NAME),
                                        addNode("import", ContentNode.NAME)
                                    )
                                )
                            )
                        ),
                        ifTrue(this.'app-folder-support').then(
                            addNode("folder", ContentNode.NAME).then(
                                addProperty("label", "Folder"),
                                addNode("availability", ContentNode.NAME).then(
                                    addNode("nodeTypes", ContentNode.NAME).then(
                                        addProperty("folder", Folder.NAME)
                                    )
                                ),
                                addNode("groups", ContentNode.NAME).then(
                                    addProperty("extends", "../../root/groups"),
                                    addNode("editActions", ContentNode.NAME).then(
                                        addNode("items", ContentNode.NAME).then(
                                            addNode("editFolder", ContentNode.NAME),
                                            addNode("delete", ContentNode.NAME)
                                        )
                                    )
                                )
                            )
                        ),
                        addNode(this.'app-item-name', ContentNode.NAME).then(
                            addProperty("label", this.'app-item-name-capped'),
                            addNode("availability", ContentNode.NAME).then(
                                addNode("nodeTypes", ContentNode.NAME).then(
                                    addProperty(this.'app-item-name', this.'app-node-type')
                                )
                            ),
                            addNode("groups", ContentNode.NAME).then(
                                addProperty("extends", "../../root/groups"),
                                addNode("editActions", ContentNode.NAME).then(
                                    addNode("items", ContentNode.NAME).then(
                                        addNode("edit" + this.'app-item-name-capped', ContentNode.NAME),
                                        addNode("rename" + this.'app-item-name-capped', ContentNode.NAME),
                                        addNode("delete", ContentNode.NAME)
                                    )
                                )
                            )
                        )
                    )
                )
            ), // end of browser
            addNode("detail", ContentNode.NAME).then(
                addProperty("class", "info.magnolia.ui.contentapp.detail.DetailSubAppDescriptor"),
                addProperty("subAppClass", "info.magnolia.ui.contentapp.detail.DetailSubApp"),
                addProperty("label", this.'app-item-display-name'),
                addNode("contentConnector", ContentNode.NAME).then(
                    addProperty("extends", "../../browser/contentConnector")
                ),
                addNode("editor", ContentNode.NAME).then(
                    addProperty("workspace", this.'app-workspace'),
                    addNode("nodeType", ContentNode.NAME).then(
                        addProperty("icon", "icon-items"),
                        addProperty("name", this.'app-node-type')
                    ),
                    addNode("form", ContentNode.NAME).then(
                        addProperty("description", "Define the " + this.'app-item-name' + " information"),
                        addProperty("label", "Edit " + this.'app-item-name'),
                        addNode("tabs", ContentNode.NAME).then(
                            addNode(this.'app-item-name', ContentNode.NAME).then(
                                addProperty("label", this.'app-item-display-name'),
                                addNode("fields", ContentNode.NAME).then(
                                    addNode("name", ContentNode.NAME).then(
                                        addProperty("class", "info.magnolia.ui.form.field.definition.TextFieldDefinition"),
                                        addProperty("label", this.'app-item-name-capped' + " name"),
                                        addProperty("name", "jcrName")
                                    ),
                                    addNode("fileUpload", ContentNode.NAME).then(
                                        addProperty("class", "info.magnolia.dam.app.ui.field.definition.DamUploadFieldDefinition"),
                                        addProperty("label", this.'app-item-name-capped' + " photo"),
                                        addProperty("binaryNodeName", "photo"),
                                        addProperty("allowedMimeTypePattern", "image.*")
                                    )
                                )
                            )
                        )
                    ),
                    addNode("actions", ContentNode.NAME).then(
                        addNode("commit", ContentNode.NAME),
                        addNode("cancel", ContentNode.NAME)
                    )
                ),
                addNode("actions", ContentNode.NAME).then(
                    addNode("commit", ContentNode.NAME).then(
                        addProperty("class", "info.magnolia.ui.form.action.SaveFormActionDefinition"),
                        addProperty("implementationClass","info.magnolia.ui.form.action.SaveFormAction")
                    ),
                    addNode("cancel", ContentNode.NAME).then(
                        addProperty("class", "info.magnolia.ui.form.action.CancelFormActionDefinition")
                    )
                )
            )
        )
    )
).exec()
session.save()

// create workspace
Components.getSingleton(RepositoryManager.class).createWorkspace(this.'app-repository', this.'app-workspace')
// check we registered all right
appSession = ctx.getJCRSession(this.'app-workspace')
// register node type
nodeTypeManager = appSession.getWorkspace().getNodeTypeManager()
type = NodeTypeTemplateUtil.createSimpleNodeType(nodeTypeManager, this.'app-node-type', Arrays.asList(NodeType.NT_HIERARCHY_NODE, NodeType.MIX_REFERENCEABLE, NodeTypes.Created.NAME, NodeTypes.Activatable.NAME, NodeTypes.LastModified.NAME, NodeTypes.Renderable.NAME));
nodeTypeManager.registerNodeType(type, true)
appSession.save()
// double check it registered all right
nodeTypeManager.getNodeType(this.'app-node-type')

// security
roleMan = Components.getSingleton(SecuritySupport.class).roleManager
// add superuser permission to edit the content of the app
roleMan.addPermission(roleMan.getRole("superuser"), this.'app-workspace', "/", 63);
roleMan.addPermission(roleMan.getRole("superuser"), this.'app-workspace', "/*", 63);
// add anonymous permission to view the content of the app
roleMan.addPermission(roleMan.getRole("anonymous"), this.'app-workspace', "/", 8);
roleMan.addPermission(roleMan.getRole("anonymous"), this.'app-workspace', "/*", 8);
// create a base role w/ read access
base = roleMan.createRole(this.'app-name' + "-base")
roleMan.addPermission(base, this.'app-workspace', "/", 8)
roleMan.addPermission(base, this.'app-workspace', "/*", 8)
// create an editor role w/ write access
base = roleMan.createRole(this.'app-name' + "-editor")
roleMan.addPermission(base, this.'app-workspace', "/", 63)
roleMan.addPermission(base, this.'app-workspace', "/*", 63)
// add subscriber mapping for new workspace
new NodeBuilder(session.getNode("/server/activation/subscribers"),
    onChildNodes().then(
        getNode("subscriptions").then(
            addNode(this.'app-workspace', ContentNode.NAME).then(
                addProperty("fromURI", "/"),
                addProperty("repository", this.'app-workspace'),
                addProperty("toURI", "/")
            )
        )
    )
).exec()
session.save()

println "Magnolia content app " + this.'app-display-name' + " have been successfuly created."
return "Go to the app launcher to start you app"