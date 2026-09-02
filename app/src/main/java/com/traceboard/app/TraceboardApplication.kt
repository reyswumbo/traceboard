package com.traceboard.app

import android.app.Application
import com.traceboard.app.data.repository.TraceboardDatabase

class TraceboardApplication : Application() {
    val database: TraceboardDatabase by lazy { TraceboardDatabase.getInstance(this) }
}
