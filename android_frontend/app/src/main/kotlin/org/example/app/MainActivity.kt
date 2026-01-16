package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.TextView
import androidx.core.content.ContextCompat
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

    // Track announcements so we don't spam TalkBack with duplicate "turn" messages.
    private var lastAnnouncedStatus: String? = null

    // Track end-of-game announcements so they are announced exactly once.
    private var lastAnnouncedEndState: MoveResult? = null

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

        // Ensure keyboard navigation works: make each cell focusable and show focus indicator
        // (focus ring is handled in the stateful background drawable).
        for (i in cells.indices) {
            val btn = cells[i]
            btn.isFocusable = true
            btn.isFocusableInTouchMode = true

            btn.setOnClickListener { view ->
                // If game is over or cell is not valid, ignore taps (also UI disables, but this is extra safety).
                if (gameState.gameOver) return@setOnClickListener

                // Small press animation (simple scale) for smooth feedback without extra deps.
                view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(60).withEndAction {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
                }.start()

                val result = gameState.onCellTapped(i)
                render()

                // Announce end-game results (once), and show snackbar.
                when (result) {
                    MoveResult.XWins -> {
                        val msg = getString(R.string.result_x_wins)
                        showResultSnackbar(msg)
                        maybeAnnounceEndState(result, getString(R.string.a11y_announce_x_wins))
                    }

                    MoveResult.OWins -> {
                        val msg = getString(R.string.result_o_wins)
                        showResultSnackbar(msg)
                        maybeAnnounceEndState(result, getString(R.string.a11y_announce_o_wins))
                    }

                    MoveResult.Draw -> {
                        val msg = getString(R.string.result_draw)
                        showResultSnackbar(msg)
                        maybeAnnounceEndState(result, getString(R.string.a11y_announce_draw))
                    }

                    else -> Unit
                }
            }
        }

        findViewById<View>(R.id.resetFab).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setOnClickListener {
                gameState.reset()
                // reset announcement trackers
                lastAnnouncedStatus = null
                lastAnnouncedEndState = null

                render()
                Snackbar.make(root, getString(R.string.action_reset), Snackbar.LENGTH_SHORT).show()
                announceForAccessibility(getString(R.string.action_reset))
            }
        }

        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        gameState.saveToBundle(outState)
    }

    private fun render() {
        // Reflect game state in status text: turn vs win/draw.
        val status = gameState.statusText(this)
        statusText.text = status

        // Announce status updates politely via live region + explicit announcement, but avoid duplicates.
        // Note: end-game announcements are handled separately (once).
        if (!gameState.gameOver) {
            if (status != lastAnnouncedStatus) {
                lastAnnouncedStatus = status
                // "Turn: X"/"Turn: O" is already localized.
                announceForAccessibility(getString(R.string.a11y_announce_turn, status.removePrefix("Turn: ").trim()))
            }
        }

        val winLine = gameState.winLine?.toSet()

        // High-contrast X/O symbols: X uses Ocean primary (blue), O uses Ocean secondary (amber).
        val xColor = ContextCompat.getColor(this, R.color.ocean_primary)
        val oColor = ContextCompat.getColor(this, R.color.ocean_secondary)
        val defaultTextColor = ContextCompat.getColor(this, R.color.ocean_text)
        val disabledTextColor = ContextCompat.getColor(this, R.color.ocean_disabled_text)

        for (i in cells.indices) {
            val c = gameState.board[i]
            val btn = cells[i]

            btn.text = if (c == TicTacToeGame.EMPTY) "" else c.toString()

            btn.setTextColor(
                when (c) {
                    TicTacToeGame.X -> xColor
                    TicTacToeGame.O -> oColor
                    else -> if (gameState.gameOver) disabledTextColor else defaultTextColor
                },
            )

            // Disable re-click of filled cells. Also prevent any moves once game is over.
            val cellEnabled = !gameState.gameOver && c == TicTacToeGame.EMPTY
            btn.isEnabled = cellEnabled

            // Accessible cell description: "Cell [n], empty/X/O"
            btn.contentDescription = cellContentDescription(cellIndex = i, value = c)

            // Background: use stateful backgrounds so focus + press are always visible.
            if (winLine != null && winLine.contains(i)) {
                btn.setBackgroundResource(R.drawable.bg_cell_win_stateful)
            } else {
                btn.setBackgroundResource(R.drawable.bg_cell_stateful)
            }

            // Visual disabled treatment after game over (avoid overdraw: use alpha rather than extra backgrounds).
            btn.alpha = if (gameState.gameOver && c == TicTacToeGame.EMPTY) 0.55f else 1.0f
        }
    }

    private fun showResultSnackbar(message: String) {
        Snackbar
            .make(root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.action_reset)) {
                gameState.reset()
                lastAnnouncedStatus = null
                lastAnnouncedEndState = null
                render()
            }
            .show()
    }

    private fun cellContentDescription(cellIndex: Int, value: Char): String {
        val cellNumber = cellIndex + 1
        return when (value) {
            TicTacToeGame.X -> getString(R.string.a11y_cell_x, cellNumber)
            TicTacToeGame.O -> getString(R.string.a11y_cell_o, cellNumber)
            else -> getString(R.string.a11y_cell_empty, cellNumber)
        }
    }

    private fun maybeAnnounceEndState(result: MoveResult, message: String) {
        if (lastAnnouncedEndState == result) return
        lastAnnouncedEndState = result
        announceForAccessibility(message)
    }

    private fun announceForAccessibility(message: String) {
        // Send an accessibility announcement without adding new UI features.
        root.announceForAccessibility(message)
        root.sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT)
    }
}
