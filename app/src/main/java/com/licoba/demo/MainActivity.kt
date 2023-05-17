package com.licoba.demo

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.lgtv.GradientTextView

/**
 * Time:2023/05/17
 * Author:licoba
 * Description:展示的Activity
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ltv1 = findViewById<GradientTextView>(R.id.ltv_single)
        val ltv2 = findViewById<GradientTextView>(R.id.ltv_multiple)
        val tvProgress = findViewById<TextView>(R.id.tv_progress)
        tvProgress.text =  "${getText(R.string.progress)}${String.format("%.2f", 0f)}"

        val vSeekBar = findViewById<SeekBar>(R.id.v_seek_bar)

        vSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress/100f
                ltv1.setCurrentProgress(value)
                ltv2.setCurrentProgress(value)
                tvProgress.text =  "${getText(R.string.progress)}${String.format("%.2f", value)}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }
}
