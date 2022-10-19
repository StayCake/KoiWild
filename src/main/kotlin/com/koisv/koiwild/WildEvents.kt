package com.koisv.koiwild

import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent
import com.koisv.koiwild.KoiWild.Companion.isAfk
import com.koisv.koiwild.KoiWild.Companion.lastActivity
import com.koisv.koiwild.KoiWild.Companion.patchShow
import com.koisv.koiwild.KoiWild.Companion.protectOff
import com.koisv.koiwild.KoiWild.Companion.tempWrite
import com.koisv.koiwild.KoiWild.Companion.writeMode
import hazae41.minecraft.kutils.bukkit.info
import net.kyori.adventure.text.Component
import org.bukkit.ChatColor
import org.bukkit.GameRule
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.*
import java.time.LocalDateTime

class WildEvents: Listener {
    private val instance = KoiWild.instance

    fun cancel(p: Player) {
        lastActivity[p] = LocalDateTime.now()
        if (isAfk[p] == 2) {
            p.displayName(
                Component.text(p.name)
            )
            p.playerListName(
                Component.text(p.name)
            )
            protectOff(p)
            instance.server.onlinePlayers.forEach {
                it.sendMessage(
                    ChatColor.translateAlternateColorCodes(
                        '&',"&7>> &r${p.name}&7님은 이제 &a잠수 상태&7가 아닙니다."
                    )
                )
                instance.info(">> ${p.name}님은 이제 잠수 상태가 아닙니다.")
            }
            p.clearTitle()
            isAfk[p] = 0
        }
        isAfk[p] = 0
    }

    @EventHandler
    private fun afkMoveCancel(e: PlayerMoveEvent) { cancel(e.player) }
    @EventHandler
    private fun afkInteractCancel(e: PlayerInteractEvent) { cancel(e.player) }
    @EventHandler
    private fun afkClientCancel(e: PlayerClientOptionsChangeEvent) { cancel(e.player) }
    @EventHandler
    private fun playerPreCommand(e: PlayerCommandPreprocessEvent) { cancel(e.player) }
    @EventHandler
    private fun playerCommand(e: PlayerCommandSendEvent) { cancel(e.player) }

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
        cancel(e.player)
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
        if (instance.config.getDouble(e.player.name) != KoiWild.patch.getDouble("version")) {
            instance.config.set(e.player.name, KoiWild.patch.getDouble("version"))
            patchShow(e.player)
        }
    }
    @EventHandler
    private fun playerChat(e: PlayerChatEvent) {
        cancel(e.player)
        if (writeMode[e.player] == true) {
            e.isCancelled = true
            e.player.sendMessage("- ${e.message}")
            tempWrite.add(e.message)
        }
    }
}