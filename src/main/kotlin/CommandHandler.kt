import net.dv8tion.jda.api.entities.Member
import java.io.File
import java.io.InputStream
import java.time.Duration

enum class SecurityLevel { RELAXED, DEFAULT, LOCKDOWN }

class CommandHandler(var securityLevel: SecurityLevel) {
    private val permissionLevelMap: MutableMap<Int, MutableList<String>> = mutableMapOf()

    init {
        val permFile = this.javaClass.getResourceAsStream("permissions/" + securityLevel.name)
        constructMapFromFile(permFile)
        permFile.close()
    }

    private fun updateCommandList() {
        val file = File("permissions/" + securityLevel.name)
        var curLvl = 0;

        permissionLevelMap.clear()

        file.forEachLine {
            if(it.toIntOrNull() != null) {
                curLvl = it.toInt()
            } else {
                if(permissionLevelMap[curLvl]?.contains(it) == false) {
                    permissionLevelMap.getOrPut(curLvl) { mutableListOf() }.add(it)
                }
            }
        }
        for(i in 0..3) {
            permissionLevelMap[i]?.forEach { println(it) }
        }
    }

    private fun constructMapFromFile(file: InputStream) {
        var curLvl = 0;
        file.reader().forEachLine {
            if(it.toIntOrNull() != null) {
                curLvl = it.toInt()
            } else {
                permissionLevelMap.getOrPut(curLvl) { mutableListOf() }.add(it)
            }
        }
    }

    private fun getPermLevel(member: Member?): Int {
        if(member?.roles == null) return 0

        return if(member.isOwner) 3
        else if(member.roles.any { it.name == "admin" }) 2
        else if(member.roles.any { it.name == "mod" }) 1
        else 0
    }

    fun handleCommand(command: Command) {
        // Check if user has required perms, if not then don't execute the command
        for(i in getPermLevel(command.event.member) downTo 0) {
            if(command.command in permissionLevelMap[i] ?: listOf<String>()) {
                execute(command)
            }
        }
    }

    private fun execute(command: Command) {
        when(command.command) {
            "register" -> {
                command.params.forEach {
                    File("fortnite\nf○rtnite\nfortntie\nforkknife\n4tnite\nfort_n_ite\nfortnit_e_\nfortnіte\n4ortnite").appendText("$it\n")
                }
                updateBannedWords()
            }
            "warn" -> {
                if(command.event.message.mentionedMembers.size == 1) {
                    command.event.message.textChannel.sendMessage(
                        "Consider yourself warned, ${command.event.message.mentionedMembers[0].user.name}"
                    ).queue()
                }
            }
            "dev" -> {
                if(command.params.isEmpty()) return
                when(command.params[0]) {
                    "u" -> when(command.params[1]) {
                        "nnw" -> updateBannedWords()
                        "c" -> updateCommandList()
                    }
                }
            }
            "purge" -> {
                if(command.params[0].toIntOrNull() != null) {
                    with(command.event.message.textChannel) {
                        this.deleteMessages(
                            history.retrievePast(
                                command.params[0].toInt().coerceAtMost(50) + 1
                            ).complete()
                        ).queue()
                    }
                }
            }
            "ping", "ping!" -> {
                if(command.params.isEmpty()) {
                    command.event.textChannel.sendMessage(
                        "Pong! [took ${Duration.between(
                            command.event.message.timeCreated.toInstant(),
                            java.time.Instant.now()
                        ).toMillis()}ms]"
                    ).queue()
                }
            }
            "help" -> {
                var message = ""
                for(i in getPermLevel(command.event.member) downTo 0) {
                    permissionLevelMap[i]?.forEach { message += "- $it\n" }
                }
                command.event.message.textChannel.sendMessage(message).queue()
            }
            "owo" -> {
                with(command.event.message.textChannel) {
                    sendMessage(
                        history.retrievePast(
                            2
                        ).complete()[1].contentDisplay
                            .replace("[rl]".toRegex(), "w")
                            .replace("[RL]".toRegex(), "W")
                    ).queue()
                }
            }
            "updateLevel" -> {
                try {
                    securityLevel = SecurityLevel.valueOf(command.params[0])
                    updateCommandList()
                    command.event.message.textChannel.sendMessage("Security level has been changed to ${command.params[0]}").queue()
                } catch (e: IllegalArgumentException) {
                    command.event.message.textChannel.sendMessage("${command.params[0]} is not a valid security level!").queue()
                }
            }
            "shutdown" -> {
                shutdown()
            }
        }
    }
}