package com.monohelper.data.repo

import com.monohelper.core.result.AppResult
import com.monohelper.data.api.ApiService
import com.monohelper.domain.model.MonthlyReport
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface ReportRepository {
    /** Cache of fetched reports keyed by `YYYY-MM` month. */
    val reports: StateFlow<Map<String, MonthlyReport>>

    suspend fun refresh(month: String, userId: String? = null): AppResult<MonthlyReport>
}

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val api: ApiService,
) : ReportRepository {

    private val _reports = MutableStateFlow<Map<String, MonthlyReport>>(emptyMap())
    override val reports: StateFlow<Map<String, MonthlyReport>> = _reports.asStateFlow()

    override suspend fun refresh(month: String, userId: String?): AppResult<MonthlyReport> {
        val result = safeCall { api.monthlyReport(month, userId).toDomain() }
        if (result is AppResult.Success) {
            _reports.value = _reports.value + (month to result.value)
        }
        return result
    }
}
