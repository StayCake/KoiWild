package com.koisv.koiwild

import com.koisv.koiwild.KoiWild.Companion.isAfk
import com.koisv.koiwild.KoiWild.Companion.lastActivity
import com.koisv.koiwild.KoiWild.Companion.protectOn
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AfkTimer: BukkitRunnable() {
    private val instance = KoiWild.instance
    private val scheduler = Bukkit.getScheduler()

    override fun run() {
        lastActivity.forEach { (p, t) ->
            val calcT = t.plusMinutes(0)
            if (calcT.plusSeconds(60).isBefore(LocalDateTime.now())) {
                if (isAfk[p] != 2) {
                    isAfk[p] = 2
                    p.displayName(
                        Component.text(p.name)
                            .append(
                                Component.text(" | 잠수").color(TextColor.color(100,100,100))
                            )
                    )
                    p.playerListName(
                        Component.text(p.name)
                            .append(
                                Component.text(" | 잠수").color(TextColor.color(100,100,100))
                            )
                    )
                    protectOn(p)
                    instance.server.onlinePlayers.forEach {
                        it.sendMessage(Component.text(
                            "&7>> &r${p.name}&7님은 이제 &6잠수 상태&7입니다."
                                .replace(Regex("/&/g"), "§")
                        ))
                        instance.logger.info(">> ${p.name}님은 이제 잠수 상태입니다.")
                    }
                    class ScreenTask : BukkitRunnable() {
                        override fun run() {
                            scheduler.runTaskTimerAsynchronously(
                                instance,
                                Runnable {
                                    if (isAfk[p] == 2)
                                        p.showTitle(
                                            Title.title(
                                                Component.text("- 잠수 상태 -")
                                                    .color(TextColor.color(100, 100, 100))
                                                    .decorate(TextDecoration.BOLD),
                                                Component.text("시작 시간 : ")
                                                    .color(TextColor.color(250, 150, 10))
                                                    .append(
                                                        Component.text(
                                                            t.format(DateTimeFormatter.ofPattern("a hh:mm:ss | MM/dd"))
                                                        ).color(TextColor.color(200,190,50))
                                                    ), Title.Times.times(
                                                    Duration.ZERO, Duration.ofDays(1), Duration.ZERO
                                                )
                                            )
                                        )
                                    else if (scheduler.isCurrentlyRunning(taskId)) scheduler.cancelTask(taskId)
                                }, 0, 1728000
                            )
                        }
                    }
                    ScreenTask().runTask(instance)
                }
            } else if (calcT.plusSeconds(55).isBefore(LocalDateTime.now())) {
                if (isAfk[p] != 1) {
                    var count = 5
                    class TimerTask : BukkitRunnable() {
                        override fun run() {
                            if (count > 0 && isAfk[p] == 1) {
                                p.clearTitle()
                                p.showTitle(
                                    Title.title(
                                        Component.empty(),
                                        Component.text("${count}초 후 잠수 상태로 진입합니다...")
                                            .color(TextColor.color(50, 50, 50)),
                                        Title.Times.times(Duration.ofSeconds(1), Duration.ZERO, Duration.ZERO)
                                    )
                                )
                                count--
                            } else if (!this@TimerTask.isCancelled) this@TimerTask.cancel()
                        }
                    }
                    TimerTask().runTaskTimer(instance, 0, 20)
                    isAfk[p] = 1
                }
            }
        }
    }
}