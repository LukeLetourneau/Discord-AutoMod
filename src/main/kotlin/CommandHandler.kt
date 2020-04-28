import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Role
import java.io.File
import java.time.Duration

enum class SecurityLevel { RELAXED, DEFAULT, LOCKDOWN }

class CommandHandler(val securityLevel: SecurityLevel) {
    private val permissionLevelMap: MutableMap<Int, MutableList<String>> = mutableMapOf()

    init {
        val permFile = File("permissions/" + securityLevel.name)
        constructMapFromFile(permFile)
    }

    private fun updateCommandList() {
        val file = File("permissions/" + securityLevel.name)
        var curLvl = 0;
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

    private fun constructMapFromFile(file: File) {
        var curLvl = 0;
        file.forEachLine {
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
                    File("nonowords.txt").appendText("$it\n")
                }
                updateNonoWords()
            }
            "warn" -> {
                if(command.event.message.mentionedMembers.size == 1) {
                    command.event.message.textChannel.sendMessage(
                        "Consider yourself warned, ${command.event.message.mentionedMembers[0].effectiveName}"
                    ).queue()
                }
            }
            "dev" -> {
                if(command.params.isEmpty()) return
                when(command.params[0]) {
                    "u" -> when(command.params[1]) {
                        "nnw" -> updateNonoWords()
                        "c" -> updateCommandList()
                    }
                }
            }
            "purge" -> {
                if(command.params[0].toIntOrNull() != null) {
                    with(command.event.message.textChannel) {
                        this.deleteMessages(history.retrievePast(command.params[0].toInt() + 1).complete()).queue()
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
        }
    }
}