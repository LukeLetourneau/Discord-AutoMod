import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*
import kotlin.system.exitProcess

fun main() {
    Bot()
}

const val BANNED_WORDS_FILE = "bannedwords"
const val TOKEN_FILE = "token"
const val CONFIG_FILE = "config.dat"
const val VIOLATION_FILE = "violations.dat"

val bannedWords = mutableListOf<String>()
const val commandPrefix = "!"
var commandHandler: CommandHandler? = null
val violationManager = ViolationManager(VIOLATION_FILE)
val appProperties = Properties()

class Bot {
    init {
        val jda = JDABuilder.createDefault(getReader(TOKEN_FILE).readText()).build()
        jda.addEventListener(EventListener())
        jda.awaitReady()
    }
}

class EventListener: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        val propFile = getReader(CONFIG_FILE)

        appProperties.load(propFile)

        if(appProperties.isEmpty) {
            initProperties()
        }

        commandHandler = CommandHandler(SecurityLevel.valueOf(appProperties["permlvl"].toString()))
        updateBannedWords()
        println("Bot Ready to go")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if(event.isFromType(ChannelType.TEXT)) {
            val server = event.guild.name
            val channel = event.textChannel.name
            val username = event.member?.user?.name
            val message = event.message.contentDisplay
            println("[$server][$channel] $username: $message")
            if(!event.author.isBot) {
                if (isCommand(message)) {
                    commandHandler?.handleCommand(Command(message, event))
                } else {
                    if (messageContainsBannedWord(message)) {
                        event.message.delete().queue()
                    }
                }
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        val message = event.message.contentDisplay

        if(messageContainsBannedWord(message)) {
            event.message.delete().queue()
        }
    }
}

fun isCommand(message: String) = message.startsWith(commandPrefix) ||
              message.toLowerCase() == "ping" ||
              message.toLowerCase() == "ping!"

fun messageContainsBannedWord(message: String) = bannedWords.any { fullTranslate(message).toLowerCase().contains(it) }

fun updateBannedWords() {
    val s = getReader(BANNED_WORDS_FILE)
    s.forEachLine {
        if(!bannedWords.contains(it)) {
            bannedWords.add(it)
        }
    }
    println(bannedWords)
    s.close()
}

fun initProperties() {
    appProperties["permlvl"] = "DEFAULT"
    appProperties["severe_violations_to_ban"] = "2"
    appProperties["warnings_to_severe"] = "3"
}

fun shutdown() {
    appProperties.store(getWriter(CONFIG_FILE), "Program properties")
    appProperties.store(getWriter(VIOLATION_FILE), "User Violations")
    exitProcess(1)
}

class Command(val command: String, val params: List<String>, val event: MessageReceivedEvent) {
    constructor (message: String, event: MessageReceivedEvent) : this(
        message.removePrefix(commandPrefix).split(" ").first().toLowerCase(),
        message.split(" ").drop(1).map { it.toLowerCase() },
        event
    )

    override fun toString(): String {
        return "$command: params = $params"
    }
}

