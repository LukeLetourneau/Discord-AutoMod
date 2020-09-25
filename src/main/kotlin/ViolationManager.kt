import net.dv8tion.jda.api.entities.User
import java.util.*

enum class ViolationSeverity { WARNING, SEVERE }

class ViolationManager(violationFile: String) {
    private val violationTracker: Properties = Properties()

    init {
        violationTracker.load(getReader(violationFile))
    }

    fun resetViolationsOfSeverity(user: User, severity: ViolationSeverity) {
        val key = genKey(user, severity)
        violationTracker[key] = 0
    }

    fun reportViolation(user: User, severity: ViolationSeverity) {
        val key = genKey(user, severity)
        if(violationTracker.containsKey(key)) {
            violationTracker[key] = (Integer.parseInt(violationTracker[key].toString()) + 1).toString()
            when(severity) {
                ViolationSeverity.SEVERE ->
                    if(getViolationCount(user, severity) >= appProperties.getProperty("severe_to_ban").toInt()) {
                        //Bot.ban(user);
                    }
                ViolationSeverity.WARNING ->
                    if(getViolationCount(user, severity) >= appProperties.getProperty("warnings_to_severe").toInt()) {
                        resetViolationsOfSeverity(user, severity)
                        reportViolation(user, severity)
                    }
            }
        } else {
            violationTracker[key] = 1
        }
    }

    fun getViolationCount(user: User, severity: ViolationSeverity) : Int {
        val key = genKey(user, severity)
        return Integer.parseInt(violationTracker.getOrDefault(key, "0").toString())
    }

    private fun genKey(user: User, severity: ViolationSeverity) = "${severity}_${user.name}${user.discriminator}"
}