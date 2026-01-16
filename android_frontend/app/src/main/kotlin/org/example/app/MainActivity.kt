package org.example.app

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.example.app.game.TicTacToeGame
import org.example.app.ui.MoveResult
import org.example.app.ui.TicTacToeState

class MainActivity : Activity() {

    private val gameState = TicTacToeState()

    private lateinit var root: View
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: MaterialToolbar

    private lateinit var statusText: TextView
    private lateinit var scoreX: TextView
    private lateinit var scoreO: TextView
    private lateinit var scoreDraws: TextView

    private lateinit var cells: Array<MaterialButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            gameState.restoreFromBundle(savedInstanceState)
        }

        root = findViewById(R.id.root)
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolbar)

        statusText = findViewById(R.id.statusText)
        scoreX = findViewById(R.id.scoreX)
        scoreO = findViewById(R.id.scoreO)
        scoreDraws = findViewById(R.id.scoreDraws)

        setupDrawer()

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

            // Accessibility: describe cell position and state; updated during render().
            btn.contentDescription = getString(R.string.cell_content_description, i + 1, getString(R.string.cell_state_empty))

            btn.setOnClickListener { view ->
                // Smooth press feedback: quick scale down/up.
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
            Snackbar.make(root, getString(R.string.action_new_game), Snackbar.LENGTH_SHORT).show()
        }

        render()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        gameState.saveToBundle(outState)
    }

    private fun setupDrawer() {
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val navView: NavigationView = findViewById(R.id.navigationView)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_new_game -> {
                    gameState.reset()
                    render()
                    Snackbar.make(root, getString(R.string.action_new_game), Snackbar.LENGTH_SHORT).show()
                }

                R.id.action_reset_scores -> {
                    gameState.resetScores()
                    render()
                    Snackbar.make(root, getString(R.string.action_reset_scores), Snackbar.LENGTH_SHORT).show()
                }

                R.id.action_about -> {
                    Snackbar.make(root, getString(R.string.drawer_subtitle), Snackbar.LENGTH_SHORT).show()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun render() {
        // Turn indicator with clearer emphasis and state.
        statusText.text = if (gameState.gameOver) {
            getString(R.string.status_game_over)
        } else {
            getString(R.string.status_turn, gameState.currentTurn.toString())
        }

        // Score row.
        scoreX.text = getString(R.string.score_x, gameState.scoreX)
        scoreO.text = getString(R.string.score_o, gameState.scoreO)
        scoreDraws.text = getString(R.string.score_draws, gameState.scoreDraws)

        val winLine = gameState.winLine?.toSet()

        for (i in cells.indices) {
            val c = gameState.board[i]
            val btn = cells[i]

            val isEmpty = c == TicTacToeGame.EMPTY
            btn.text = if (isEmpty) "" else c.toString()
            btn.isEnabled = !gameState.gameOver && isEmpty

            // Per-mark accent colors (clearer X/O) + gentle pop animation when a mark appears.
            if (!isEmpty) {
                val colorRes = if (c == TicTacToeGame.X) R.color.ocean_x_accent else R.color.ocean_o_accent
                btn.setTextColor(getColor(colorRes))

                // Pop-in animation: only when newly set (heuristic: if scale is 1 already, still safe).
                btn.animate().cancel()
                btn.scaleX = 0.92f
                btn.scaleY = 0.92f
                btn.animate().scaleX(1f).scaleY(1f).setDuration(140).start()
            } else {
                btn.setTextColor(getColor(R.color.ocean_text))
            }

            // Win highlight.
            if (winLine != null && winLine.contains(i)) {
                btn.setBackgroundResource(R.drawable.bg_cell_win)
            } else {
                btn.setBackgroundResource(R.drawable.bg_cell)
            }

            // Accessibility: update content description with current state.
            val stateLabel = if (isEmpty) getString(R.string.cell_state_empty) else c.toString()
            btn.contentDescription = getString(R.string.cell_content_description, i + 1, stateLabel)
        }
    }

    private fun showResultSnackbar(message: String) {
        Snackbar
            .make(root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(R.string.action_new_game)) {
                gameState.reset()
                render()
            }
            .show()
    }
}
