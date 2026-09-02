package com.traceboard.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.traceboard.app.data.model.AppUsage
import com.traceboard.app.data.repository.StorageInfo
import com.traceboard.app.data.repository.UsageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class UsagePeriod(val label: String, val millis: Long) {
    TODAY("Hari Ini", 24L * 3600 * 1000),
    SEVEN_DAYS("7 Hari", 7L * 24 * 3600 * 1000),
    ALL_TIME("Semua", Long.MAX_VALUE)
}

data class CustomPeriod(val label: String, val millis: Long) {
    companion object {
        val options: List<CustomPeriod> =
            (1..12).map { n -> CustomPeriod("$n Bulan", n * 30L * 24 * 3600 * 1000) } +
                CustomPeriod("1 Tahun", 365L * 24 * 3600 * 1000)
    }
}

class UsageViewModel(
    app: Application,
    private val usageRepository: UsageRepository
) : AndroidViewModel(app) {

    private val _period = MutableStateFlow(UsagePeriod.TODAY)
    val period: StateFlow<UsagePeriod> = _period.asStateFlow()

    private val _custom = MutableStateFlow<CustomPeriod?>(null)
    val custom: StateFlow<CustomPeriod?> = _custom.asStateFlow()

    val activeLabel: StateFlow<String> =
        combine(_period, _custom) { p, c -> c?.label ?: p.label }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UsagePeriod.TODAY.label)

    private val _appUsage = MutableStateFlow<List<AppUsage>>(emptyList())
    val appUsage: StateFlow<List<AppUsage>> = _appUsage.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _lastUpdated = MutableStateFlow(0L)
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    private val _batteryLevel = MutableStateFlow(-1)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _storage = MutableStateFlow<StorageInfo?>(null)
    val storage: StateFlow<StorageInfo?> = _storage.asStateFlow()

    init {
        refresh()
    }

    fun selectQuickPeriod(newPeriod: UsagePeriod) {
        _period.value = newPeriod
        _custom.value = null
        refresh()
    }

    fun selectCustomPeriod(newCustom: CustomPeriod) {
        _custom.value = newCustom
        refresh()
    }

    fun openUsageSettings() {
        usageRepository.openUsageSettings()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPerm = usageRepository.hasUsagePermission()
            _hasPermission.value = hasPerm
            if (hasPerm) {
                val periodMillis = _custom.value?.millis ?: _period.value.millis
                _appUsage.value = usageRepository.getAppUsage(periodMillis)
            }
            _batteryLevel.value = usageRepository.getBatteryLevel()
            _storage.value = usageRepository.getStorageInfo()
            _lastUpdated.value = System.currentTimeMillis()
        }
    }
}