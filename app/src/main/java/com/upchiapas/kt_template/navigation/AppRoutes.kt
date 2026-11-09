package com.upchiapas.kt_template.navigation

object AppRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val TASK_FORM = "task_form?taskId={taskId}"

    const val ARG_TASK_ID = "taskId"
    const val KEY_OPERATION_MESSAGE = "operation_message"

    fun taskDetail(taskId: Int): String = "task_detail/$taskId"
    fun taskCreate(): String = "task_form"
    fun taskEdit(taskId: Int): String = "task_form?taskId=$taskId"
}
