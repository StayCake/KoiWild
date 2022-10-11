package com.koisv.koiwild

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameRule
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import java.time.LocalDateTime

class WildEvents: Listener {
    private val instance = KoiWild.instance

    private fun cancel(e: PlayerEvent) {
        lastActivity[e.player] = LocalDateTime.now()
        if (isAfk[e.player] == 2) {
            e.player.displayName(
                Component.text(e.player.name)
            )
            e.player.playerListName(
                Component.text(e.player.name)
            )
            instance.server.onlinePlayers.forEach {
                it.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                        '&',"&7>> &r${e.player.name}&7님은 이제 &a잠수 상태&7가 아닙니다."
                    )
                )
            }
            e.player.clearTitle()
            isAfk[e.player] = 0
        }
    }

    @EventHandler
    private fun afkMoveCancel(e: PlayerMoveEvent) { cancel(e) }
    @EventHandler
    private fun afkInteractCancel(e: PlayerInteractEvent) { cancel(e) }
    @EventHandler
    private fun afkClientCancel(e: PlayerClientOptionsChangeEvent) { cancel(e) }

    @EventHandler
    private fun playerLeft(e: PlayerQuitEvent) {
        val online = instance.server.onlinePlayers.count()
        if (online == 0) instance.server.worlds.forEach {
            it.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            it.setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            it.setGameRule(GameRule.DISABLE_RAIDS, true)
            it.setGameRule(GameRule.RANDOM_TICK_SPEED, 0)
            it.loadedChunks.forEach { chunk ->
                it.unloadChunk(chunk)
            }
        }
        cancel(e)
        isAfk.remove(e.player)
        lastActivity.remove(e.player)
    }

    @EventHandler
    private fun playerJoin(e: PlayerJoinEvent) {
        lastActivity[e.player] = LocalDateTime.now()
        val online = instance.server.onlinePlayers.count()
        if (online > 0) instance.server.worlds.forEach {
            it.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true)
            it.setGameRule(GameRule.DO_WEATHER_CYCLE, true)
            it.setGameRule(GameRule.DISABLE_RAIDS, false)
            it.setGameRule(GameRule.RANDOM_TICK_SPEED, 3)
        }
    }
}