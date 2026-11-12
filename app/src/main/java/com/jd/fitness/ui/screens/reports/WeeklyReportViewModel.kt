package com.jd.fitness.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jd.fitness.data.local.entity.WeeklyReportEntity
import com.jd.fitness.data.repository.FitnessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class WeeklyReportUiState(
    val selectedReport: WeeklyReportEntity? = null,
    val previousReport: WeeklyReportEntity? = null,
    val allReports: List<WeeklyReportEntity> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isGenerating: Boolean = false,
    val shareText: String = "",
    val pdfExportPath: String? = null
)

class WeeklyReportViewModel(
    private val repository: FitnessRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeeklyReportUiState())
    val uiState: StateFlow<WeeklyReportUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadReports()
    }

    private fun loadReports() {
        viewModelScope.launch {
            // Auto generate latest week report if none exists
            val now = LocalDate.now()
            val lastSunday = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            val weekStart = lastSunday.minusDays(6).format(dateFormatter)
            val weekEnd = lastSunday.format(dateFormatter)

            repository.generateWeeklyReport(weekStart, weekEnd)

            repository.getAllWeeklyReports().collect { reports ->
                val sorted = reports.sortedByDescending { it.weekStartDate }
                val current = sorted.firstOrNull()
                val previous = if (sorted.size > 1) sorted[1] else null

                val suggestions = buildSuggestions(current)
                val share = buildShareText(current)

                _uiState.value = _uiState.value.copy(
                    allReports = sorted,
                    selectedReport = current,
                    previousReport = previous,
                    suggestions = suggestions,
                    shareText = share
                )
            }
        }
    }

    fun selectReport(report: WeeklyReportEntity) {
        val index = _uiState.value.allReports.indexOf(report)
        val prev = if (index >= 0 && index + 1 < _uiState.value.allReports.size) {
            _uiState.value.allReports[index + 1]
        } else null

        _uiState.value = _uiState.value.copy(
            selectedReport = report,
            previousReport = prev,
            suggestions = buildSuggestions(report),
            shareText = buildShareText(report)
        )
    }

    private fun buildSuggestions(report: WeeklyReportEntity?): List<String> {
        if (report == null) return emptyList()
        val list = mutableListOf<String>()

        if (report.averageDailySteps < 10000) {
            list.add("Increase daily movement: averaging ${report.averageDailySteps} steps. Target 10,000 steps next week.")
        } else {
            list.add("Solid movement consistency: ${report.averageDailySteps} average daily steps. Maintain this daily momentum.")
        }

        if (report.worstHabitTitle.isNotBlank()) {
            list.add("Reinforce \"${report.worstHabitTitle}\" by stacking it right after an already consistent habit.")
        }

        if (report.restDaysCount < 1) {
            list.add("Schedule at least 1 deliberate rest day to optimize recovery and prevent physical fatigue.")
        } else {
            list.add("Maintain balanced recovery: ${report.restDaysCount} rest days logged this week.")
        }

        return list.take(3)
    }

    private fun buildShareText(report: WeeklyReportEntity?): String {
        if (report == null) return ""
        return """
            📊 FITNESS 1.2 — WEEKLY INTELLIGENCE REPORT
            Period: ${report.weekStartDate} to ${report.weekEndDate}
            
            • Habit Consistency: ${report.overallHabitPercentage.toInt()}%
            • Top Habit: ${report.bestHabitTitle}
            • Daily Steps Avg: ${report.averageDailySteps}
            • Workouts Logged: ${report.totalWorkouts}
            • Rest Days: ${report.restDaysCount}
            • Fasting Logged: ${String.format(Locale.US, "%.1f", report.fastingHoursTotal)}h
            
            Discipline compounds daily.
        """.trimIndent()
    }

    fun shareReport(context: Context) {
        val text = _uiState.value.shareText
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Weekly Intelligence Report — FITNESS 1.2")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share Weekly Report"))
    }

    fun exportToPdf(context: Context): File? {
        val report = _uiState.value.selectedReport ?: return null
        return try {
            val doc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = doc.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = AndroidColor.BLACK
                textSize = 14f
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = AndroidColor.rgb(10, 20, 50)
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = AndroidColor.rgb(0, 110, 200)
                textSize = 12f
                isFakeBoldText = true
                isAntiAlias = true
            }

            var y = 60f
            canvas.drawText("FITNESS 1.2 — WEEKLY INTELLIGENCE REPORT", 50f, y, titlePaint)
            y += 24f
            canvas.drawText("Week: ${report.weekStartDate} to ${report.weekEndDate}  |  Generated for JD", 50f, y, headerPaint)
            y += 36f

            val lines = listOf(
                "Overall Habit Compliance: ${report.overallHabitPercentage.toInt()}%",
                "Best Performing Habit: ${report.bestHabitTitle}",
                "Habit Needing Attention: ${report.worstHabitTitle}",
                "Average Daily Steps: ${report.averageDailySteps} steps",
                "Total Workouts Logged: ${report.totalWorkouts}",
                "Weight Trend: ${if (report.weightTrendKg != null) "${report.weightTrendKg} kg" else "Stable / insufficient entries"}",
                "Skincare Routine Compliance: ${report.skincareRate.toInt()}%",
                "Total Fasting Recorded: ${String.format(Locale.US, "%.1f", report.fastingHoursTotal)} hours",
                "Rest & Recovery Days: ${report.restDaysCount} days",
                "Discipline Milestones Unlocked: ${report.achievementsUnlockedCount}"
            )

            lines.forEach { line ->
                canvas.drawText("•  $line", 60f, y, paint)
                y += 24f
            }

            y += 20f
            canvas.drawText("NEXT WEEK ACTIONABLE RECOMMENDATIONS:", 50f, y, headerPaint)
            y += 24f

            _uiState.value.suggestions.forEach { sug ->
                canvas.drawText("1.  $sug", 60f, y, paint)
                y += 24f
            }

            y += 30f
            paint.textSize = 10f
            paint.color = AndroidColor.GRAY
            canvas.drawText("Generated by FITNESS 1.2 — Adaptive Personal Companion. All data calculated from native records.", 50f, y, paint)

            doc.finishPage(page)

            val dir = File(context.getExternalFilesDir(null), "reports")
            dir.mkdirs()
            val file = File(dir, "FITNESS_Report_${report.weekStartDate}.pdf")
            FileOutputStream(file).use { out ->
                doc.writeTo(out)
            }
            doc.close()
            _uiState.value = _uiState.value.copy(pdfExportPath = file.absolutePath)
            file
        } catch (e: Exception) {
            null
        }
    }
}

