package com.example.xogame


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins

class BoardActivity : AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var cells: Array<String?>
    private var currentPlayer: String = "X"
    private var scoreX = 0
    private var scoreO = 0
    private lateinit var currentPlayerTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var repository: GameHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board)

        gridLayout = findViewById(R.id.gridLayout)

        val size = intent.getIntExtra("GRID_SIZE", 3)
        createGrid(size)

        currentPlayerTextView = findViewById(R.id.currentPlayerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)

        val homeButton: Button = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            homeButton()
        }

        val db = GameHistoryDatabase.getDatabase(applicationContext)
        val gameHistoryDao = db.gameHistoryDao()
        repository = GameHistoryAdapter()
    }

    private fun homeButton() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    @SuppressLint("SetTextI18n")
    private fun resetGame() {
        cells.fill(null)
        currentPlayer = "X"

        for (i in 0 until gridLayout.childCount) {
            (gridLayout.getChildAt(i) as Button).text = ""
        }
        scoreX = 0
        scoreO = 0
        scoreTextView.text = "X: $scoreX - O: $scoreO"

        Toast.makeText(this, "Reset Game!", Toast.LENGTH_SHORT).show()
    }

    private fun createGrid(size: Int) {
        cells = Array(size * size) { null }
        val screenWidth = resources.displayMetrics.widthPixels
        val screenDensity = resources.displayMetrics.density
        val buttonMargin = (5 * screenDensity).toInt()
        val buttonSize = (screenWidth / size) - (buttonMargin * 2)

        gridLayout.apply {
            removeAllViews()
            columnCount = size
            rowCount = size

            for (i in 0 until size * size) {
                val button = Button(this@BoardActivity).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = buttonSize
                        height = buttonSize
                        setMargins(buttonMargin)
                        columnSpec = GridLayout.spec(i % size)
                        rowSpec = GridLayout.spec(i / size)
                    }
                    textSize = buttonSize / 3 / screenDensity
                    setOnClickListener { onGridButtonClick(this, size) }
                }
                addView(button)
            }
        }
    }

    private fun onGridButtonClick(button: Button, size: Int) {
        val index = gridLayout.indexOfChild(button)

        if (cells[index].isNullOrEmpty()) {
            cells[index] = currentPlayer // ผู้เล่นมนุษย์ทำการเคลื่อนไหว
            button.text = currentPlayer
            checkGameStatus(size)

            // ทำการเคลื่อนไหวของ AI
            aiMakeMove(size)
        }
    }

    private fun aiMakeMove(size: Int) {
        val emptyCells = cells.mapIndexedNotNull { index, value -> if (value.isNullOrEmpty()) index else null }
        if (emptyCells.isNotEmpty()) {
            val aiMove = emptyCells.random()
            cells[aiMove] = currentPlayer
            (gridLayout.getChildAt(aiMove) as Button).text = currentPlayer

            checkGameStatus(size)
        }
    }

    private fun checkGameStatus(size: Int) {
        if (checkForWin(size)) {
            handleWin()
        } else if (checkForDraw()) {
            handleDraw()
        } else {
            switchPlayer()
        }
    }


    private fun handleWin() {
        showAlert("Player $currentPlayer wins!")
        if (currentPlayer == "X") {
            scoreX++
        } else {
            scoreO++
        }
        scoreTextView.text = "X: $scoreX - O: $scoreO"
        resetGameAfterDelay()
    }

    private fun handleDraw() {
        showAlert("The game is a draw!")
        resetGameAfterDelay()
    }

    private fun resetGameAfterDelay() {
        Handler(Looper.getMainLooper()).postDelayed({
            resetGame()
        }, 5000)
    }

    private fun showAlert(s: String) {
        Toast.makeText(this@BoardActivity, s, Toast.LENGTH_SHORT).show()
    }

    private fun switchPlayer() {
        currentPlayer = if (currentPlayer == "X") "O" else "X"
        currentPlayerTextView.text = "Next Player is $currentPlayer"
    }

    private fun checkForWin(size: Int): Boolean {
        for (i in 0 until size) {
            if ((0 until size).all { cells[i * size + it] == currentPlayer }) return true
            if ((0 until size).all { cells[it * size + i] == currentPlayer }) return true
        }

        if ((0 until size).all { cells[it * size + it] == currentPlayer }) return true
        if ((0 until size).all { cells[it * size + (size - it - 1)] == currentPlayer }) return true
        return false
    }

    private fun checkForDraw(): Boolean {
        return cells.all { !it.isNullOrEmpty() }
    }
}
