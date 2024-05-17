/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.build.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin BUILD = new Plugin("Build", "XBL");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
