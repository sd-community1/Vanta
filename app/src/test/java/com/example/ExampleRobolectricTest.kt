package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.StorageAccessHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("VANTA", appName)
  }

  @Test
  fun `formatDuration formats milliseconds correctly`() {
    val durationFormatted = StorageAccessHelper.formatDuration(65000L)
    assertEquals("01:05", durationFormatted)

    val readableFormatted = StorageAccessHelper.formatDurationReadable(3720000L)
    assertEquals("1h 2m", readableFormatted)
  }
}
