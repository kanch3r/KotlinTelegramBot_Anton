package org.example

class TelegramUpdatesParser {

    companion object {
        const val POSITION_UPDATE_ID_REGEX: String = "\"update_id\":(.+?),"
        const val USER_CHAT_ID_REGEX: String = "\"chat\":\\{\"id\":(.+?),\""
        const val MESSAGE_TEXT_REGEX: String = "\"text\":\"(.+?)\""

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
    }
}