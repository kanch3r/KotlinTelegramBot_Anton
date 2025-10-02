package org.example

object TelegramUpdatesParser {

    const val POSITION_UPDATE_ID_REGEX: String = "\"update_id\":(.+?),"
    const val USER_CHAT_ID_REGEX: String = "\"chat\":\\{\"id\":(.+?),\""
    const val MESSAGE_TEXT_REGEX: String = "\"text\":\"(.+?)\""
    const val CALLBACK_DATA_REGEX: String = "\"data\":\"(.+?)\""

    fun getUpdateId(updates: String): Int? {
        val positionUpdateIdRegex = POSITION_UPDATE_ID_REGEX.toRegex()
        val allUpdateIds = positionUpdateIdRegex.findAll(updates)
        val lastUpdateIdMatch = allUpdateIds.lastOrNull()
        val groupUpdateId = lastUpdateIdMatch?.groups
        val resultId = groupUpdateId?.get(1)?.value?.toInt()
        return resultId
    }

    fun getChatId(updates: String): Int {
        val userChatIdRegex = USER_CHAT_ID_REGEX.toRegex()
        val matchChatId = userChatIdRegex.find(updates)
        val groupsChatId = matchChatId?.groups
        val resultChatId = groupsChatId?.get(1)?.value?.toInt()
            ?: throw IllegalArgumentException("Chat ID not found in updates")
        return resultChatId
    }

    fun getUserChatMessage(updates: String): String {
        val messageTextRegex = MESSAGE_TEXT_REGEX.toRegex()
        val matchResultText = messageTextRegex.find(updates)
        val groupsText = matchResultText?.groups
        val resultText = groupsText?.get(1)?.value ?: "Message not found in updates"
        return resultText
    }

    fun getUserCallBackData(updates: String): String {
        val callBackDataRegex = CALLBACK_DATA_REGEX.toRegex()
        val matchCallBackData = callBackDataRegex.find(updates)
        val groupsCallBackData = matchCallBackData?.groups
        val resultCallBackData = groupsCallBackData?.get(1)?.value ?: "CallBack data not found in updates"
        return resultCallBackData
    }
}