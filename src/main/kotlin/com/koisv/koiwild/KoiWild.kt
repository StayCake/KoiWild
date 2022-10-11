package com.koisv.koiwild

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.time.LocalDateTime

val lastActivity = mutableMapOf<Player, LocalDateTime>()
val isAfk = mutableMapOf<Player, Int>()

class KoiWild: JavaPlugin() {
    companion object {
        lateinit var instance: KoiWild
    }

    override fun onLoad() {
        logger.info("Now Loading...")
    }

    override fun onEnable() {
        logger.info("Enabling - v${description.version}")
        instance = this
        server.pluginManager.registerEvents(WildEvents(), this)
        AfkTimer().runTaskTimer(this, 0, 1)
        server.onlinePlayers.forEach { lastActivity[it] = LocalDateTime.now() }
    }

    override fun onDisable() {
        logger.info("Disabling Now...")
    }
}