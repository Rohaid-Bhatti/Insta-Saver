package com.hazel.instadownloader.app.callbacks

interface ToolbarVisibilityListener {

    fun onToolbarVisibilityChanged(isLock: Boolean)

    fun swipeLock(swappable: Boolean)
}