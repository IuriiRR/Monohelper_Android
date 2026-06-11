package com.monohelper.domain.usecase

import com.monohelper.core.result.AppResult
import com.monohelper.data.repo.ReportRepository
import com.monohelper.domain.model.MonthlyReport
import javax.inject.Inject

class GetMonthlyReport @Inject constructor(
    private val repository: ReportRepository,
) {
    fun cached(month: String): MonthlyReport? = repository.reports.value[month]

    suspend operator fun invoke(month: String, userId: String? = null): AppResult<MonthlyReport> =
        repository.refresh(month, userId)
}
