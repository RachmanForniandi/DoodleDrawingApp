package rachman.forniandi.doodledrawingapp

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rachman.forniandi.doodledrawingapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var mImgButtonCurrentPaint:ImageButton?= null
    var customProgressDialog: Dialog? = null



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

        binding.imgButtonGallery.setOnClickListener {
            requestStoragePermission()
        }
        binding.imgButtonUndo.setOnClickListener {
            binding.drawingView.onClickUndo()
        }

        binding.imgButtonSave.setOnClickListener {
            if (isReadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch{
                    val myBitmap:Bitmap = getBitmapView(binding.flDrawingViewContainer)
                    saveBitmapFile(myBitmap)
                }
            }
        }

        /*binding.imgButtonGallery.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)){
                showRationaleDialog(" Permission Demo requires camera access",
                    "Camera cannot be used because Camera access is denied")
            } else {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                cameraAndLocationResultLauncher.launch(
                    arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION)
                )

            }
        }*/
    }
    val openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
                result->
            if (result.resultCode == RESULT_OK && result.data != null){
                binding.ivBackground.setImageURI(result.data?.data)
            }
        }


    val requestPermission:ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            permissions.entries.forEach{
                val permissionsName = it.key
                val isGranted = it.value

                if (isGranted){
                    Toast.makeText(this,"Permission granted now you can read the storage files.",Toast.LENGTH_LONG).show()
                    val pickIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }else{
                    if (permissionsName == android.Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(this,"Permission denied to read storage files",Toast.LENGTH_LONG).show()
                    }

                }
            }

        }

    private fun requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationaleDialog(
                "DoodleDrawingApp","DoodleDrawingApp " + "needs to access your External Storage")
        }else{
            requestPermission.launch(
                arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,)
            )
        }
    }


    private fun showRationaleDialog(
            title: String,
            message: String,
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK"){ dialog, _ ->
                    requestPermission.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            builder.create().show()
    }

    //This time we creat the Activity result launcher of type Array<String>
    private val cameraAndLocationResultLauncher:ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ){permissions->
            /**
            Here it returns a Map of permission name as key with boolean as value
            Todo 2: We loop through the map to get the value we need which is the boolean
            value
             */
            Log.d("MainActivity","Permissions $permissions")
            permissions.entries.forEach {
                val permissionName = it.key
                //Todo 3: if it is granted then we show its granted
                val isGranted = it.value
                if (isGranted) {
                    //check the permission name and perform the specific operation
                    if ( permissionName == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                        Toast.makeText(
                            this,
                            "Permission granted for location",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }else{
                        //check the permission name and perform the specific operation
                        Toast.makeText(
                            this,
                            "Permission granted for Camera",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                } else {
                    if ( permissionName == android.Manifest.permission.ACCESS_FINE_LOCATION) {
                        Toast.makeText(
                            this,
                            "Permission denied for location",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }else{
                        Toast.makeText(
                            this,
                            "Permission denied for Camera",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
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

    private fun isReadStorageAllowed():Boolean{
        val result = ContextCompat.checkSelfPermission(
            this,Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapView(view:View):Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,
        view.height,Bitmap.Config.ARGB_8888)

        val canvas = Canvas(returnedBitmap)

        val bgDrawable = view.background
        if (bgDrawable != null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result =""
        withContext(Dispatchers.IO){
            if (mBitmap != null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    //mBitmap?.compress(Bitmap.CompressFormat.JPEG,90,bytes)

                    val file = File(
                        externalCacheDir?.absoluteFile.toString()
                                + File.separator + "DoodleDrawingApp " + System.currentTimeMillis()/1000 + ".jpg")

                    val fileOut = FileOutputStream(file)
                    fileOut.write(bytes.toByteArray())
                    fileOut.close()

                    result = file.absolutePath

                    runOnUiThread {
                        cancelProgressDialog()
                        if (!result.isEmpty()){
                            Toast.makeText(
                                this@MainActivity,
                                "File save successfully :$result",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            Toast.makeText(
                                this@MainActivity,
                                "Somethings went wrong while saving the file.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
        }
        return result
    }

    private fun showProgressDialog() {
        customProgressDialog = Dialog(this@MainActivity)

        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)

        customProgressDialog?.show()
    }

    private fun cancelProgressDialog() {
        if (customProgressDialog != null){
            customProgressDialog?.dismiss()
            customProgressDialog = null
        }
    }

    companion object{
        private const val STORAGE_PERMISSION_CODE =1
        private const val GALLERY = 2
    }
}