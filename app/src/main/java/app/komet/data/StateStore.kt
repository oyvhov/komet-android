package app.komet.data

import app.komet.domain.ActiveRound
import app.komet.domain.AppState
import app.komet.domain.DayStats
import app.komet.domain.LetterCase
import app.komet.domain.Maalform
import app.komet.domain.Profile
import app.komet.domain.RaceMode
import app.komet.domain.Settings
import app.komet.domain.SkillStats
import app.komet.domain.Subject
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * One small JSON file holds every profile and setting. Writes go to a temporary file first and are
 * moved into place, so a crash mid-write never leaves a half-written save behind.
 */
class StateStore(private val file: File) {

    fun load(): AppState {
        if (!file.exists()) return AppState()
        return try {
            decode(JSONObject(file.readText(Charsets.UTF_8)))
        } catch (error: Exception) {
            // Keep the unreadable file for inspection instead of silently overwriting a child's progress.
            file.renameTo(File(file.parentFile, "${file.name}.broken-${System.currentTimeMillis()}"))
            AppState()
        }
    }

    @Synchronized
    fun save(state: AppState) {
        file.parentFile?.mkdirs()
        val temporary = File(file.parentFile, "${file.name}.tmp")
        temporary.writeText(encode(state).toString(), Charsets.UTF_8)
        try {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: Exception) {
            Files.move(temporary.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }

    companion object {
        private const val VERSION = 1

        fun encode(state: AppState): JSONObject = JSONObject().apply {
            put("version", VERSION)
            state.activeProfileId?.let { put("activeProfileId", it) }
            put("settings", JSONObject().apply {
                put("sound", state.settings.sound)
                put("speech", state.settings.speech)
                put("slowSpeech", state.settings.slowSpeech)
                put("haptics", state.settings.haptics)
                put("autoRead", state.settings.autoRead)
                put("dailyGoal", state.settings.dailyGoal)
            })
            put("profiles", JSONArray().apply { state.profiles.forEach { put(encodeProfile(it)) } })
        }

        private fun encodeProfile(profile: Profile): JSONObject = JSONObject().apply {
            put("id", profile.id)
            put("name", profile.name)
            put("avatar", profile.avatar)
            put("grade", profile.grade)
            put("maalform", profile.maalform.name)
            put("letterCase", profile.letterCase.name)
            put("unlockAll", profile.unlockAll)
            put("totalStars", profile.totalStars)
            put("streak", profile.streak)
            put("lastGoalDay", profile.lastGoalDay)
            profile.lastSubject?.let { put("lastSubject", it.name) }
            put("seenCards", profile.seenCards)
            put("createdAt", profile.createdAt)
            put("skills", JSONObject().apply {
                profile.skills.forEach { (id, stats) ->
                    put(id, JSONObject().apply {
                        put("bestStars", stats.bestStars)
                        put("plays", stats.plays)
                        put("answered", stats.answered)
                        put("firstTry", stats.firstTry)
                        put("lastPlayed", stats.lastPlayed)
                    })
                }
            })
            put("days", JSONObject().apply {
                profile.days.forEach { (day, stats) ->
                    put(day.toString(), JSONObject().apply {
                        put("rounds", stats.rounds)
                        put("seconds", stats.seconds)
                        put("answered", stats.answered)
                        put("firstTry", stats.firstTry)
                    })
                }
            })
            put("raceBest", JSONObject().apply { profile.raceBest.forEach { (mode, best) -> put(mode.name, best) } })
            profile.activeRound?.let { round ->
                put("activeRound", JSONObject().apply {
                    put("skillId", round.skillId)
                    put("done", round.done)
                    put("firstTry", round.firstTry)
                    put("total", round.total)
                    put("updatedAt", round.updatedAt)
                })
            }
            put("favorites", JSONArray().apply { profile.favorites.forEach { put(it) } })
        }

        fun decode(json: JSONObject): AppState {
            val settingsJson = json.optJSONObject("settings") ?: JSONObject()
            val defaults = Settings()
            val settings = Settings(
                sound = settingsJson.optBoolean("sound", defaults.sound),
                speech = settingsJson.optBoolean("speech", defaults.speech),
                slowSpeech = settingsJson.optBoolean("slowSpeech", defaults.slowSpeech),
                haptics = settingsJson.optBoolean("haptics", defaults.haptics),
                autoRead = settingsJson.optBoolean("autoRead", defaults.autoRead),
                dailyGoal = settingsJson.optInt("dailyGoal", defaults.dailyGoal).coerceIn(1, 10),
            )
            val profilesJson = json.optJSONArray("profiles") ?: JSONArray()
            val profiles = (0 until profilesJson.length()).mapNotNull { index ->
                profilesJson.optJSONObject(index)?.let(::decodeProfile)
            }
            return AppState(
                profiles = profiles,
                activeProfileId = json.optString("activeProfileId", "").ifBlank { null },
                settings = settings,
            )
        }

        private fun decodeProfile(json: JSONObject): Profile? {
            val id = json.optString("id", "").ifBlank { return null }
            val skillsJson = json.optJSONObject("skills") ?: JSONObject()
            val skills = skillsJson.keys().asSequence().associateWith { key ->
                val stats = skillsJson.getJSONObject(key)
                SkillStats(
                    bestStars = stats.optInt("bestStars", 0).coerceIn(0, 3),
                    plays = stats.optInt("plays", 0),
                    answered = stats.optInt("answered", 0),
                    firstTry = stats.optInt("firstTry", 0),
                    lastPlayed = stats.optLong("lastPlayed", 0L),
                )
            }
            val daysJson = json.optJSONObject("days") ?: JSONObject()
            val days = daysJson.keys().asSequence().mapNotNull { key ->
                val day = key.toLongOrNull() ?: return@mapNotNull null
                val stats = daysJson.getJSONObject(key)
                day to DayStats(
                    rounds = stats.optInt("rounds", 0),
                    seconds = stats.optInt("seconds", 0),
                    answered = stats.optInt("answered", 0),
                    firstTry = stats.optInt("firstTry", 0),
                )
            }.toMap()
            val raceJson = json.optJSONObject("raceBest") ?: JSONObject()
            val raceBest = raceJson.keys().asSequence().mapNotNull { key ->
                enumOrNull<RaceMode>(key)?.let { it to raceJson.optInt(key, 0) }
            }.toMap()
            val activeRound = json.optJSONObject("activeRound")?.let { round ->
                val skillId = round.optString("skillId", "").ifBlank { return@let null }
                val total = round.optInt("total", 0)
                val done = round.optInt("done", 0)
                if (total <= 0 || done !in 1 until total) return@let null
                ActiveRound(
                    skillId = skillId,
                    done = done,
                    firstTry = round.optInt("firstTry", 0).coerceIn(0, done),
                    total = total,
                    updatedAt = round.optLong("updatedAt", 0L),
                )
            }
            val favoritesJson = json.optJSONArray("favorites") ?: JSONArray()
            val favorites = (0 until favoritesJson.length()).mapNotNull { favoritesJson.optString(it, "").ifBlank { null } }.distinct()
            return Profile(
                id = id,
                name = json.optString("name", "").ifBlank { "Romfarar" },
                avatar = json.optInt("avatar", 0),
                grade = json.optInt("grade", 1).coerceIn(0, 3),
                maalform = enumOrNull<Maalform>(json.optString("maalform", "")) ?: Maalform.NYNORSK,
                letterCase = enumOrNull<LetterCase>(json.optString("letterCase", "")) ?: LetterCase.UPPER,
                unlockAll = json.optBoolean("unlockAll", false),
                skills = skills,
                totalStars = json.optInt("totalStars", 0),
                streak = json.optInt("streak", 0),
                lastGoalDay = json.optLong("lastGoalDay", -1L),
                days = days,
                raceBest = raceBest,
                lastSubject = enumOrNull<Subject>(json.optString("lastSubject", "")),
                seenCards = json.optInt("seenCards", 0),
                createdAt = json.optLong("createdAt", 0L),
                activeRound = activeRound,
                favorites = favorites,
            )
        }

        private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
            enumValues<T>().firstOrNull { it.name == name }
    }
}
