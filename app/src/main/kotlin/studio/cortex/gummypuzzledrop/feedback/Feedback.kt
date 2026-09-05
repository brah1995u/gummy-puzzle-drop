package studio.cortex.gummypuzzledrop.feedback

enum class FeedbackCue {
    MOVE,
    ROTATE,
    HARD_DROP,
    LAND,
    POP,
    COMBO,
    BOMB,
    BUTTON,
    GAME_OVER,
}

enum class HapticStrength { NONE, LIGHT, MEDIUM, STRONG }

data class FeedbackEvent(
    val cue: FeedbackCue,
    val haptic: HapticStrength = HapticStrength.NONE,
)
