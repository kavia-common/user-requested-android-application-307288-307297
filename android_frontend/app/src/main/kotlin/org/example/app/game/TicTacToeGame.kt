package org.example.app.game

/**
 * Pure game logic helpers for Tic Tac Toe.
 *
 * Kept UI-agnostic to make it easy to test and reuse.
 */
internal object TicTacToeGame {

    internal const val EMPTY: Char = ' '
    internal const val X: Char = 'X'
    internal const val O: Char = 'O'

    // All winning triples as board indices 0..8
    private val WIN_LINES: Array<IntArray> = arrayOf(
        intArrayOf(0, 1, 2),
        intArrayOf(3, 4, 5),
        intArrayOf(6, 7, 8),

        intArrayOf(0, 3, 6),
        intArrayOf(1, 4, 7),
        intArrayOf(2, 5, 8),

        intArrayOf(0, 4, 8),
        intArrayOf(2, 4, 6),
    )

    /**
     * Returns the winning line indices (size 3) if there is a winner, otherwise null.
     */
    fun winningLine(board: CharArray): IntArray? {
        for (line in WIN_LINES) {
            val a = board[line[0]]
            if (a == EMPTY) continue
            val b = board[line[1]]
            val c = board[line[2]]
            if (a == b && b == c) {
                return line
            }
        }
        return null
    }

    /**
     * Returns true if all cells are filled and there is no winner.
     */
    fun isDraw(board: CharArray): Boolean {
        if (winningLine(board) != null) return false
        for (c in board) {
            if (c == EMPTY) return false
        }
        return true
    }
}
