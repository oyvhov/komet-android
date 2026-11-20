package app.komet.ui

import app.komet.domain.Txt
import app.komet.domain.txt

enum class CompanionReaction { READY, RETRY, HINT, CHEER, EXPLAIN }

data class CompanionMessage(val reaction: CompanionReaction, val title: Txt, val detail: Txt) {
    val speech: Txt get() = title.map { it.trimEnd('.', '!', '?') + ". " } + detail
}

/** Uses only the current task. Answers and explanations stay hidden until it has been answered. */
fun RoundState.companionMessage(): CompanionMessage = when {
    phase == Phase.CORRECT -> CompanionMessage(
        CompanionReaction.CHEER,
        S.praise[praise % S.praise.size],
        question.explanation ?: question.reward ?: S.boltWellDone,
    )
    phase == Phase.REVEALED -> CompanionMessage(
        CompanionReaction.EXPLAIN,
        S.boltTogether,
        question.explanation ?: question.reward ?: S.boltRevealed,
    )
    hintShown && question.hint != null -> CompanionMessage(
        CompanionReaction.HINT,
        S.boltTogether,
        S.boltLookAtHint + txt(" ") + question.speech,
    )
    misses > 0 -> CompanionMessage(
        CompanionReaction.RETRY,
        S.tryAgain,
        S.boltTakeYourTime + txt(" ") + question.speech,
    )
    else -> CompanionMessage(CompanionReaction.READY, S.boltWithYou, question.speech)
}
