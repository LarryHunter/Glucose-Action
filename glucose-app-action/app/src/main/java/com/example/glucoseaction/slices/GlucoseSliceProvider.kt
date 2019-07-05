package com.example.glucoseaction.slices

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.ListBuilder.RowBuilder
import androidx.slice.builders.SliceAction
import com.example.glucoseaction.GlucoseActivity
import com.example.glucoseaction.R

class GlucoseSliceProvider : SliceProvider() {
    /**
     * Instantiate any required objects. Return true if the provider was successfully created,
     * false otherwise.
     */
    override fun onCreateSliceProvider(): Boolean {
        return true
    }

    /**
     * Converts URL to content URI (i.e. content://com.actions.slices...)
     */
    override fun onMapIntentToUri(intent: Intent?): Uri {
        // Note: implementing this is only required if you plan on catching URL requests.
        // This is an example solution.
        var uriBuilder: Uri.Builder = Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
        if (intent == null) return uriBuilder.build()
        val data = intent.data
        if (data != null && data.path != null) {
            val path = data.path!!.replace("/", "")
            uriBuilder = uriBuilder.path(path)
        }
        if (context != null) {
            uriBuilder = uriBuilder.authority(context!!.packageName)
        }
        return uriBuilder.build()
    }

    /**
     * Construct the Slice and bind data if available.
     */
    override fun onBindSlice(sliceUri: Uri): Slice? {
        val activityAction = createActivityAction()
        if (context == null) {
            return null
        }

        return when (sliceUri.path) {
            "/glucose" -> {
                // Path recognized. Customize the Slice using the androidx.slice.builders API.
                // Note: ANRs and strict mode is enforced here so don't do any heavy operations.
                // Only bind data that is currently available in memory.
                glucoseValue = randomEgv()
                val trendRate = randomTrend()
                glucoseAndTrend = context!!.getString(
                    R.string.glucose_reading_format_string,
                    glucoseValue, trendRate
                )

                ListBuilder(context!!, sliceUri, ListBuilder.INFINITY)
                    .addRow(
                        RowBuilder()
                            .setTitle(glucoseAndTrend)
                            .setPrimaryAction(activityAction)
                    )
                    .build()
            }
            else -> {
                // Error: Path not found.
                ListBuilder(context!!, sliceUri, ListBuilder.INFINITY)
                    .addRow(
                        RowBuilder()
                            .setTitle("URI not found.")
                            .setPrimaryAction(activityAction)
                    )
                    .build()
            }
        }
    }

    private fun randomEgv(): String {
        return when (val egv = (39..401).random()) {
            39 -> "LOW"
            401 -> "HIGH"
            else -> "$egv mg/dL"
        }
    }

    private fun randomTrend(): String {
        val randomValue = (0..7).random()

        trendArrow = when (randomValue) {
            0 -> "&#8650;"
            1 -> "&#8595;"
            2 -> "&#8600;"
            3 -> "&#8594;"
            4 -> "&#8599;"
            5 -> "&#8593;"
            6 -> "&#8648;"
            else -> ""
        }

        return when (randomValue) {
            0 -> "and falling rapidly"
            1 -> "and falling"
            2 -> "and slowly falling"
            3 -> "and steady"
            4 -> "and slowly rising"
            5 -> "and rising"
            6 -> "and rising rapidly"
            else -> ""
        }
    }

    private fun createActivityAction(): SliceAction {
        return SliceAction.create(
            PendingIntent.getActivity(context, 0, Intent(context, GlucoseActivity::class.java), 0),
            IconCompat.createWithResource(context!!, R.mipmap.ic_launcher),
            ListBuilder.ICON_IMAGE,
            "Open App"
        )
    }

    /**
     * Slice has been pinned to external process. Subscribe to data source if necessary.
     */
    override fun onSlicePinned(sliceUri: Uri?) {
        // When data is received, call context.contentResolver.notifyChange(sliceUri, null) to
        // trigger GlucoseSliceProvider#onBindSlice(Uri) again.
    }

    /**
     * Unsubscribe from data source if necessary.
     */
    override fun onSliceUnpinned(sliceUri: Uri?) {
        // Remove any observers if necessary to avoid memory leaks.
    }

    companion object {
        lateinit var trendArrow: String
        lateinit var glucoseValue: String
        var glucoseAndTrend: String = ""
    }
}
