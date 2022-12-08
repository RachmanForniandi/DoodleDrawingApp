package rachman.forniandi.doodledrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.get
import rachman.forniandi.doodledrawingapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mImgButtonCurrentPaint:ImageButton?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //binding.drawingView.setSizeForBrush(20.toFloat())

        mImgButtonCurrentPaint = binding.linePaintColors[1] as ImageButton
        mImgButtonCurrentPaint?.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_pressed))

        binding.imgButtonBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Size: ")
        val smallBtn:ImageButton = brushDialog.findViewById(R.id.ib_small_brush)
        smallBtn.setOnClickListener ({
            binding.drawingView.setSizeForBrush(10.toFloat())
            brushDialog.dismiss()
        })
        val mediumBtn:ImageButton = brushDialog.findViewById(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener({
            binding.drawingView.setSizeForBrush(20.toFloat())
            brushDialog.dismiss()
        })
        val largeBtn:ImageButton = brushDialog.findViewById(R.id.ib_large_brush)
        largeBtn.setOnClickListener ({
            binding.drawingView.setSizeForBrush(30.toFloat())
            brushDialog.dismiss()
        })
        brushDialog.show()
    }

    fun paintClicked(view: View){
        //Toast.makeText(this,"clicked paint", Toast.LENGTH_LONG).show()
        if (view !== mImgButtonCurrentPaint){
            // Update the color
            val imgButton = view as ImageButton
            // Here the tag is used for swaping the current color with previous color.
            // The tag stores the selected view
            val colorTag = imgButton.tag.toString()
            // The color is set as per the selected tag here.
            binding.drawingView.setColor(colorTag)
            // Swap the backgrounds for last active and currently active image button.
            imgButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
            )

            mImgButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )

            mImgButtonCurrentPaint = view

        }
    }
}