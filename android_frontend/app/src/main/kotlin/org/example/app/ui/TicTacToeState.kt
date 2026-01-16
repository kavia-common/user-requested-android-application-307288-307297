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
        private const val KEY_GAME_OVER = "ttt_game_over" // Boolean
        private const val KEY_WIN_LINE = "ttt_win_line" // IntArray
    }

    var board: CharArray = CharArray(9) { TicTacToeGame.EMPTY }
        private set

    var currentTurn: Char = TicTacToeGame.X
        private set

    var gameOver: Boolean = false
        private set

    var winLine: IntArray? = null
        private set

    // PUBLIC_INTERFACE
    fun onCellTapped(index: Int): MoveResult {
        /** Applies a move if valid; returns a MoveResult for the UI to react (Snackbar, etc.). */
        if (gameOver) return MoveResult.NoOp
        if (index !in 0..8) return MoveResult.NoOp
        if (board[index] != TicTacToeGame.EMPTY) return MoveResult.NoOp

        board[index] = currentTurn

        val line = TicTacToeGame.winningLine(board)
        if (line != null) {
            gameOver = true
            winLine = line
            return if (currentTurn == TicTacToeGame.X) MoveResult.XWins else MoveResult.OWins
        }

        if (TicTacToeGame.isDraw(board)) {
            gameOver = true
            winLine = null
            return MoveResult.Draw
        }

        currentTurn = if (currentTurn == TicTacToeGame.X) TicTacToeGame.O else TicTacToeGame.X
        return MoveResult.Continue
    }

    // PUBLIC_INTERFACE
    fun reset() {
        /** Resets the game to an empty board starting with X. */
        board = CharArray(9) { TicTacToeGame.EMPTY }
        currentTurn = TicTacToeGame.X
        gameOver = false
        winLine = null
    }

    // PUBLIC_INTERFACE
    fun saveToBundle(outState: Bundle) {
        /** Save game state for process death / recreation. */
        outState.putString(KEY_BOARD, board.concatToString())
        outState.putString(KEY_TURN, currentTurn.toString())
        outState.putBoolean(KEY_GAME_OVER, gameOver)
        if (winLine != null) outState.putIntArray(KEY_WIN_LINE, winLine)
    }

    // PUBLIC_INTERFACE
    fun restoreFromBundle(state: Bundle) {
        /** Restore game state for process death / recreation. */
        val savedBoard = state.getString(KEY_BOARD)
        if (savedBoard != null && savedBoard.length == 9) {
            board = savedBoard.toCharArray()
        }

        val savedTurn = state.getString(KEY_TURN)?.firstOrNull()
        currentTurn = if (savedTurn == TicTacToeGame.O) TicTacToeGame.O else TicTacToeGame.X

        gameOver = state.getBoolean(KEY_GAME_OVER, false)
        winLine = state.getIntArray(KEY_WIN_LINE)
    }
}
