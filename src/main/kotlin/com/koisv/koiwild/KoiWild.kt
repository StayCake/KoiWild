package com.koisv.koiwild

import io.github.monun.kommand.kommand
import net.kyori.adventure.text.Component
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.time.LocalDateTime

class KoiWild: JavaPlugin() {
    companion object {

        lateinit var instance: KoiWild
        lateinit var patch: YamlConfiguration
        lateinit var patchFile: File

        val tempWrite = mutableListOf<String>()
        val lastActivity = mutableMapOf<Player, LocalDateTime>()
        val writeMode = mutableMapOf<Player, Boolean>()
        val isAfk = mutableMapOf<Player, Int>() // 0 : 해제 | 1 : 준비 | 2 : 설정

        fun patchShow(p: Player) {
            p.sendMessage(Component.text("&7≫ &a야생서버 &ev${patch.getDouble("version")} &a패치노트"))
            p.sendMessage(Component.text(""))
            patch.getStringList("notes").forEach {
                p.sendMessage(Component.text("- $it"))
            }
            p.sendMessage(Component.text(""))
            p.sendMessage(Component.text("&7≫ &a나중에 다시 보려면 \"&e/p\"&a를 사용하세요!"))
        }
        fun protectOn(p: Player) {
            p.flySpeed = 0F
            p.isSleepingIgnored = true
            p.canPickupItems = false
            p.isCollidable = false
            p.isInvulnerable = true
            p.isSilent = true
        }
        fun protectOff(p: Player) {
            p.flySpeed = 0.1F
            p.isSleepingIgnored = false
            p.canPickupItems = true
            p.isCollidable = true
            p.isInvulnerable = false
            p.isSilent = false
        }
    }

    override fun onLoad() {
        logger.info("Now Loading...")
    }

    override fun onEnable() {
        logger.info("Enabling - v${pluginMeta.version}")
        instance = this
        server.pluginManager.registerEvents(WildEvents(), this)
        patchFile = File(dataFolder, "patch.yml")
        patch = YamlConfiguration.loadConfiguration(patchFile)

        kommand {
            register("p") {
                then("version" to double(0.0, Double.MAX_VALUE)) {
                    requires { hasPermission(4, "koiwild.patchwrite") }
                    executes {
                        if (tempWrite.isNotEmpty())
                            tempWrite.clear()
                        val version: Double = it["version"]
                        patch.set("version", version)
                        writeMode[it.source.player] = true
                        it.source.player.sendMessage(Component.text("[작성 모드 시작] v$version"))
                    }
                }
                then("u") {
                    requires { hasPermission(4, "koiwild.patchwrite") }
                    executes { if (tempWrite.isNotEmpty()) {
                        tempWrite.removeIf { value -> tempWrite.lastIndexOf(value) == tempWrite.lastIndex }
                    } }
                }
                then("d") {
                    requires { hasPermission(4, "koiwild.patchwrite") }
                    executes {
                        writeMode[it.source.player] = false
                        it.source.player.sendMessage(Component.text(
                            "[작성 모드 종료] v${patch.getDouble("version")}"
                        ))
                        patch.set("notes", tempWrite.toList())
                        patch.save(patchFile)
                    }
                }
                executes {
                    patchShow(it.source.player)
                    config.set(it.source.player.name, patch.getDouble("version"))
                }
            }
        }

        /*kommand {
            register("test", "테스트1") {
                executes { player.sendMessage("Now Testing") }
            }
        }

        getCommand("p")?.setExecutor(Command())
        getCommand("pw")?.setExecutor(Command())*/
        AfkTimer().runTaskTimer(this, 0, 1)
        server.onlinePlayers.forEach { lastActivity[it] = LocalDateTime.now() }
    }

    override fun onDisable() {
        isAfk.forEach { (p, t) -> if (t != 0) WildEvents().cancel(p) }
        saveConfig()
        patch.save(patchFile)
        logger.info("Disabling Now...")
    }
}