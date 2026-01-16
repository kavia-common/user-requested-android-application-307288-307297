package org.example.app.ui

/**
 * Represents the outcome after a tap/move attempt.
 */
internal enum class MoveResult {
    Continue,
    XWins,
    OWins,
    Draw,
    NoOp
}
