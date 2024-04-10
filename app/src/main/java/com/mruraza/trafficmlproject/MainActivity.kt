package com.mruraza.trafficmlproject

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.mruraza.trafficmlproject.ml.MobilenetV110224Quant
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.ByteBuffer

lateinit var imageOfData : ImageView
lateinit var bitmap: Bitmap
//lateinit var array: Array<String>

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val arry = arrayOf("others","Pothole","others")
        getPermission()

        val str = mutableListOf<String>()
        var ct = 0
        val bufferedReader:BufferedReader = InputStreamReader(assets.open("labels.txt")).buffered()
        var line = bufferedReader.readLine()
        while (line != null) {
            str.add(line)
            line = bufferedReader.readLine()
            ct++
        }



        imageOfData = findViewById<ImageView>(R.id.pic_to_be_detected);
        val selectImage = findViewById<Button>(R.id.btn_to_select_image_form_gallery)
        val opencam = findViewById<Button>(R.id.btn_to_open_cam)
        val prdictt = findViewById<Button>(R.id.predict_btn)
        val resultss = findViewById<TextView>(R.id.results)

        selectImage.setOnClickListener {
           // Toast.makeText(this, "well its clicked", Toast.LENGTH_SHORT).show()
            intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            startActivityForResult(intent,10)
        }

        opencam.setOnClickListener{
            intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,12)
        }


        prdictt.setOnClickListener {
            val model = MobilenetV110224Quant.newInstance(this)

            // Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.UINT8)

            val inputt: Bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val imagee: TensorImage = TensorImage(DataType.UINT8)
            imagee.load(inputt)
            val byteBuffer: ByteBuffer = imagee.buffer
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs = model.process(inputFeature0)
            val outputFeature0 = outputs.outputFeature0AsTensorBuffer
            val maxValueIndex = getMax(outputFeature0.floatArray)
            val maxValue = str[maxValueIndex - 1]
            resultss.text = maxValue

            // Releases model resources if no longer used.
            model.close()
        }

    }

    fun getMax(arr: FloatArray): Int {
        var maxIndex = 0
        var maxValue = arr[0]

        for (i in 1 until arr.size) {
            if (arr[i] > maxValue) {
                maxValue = arr[i]
                maxIndex = i
            }
        }

        return maxIndex
    }






    private fun getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 11)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==11){
            if(grantResults.isNotEmpty()){
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    this.getPermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==10){
            if(data!=null){
                val uri:Uri? = data.data

               bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
                imageOfData.setImageBitmap(bitmap)
            }
        }
        else if(requestCode==12){
            bitmap = data?.extras?.get("data") as Bitmap
            imageOfData.setImageBitmap(bitmap)

        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}