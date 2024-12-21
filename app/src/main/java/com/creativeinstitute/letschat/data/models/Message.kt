package com.creativeinstitute.letschat.data.models

interface Message{
    var senderId: String
    var receiverId: String
    var messageId: String

}

data class TextMessages(
    val text: String? = null,
    override var senderId: String = "",
    override var receiverId: String = "",
    override var messageId: String = ""
): Message

data class ImageMessages(
    var imageLink: String = "",
    override var senderId: String = "",
    override var receiverId: String = "",
    override var messageId: String = ""
): Message


