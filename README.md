Magnolia Neat Tweaks module
=======================

A [module](https://documentation.magnolia-cms.com/display/DOCS/Modules) containing an [app](https://documentation.magnolia-cms.com/display/DOCS/Apps) for the [Magnolia CMS](http://www.magnolia-cms.com)

Neat Tweaks for Developers replaces existing JCR configuration module with something more user friendly.
Neat Tweaks for Editors changes default theme of Magnolia to give editors more freedom when working with Magnolia.
You can use one or both of the modules.

License
-------

Released under the GPLv3, see LICENSE.txt. 

Feel free to use this app, but if you modify the source code please fork us on Github.

Maven dependency
-----------------
```xml
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-editors</artifactId>
      <version>1.0.2</version>
    </dependency>
    <dependency>
      <groupId>com.neatresults.mgnltweaks</groupId>
      <artifactId>neat-tweaks-developers</artifactId>
      <version>1.0.2</version>
    </dependency>
```

Versions
-----------------
Version 1.0.x compatible with Magnolia 5.3.x
Modules will install and most of the tweaks will also work with MAgnolia 5.4, but full testing have not been performed yet. If you encounter any issues please report them back (ideally with attached patch :D ).

In case you want to just try out in existing installation without need to build everything, please download whole bundle and drop it in your WEB-INF/lib folder. Bundle can be found at https://nexus.magnolia-cms.com/content/repositories/magnolia.forge.releases/com/neatresults/mgnltweaks/neat-tweaks-bundle/1.0.2/neat-tweaks-bundle-1.0.2.zip

