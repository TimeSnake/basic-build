/*
 * Copyright (C) 2022 timesnake
 */

package de.timesnake.extension.build.chat;

import de.timesnake.library.basic.util.LogHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

    public static final Plugin BUILD = new Plugin("Build", "XBL", LogHelper.getLogger("Build", Level.INFO));

    protected Plugin(String name, String code, Logger logger) {
        super(name, code, logger);
    }
}
