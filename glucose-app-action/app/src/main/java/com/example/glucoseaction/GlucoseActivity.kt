package com.example.glucoseaction

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.Html
import android.text.TextUtils
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.glucoseaction.slices.GlucoseSliceProvider
import kotlinx.android.synthetic.main.content_glucose.*
import kotlinx.android.synthetic.main.fit_glucose_activity.*
import java.util.*

class GlucoseActivity : AppCompatActivity() {

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fit_glucose_activity)
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorPrimary))

        if (TextUtils.isEmpty(GlucoseSliceProvider.glucoseAndTrend)) {
            GlucoseSliceProvider.glucoseAndTrend = "No current EGV data"
            glucoseTextView.apply {
                text = GlucoseSliceProvider.glucoseAndTrend
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16F)
            }
        } else {
            glucoseTextView.text = getString(
                R.string.glucose_format_string,
                GlucoseSliceProvider.glucoseValue,
                Html.fromHtml(GlucoseSliceProvider.trendArrow))
        }

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.language = Locale.getDefault()
            }
        })

        fab.setOnClickListener {
            textToSpeech.speak(GlucoseSliceProvider.glucoseAndTrend, TextToSpeech.QUEUE_FLUSH, null)
        }
    }

    override fun onPause() {
        super.onPause()
        textToSpeech.stop()
    }

    override fun onStop() {
        super.onStop()
        textToSpeech.shutdown()
    }
}
