package com.sultlab.murmur.data.model

enum class ReportReason(val apiValue: String) {
    HARMFUL_OR_DANGEROUS("harmful_or_dangerous"),
    HARASSMENT_OR_BULLYING("harassment_or_bullying"),
    EXPLICIT_CONTENT("explicit_content"),
    SPAM_OR_BOT("spam_or_bot"),
}