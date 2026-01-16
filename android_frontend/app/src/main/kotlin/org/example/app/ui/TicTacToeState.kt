package org.example.app.ui

import android.os.Bundle
import org.example.app.game.TicTacToeGame

/**
 * Holds Tic Tac Toe state and logic without requiring AndroidX ViewModel APIs.
 *
 * This is used because the project uses android.app.Activity directly (not ComponentActivity),
 * and we keep persistence via Activity.onSaveInstanceState.
 */
internal class TicTacToeState {

    private companion object {
        private const val KEY_BOARD = "ttt_board" // String of length 9
        private const val KEY_TURN = "ttt_turn" // "X" or "O"
        private const val KEY_STATUS = "ttt_status" // "InProgress" | "XWins" | "OWins" | "Draw"
        private const val KEY_WIN_LINE = "ttt_win_line" // IntArray
    }

    var board: CharArray = CharArray(9) { TicTacToeGame.EMPTY }
        private set

    var currentTurn: Char = TicTacToeGame.X
        private set

    var status: GameStatus = GameStatus.InProgress
        private set

    var winLine: IntArray? = null
        private set

    /**
     * Convenience flag used by the UI for enabling/disabling moves.
     */
    val gameOver: Boolean
        get() = status != GameStatus.InProgress

    // PUBLIC_INTERFACE
    fun onCellTapped(index: Int): MoveResult {
        /** Applies a move if valid; returns a MoveResult for the UI to react (Snackbar, etc.). */
        if (status != GameStatus.InProgress) return MoveResult.NoOp
        if (index !in 0..8) return MoveResult.NoOp
        if (board[index] != TicTacToeGame.EMPTY) return MoveResult.NoOp

        board[index] = currentTurn

        val line = TicTacToeGame.winningLine(board)
        if (line != null) {
            winLine = line
            status = if (currentTurn == TicTacToeGame.X) GameStatus.XWins else GameStatus.OWins
            return if (currentTurn == TicTacToeGame.X) MoveResult.XWins else MoveResult.OWins
        }

        if (TicTacToeGame.isDraw(board)) {
            winLine = null
            status = GameStatus.Draw
            return MoveResult.Draw
        }

        // Continue playing: toggle player and remain InProgress.
        currentTurn = if (currentTurn == TicTacToeGame.X) TicTacToeGame.O else TicTacToeGame.X
        status = GameStatus.InProgress
        return MoveResult.Continue
    }

    // PUBLIC_INTERFACE
    fun reset() {
        /** Resets the game to an empty board starting with X. */
        board = CharArray(9) { TicTacToeGame.EMPTY }
        currentTurn = TicTacToeGame.X
        status = GameStatus.InProgress
        winLine = null
    }

    // PUBLIC_INTERFACE
    fun statusText(): String {
        /** Returns a simple status string for the UI (e.g., "X's turn", "O wins!", "Draw"). */
        return when (status) {
            GameStatus.InProgress -> "${currentTurn}'s turn"
            GameStatus.XWins -> "X wins!"
            GameStatus.OWins -> "O wins!"
            GameStatus.Draw -> "Draw"
        }
    }

    // PUBLIC_INTERFACE
    fun saveToBundle(outState: Bundle) {
        /** Save game state for process death / recreation. */
        outState.putString(KEY_BOARD, board.concatToString())
        outState.putString(KEY_TURN, currentTurn.toString())
        outState.putString(KEY_STATUS, status.name)
        if (winLine != null) outState.putIntArray(KEY_WIN_LINE, winLine)
    }

    // PUBLIC_INTERFACE
    fun restoreFromBundle(state: Bundle) {
        /** Restore game state for process death / recreation. */
        val savedBoard = state.getString(KEY_BOARD)
        if (savedBoard != null && savedBoard.length == 9) {
            board = savedBoard.toCharArray()
        } else {
            board = CharArray(9) { TicTacToeGame.EMPTY }
        }

        val savedTurn = state.getString(KEY_TURN)?.firstOrNull()
        currentTurn = if (savedTurn == TicTacToeGame.O) TicTacToeGame.O else TicTacToeGame.X

        val savedStatus = state.getString(KEY_STATUS)
        status = savedStatus?.let {
            runCatching { GameStatus.valueOf(it) }.getOrNull()
        } ?: GameStatus.InProgress

        winLine = state.getIntArray(KEY_WIN_LINE)

        // Safety: if status indicates a win, keep any valid line highlight; if not, clear highlight.
        if (status == GameStatus.InProgress || status == GameStatus.Draw) {
            // Draw should not highlight a line.
            winLine = null
        } else {
            // If we restored a win but winLine is missing/invalid, recompute.
            if (winLine == null || winLine?.size != 3) {
                winLine = TicTacToeGame.winningLine(board)
            }
        }
    }
}
