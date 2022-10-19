package com.koisv.koiwild

import com.koisv.koiwild.KoiWild.Companion.instance
import hazae41.minecraft.kutils.bukkit.msg
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command : CommandExecutor {
    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>?): Boolean {
        return when (cmd.name) {
            "p" -> {
                if (sender is Player) {
                    KoiWild.patchShow(sender)
                    instance.config.set(sender.name, KoiWild.patch.getDouble("version"))
                }
                true
            }
            "pw" -> {
                if (sender is Player) {
                    val first = args?.get(0)
                    if (first != null) {
                        if (first.toDoubleOrNull() != null) {
                            if (KoiWild.tempWrite.isNotEmpty())
                                KoiWild.tempWrite.clear()
                            val version = first.toDouble()
                            KoiWild.patch.set("version", version)
                            KoiWild.writeMode[sender] = true
                            sender.msg("[작성 모드 시작] v$version")
                            true
                        } else if (first == "u") {
                            if (KoiWild.tempWrite.isNotEmpty()) {
                                KoiWild.tempWrite.removeIf { value ->
                                    KoiWild.tempWrite.lastIndexOf(value) == KoiWild.tempWrite.lastIndex
                                }
                            }
                            true
                        } else if (first == "d") {
                            KoiWild.writeMode[sender] = false
                            sender.msg("[작성 모드 종료] v${KoiWild.patch.getDouble("version")}")
                            instance.server.onlinePlayers.forEach {
                                it.msg("&a시스템 &7>> &e새 패치노트 [v${
                                    KoiWild.patch.getDouble("version")
                                }]&a가 방금 발표되었습니다! &f[/p로 확인하기]")
                            }
                            KoiWild.patch.set("notes", KoiWild.tempWrite.toList())
                            KoiWild.patch.save(KoiWild.patchFile)
                            true
                        } else true
                    } else true
                } else true
            } else -> true
        }
    }
}