package com.agniops.subtracker

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import com.agniops.subtracker.context.SubTrackerContextMenu
import com.agniops.subtracker.ui.MainTabComponent

class SubTrackerExtension : BurpExtension {

    override fun initialize(api: MontoyaApi) {
        api.extension().setName("AgniOps SubTracker")

        val mainTab = MainTabComponent(api)

        // Register UI Tab
        api.userInterface().registerSuiteTab("SubTracker", mainTab.uiComponent)

        // Register Context Menu Provider
        api.userInterface().registerContextMenuItemsProvider(SubTrackerContextMenu(api, mainTab))

        api.logging().logToOutput("[+] AgniOps SubTracker (Kotlin / Montoya API v1.0.0) loaded successfully.")
    }
}
