import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.MessageUpdateEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.io.File
import java.util.*

fun main() {
    Bot()
}

val nonoWords = mutableListOf<String>()
const val commandPrefix = "!"
var commandHandler: CommandHandler? = null
val properties = Properties()

const val BAD_WORDS_FILE = "nonowords"
const val TOKEN_FILE = "token"
const val CONFIG_FILE = "config.dat"

class Bot {
    init {
        val jda = JDABuilder.createDefault(this.javaClass.getResourceAsStream(TOKEN_FILE).reader().readText()).build()
        jda.addEventListener(EventListener())
        jda.awaitReady()
    }
}

class EventListener: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        var propFile = this.javaClass.getResourceAsStream(CONFIG_FILE)

        //properties.load(propFile)

        if(properties.isEmpty) {
            initProperties()
        }

        commandHandler = CommandHandler(SecurityLevel.valueOf(properties["permlvl"].toString()))
        updateNonoWords()
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
                    if (messageContainsNoNoWord(message)) {
                        event.message.delete().queue()
                    }
                }
            }
        }
    }

    override fun onMessageUpdate(event: MessageUpdateEvent) {
        val message = event.message.contentDisplay

        if(messageContainsNoNoWord(message)) {
            event.message.delete().queue()
        }
    }
}

fun isCommand(message: String) = message.startsWith(commandPrefix) ||
              message.toLowerCase() == "ping" ||
              message.toLowerCase() == "ping!"

fun messageContainsNoNoWord(message: String) = nonoWords.any { fullTranslate(message).toLowerCase().contains(it) }

fun updateNonoWords() {
    val s = Bot::javaClass.javaClass.getResourceAsStream(BAD_WORDS_FILE).reader()
    s.forEachLine {
        if(!nonoWords.contains(it)) {
            nonoWords.add(it)
        }
    }
    println(nonoWords)
    s.close()
}

fun initProperties() {
    properties["permlvl"] = "DEFAULT"

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

