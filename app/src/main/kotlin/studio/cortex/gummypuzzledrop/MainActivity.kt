package studio.cortex.gummypuzzledrop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import studio.cortex.gummypuzzledrop.feedback.GameFeedbackController
import studio.cortex.gummypuzzledrop.presentation.GameViewModel
import studio.cortex.gummypuzzledrop.ui.GummyPuzzleDropApp
import studio.cortex.gummypuzzledrop.ui.GummyTheme

class MainActivity : ComponentActivity() {
    private lateinit var feedback: GameFeedbackController
    private var gameModel: GameViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        feedback = GameFeedbackController(this)
        setContent {
            GummyTheme {
                val model: GameViewModel = viewModel()
                val progress by model.progress.collectAsStateWithLifecycle()
                gameModel = model
                LaunchedEffect(model) { model.feedback.collect(feedback::handle) }
                LaunchedEffect(progress) { feedback.updateSettings(progress) }
                GummyPuzzleDropApp(model)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        feedback.setForeground(true)
    }

    override fun onStop() {
        gameModel?.onAppBackground()
        feedback.setForeground(false)
        super.onStop()
    }

    override fun onDestroy() {
        feedback.release()
        super.onDestroy()
    }
}
