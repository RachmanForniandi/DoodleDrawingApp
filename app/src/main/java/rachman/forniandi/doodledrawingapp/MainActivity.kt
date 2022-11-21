package rachman.forniandi.doodledrawingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import rachman.forniandi.doodledrawingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var drawingView:DrawingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawingView.setSizeForBrush(20.toFloat())
    }
}