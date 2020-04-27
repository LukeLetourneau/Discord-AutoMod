import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.ChannelType
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

class Bot {
    init {
        val jda = JDABuilder.createDefault(File("token").readText()).build()
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
            if(isCommand(message)) {
                val command = Command(message)
                handleCommand(command)
            } else {
                if (message.toLowerCase() == "ping") {
                    event.textChannel.sendMessage("Pong!").queue()
                } else if (messageContainsNoNoWord(message)) {
                    event.message.delete().queue()
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

fun isCommand(message: String) = message.startsWith(commandPrefix)

fun handleCommand(command: Command) {
    println(command)
    when(command.command) {
        "register" -> {
            command.params.forEach {
                File("nonowords.txt").appendText("$it\n")
            }
            updateNonoWords()
        }
    }
}

fun messageContainsNoNoWord(message: String): Boolean {
    // Remove whitespace and convert 1337
    val newMessage = StringBuilder()
    for(c in message) {
        when(c) {
            '0' -> newMessage.append('o')
            '1' -> newMessage.append('i')
            '3' -> newMessage.append('e')
            '5' -> newMessage.append('s')
            '6' -> newMessage.append('b')
            '7' -> newMessage.append('t')
            else -> {
                if(!Character.isWhitespace(c))
                    newMessage.append(c)
            }
        }
    }
    return nonoWords.any { newMessage.toString().toLowerCase().contains(it) }
}

fun updateNonoWords() {
    val s = Scanner(File("nonowords.txt"))
    do {
        with(s.nextLine()) {
            if(!nonoWords.contains(this)) {
                nonoWords.add(this)
            }
        }
    } while(s.hasNext())
    println(nonoWords)
    s.close()
}

class Command(val command: String, val params: List<String>) {
    constructor (message: String) : this(
        message.removePrefix(commandPrefix).split(" ").first(),
        message.split(" ").drop(1)
    )

    override fun toString(): String {
        return "$command: params = $params"
    }
}

