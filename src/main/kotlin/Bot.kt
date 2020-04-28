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

fun main(args: Array<String>) {
    Bot()
}

val nonoWords = mutableListOf<String>()
const val commandPrefix = "!"
var commandHandler: CommandHandler? = null

const val BAD_WORDS_FILE = "nonowords"
const val TOKEN_FILE = "token"

class Bot {
    init {
        val jda = JDABuilder.createDefault(this.javaClass.getResourceAsStream(TOKEN_FILE).reader().readText()).build()
        commandHandler = CommandHandler(SecurityLevel.DEFAULT)
        jda.addEventListener(MessageListener())
        jda.addEventListener(ReadyListener())
        jda.awaitReady()
    }
}

class MessageListener: ListenerAdapter() {
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

class ReadyListener : EventListener {
    override fun onEvent(event: GenericEvent) {
        if(event is ReadyEvent) {
            updateNonoWords()
            println("Bot Ready to go")
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

