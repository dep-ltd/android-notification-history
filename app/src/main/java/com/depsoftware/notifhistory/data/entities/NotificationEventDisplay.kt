package com.depsoftware.notifhistory.data.entities

fun NotificationEvent.hasDisplayableContent(): Boolean {
    return !title.isNullOrBlank() || !text.isNullOrBlank()
}
