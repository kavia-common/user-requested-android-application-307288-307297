package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import org.example.app.game.TicTacToeGame
import org.example.app.ui.MoveResult
import org.example.app.ui.TicTacToeState

class MainActivity : Activity() {

    private val gameState = TicTacToeState()

    private lateinit var root: View
    private lateinit var statusText: TextView
    private lateinit var cells: Array<MaterialButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            gameState.restoreFromBundle(savedInstanceState)
        }

        root = findViewById(R.id.root)
        statusText = findViewById(R.id.statusText)

        cells = arrayOf(
            findViewById(R.id.cell0),
            findViewById(R.id.cell1),
            findViewById(R.id.cell2),
            findViewById(R.id.cell3),
            findViewById(R.id.cell4),
            findViewById(R.id.cell5),
            findViewById(R.id.cell6),
            findViewById(R.id.cell7),
            findViewById(R.id.cell8),
        )

        for (i in cells.indices) {
            val btn = cells[i]
            btn.setOnClickListener { view ->
                // Small press animation (simple scale) for smooth feedback without extra deps.
                view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(60).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                }.start()

                val result = gameState.onCellTapped(i)
                render()

                when (result) {
                    MoveResult.XWins -> showResultSnackbar(getString(R.string.result_x_wins))
                    MoveResult.OWins -> showResultSnackbar(getString(R.string.result_o_wins))
                    MoveResult.Draw -> showResultSnackbar(getString(R.string.result_draw))
                    else -> Unit
                }
            }
        }

        findViewById<View>(R.id.resetFab).setOnClickListener {
            gameState.reset()
            render()
            Snackbar.make(root, getString(R.string.action_reset), Snackbar.LENGTH_SHORT).show()
        }

        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        gameState.saveToBundle(outState)
    }

    private fun render() {
        statusText.text = getString(R.string.status_turn, gameState.currentTurn.toString())

        val winLine = gameState.winLine?.toSet()

        for (i in cells.indices) {
            val c = gameState.board[i]
            val btn = cells[i]

            btn.text = if (c == TicTacToeGame.EMPTY) "" else c.toString()
            btn.isEnabled = !gameState.gameOver && c == TicTacToeGame.EMPTY

            if (winLine != null && winLine.contains(i)) {
                btn.setBackgroundResource(R.drawable.bg_cell_win)
            } else {
                btn.setBackgroundResource(R.drawable.bg_cell)
            }
        }
    }

    private fun showResultSnackbar(message: String) {
        Snackbar
            .make(root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.action_reset)) {
                gameState.reset()
                render()
            }
            .show()
    }
}
