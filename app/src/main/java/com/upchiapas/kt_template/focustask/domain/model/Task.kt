package com.upchiapas.kt_template.focustask.domain.model

data class Task(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String,
    val estimatedTime: Double,
    val priority: String,
    val dueDate: String,
    val isCompleted: Boolean = false,
    val createdAt: String = ""
) {
    val points: Int
        get() {
            val basePoints = (estimatedTime * 10).toInt()
            val priorityMultiplier = when (priority.uppercase()) {
                "ALTA" -> 3
                "MEDIA" -> 2
                else -> 1
            }
            val categoryBonus = when (category.uppercase()) {
                "TRABAJO" -> 20
                "ESTUDIO" -> 10
                else -> 5
            }
            return (basePoints * priorityMultiplier) + categoryBonus
        }

    companion object {
        val CATEGORIES = listOf("Estudio", "Trabajo", "Personal")
        val PRIORITIES = listOf("Alta", "Media", "Baja")
    }
}
