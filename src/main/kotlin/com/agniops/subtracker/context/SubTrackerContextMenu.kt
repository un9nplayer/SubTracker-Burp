package com.agniops.subtracker.context

import burp.api.montoya.MontoyaApi
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import com.agniops.subtracker.ui.MainTabComponent
import com.agniops.subtracker.util.DomainUtils
import java.awt.Component
import javax.swing.JMenuItem

class SubTrackerContextMenu(
    private val api: MontoyaApi,
    private val mainTab: MainTabComponent
) : ContextMenuItemsProvider {

    override fun provideMenuItems(event: ContextMenuEvent): List<Component> {
        val items = mutableListOf<Component>()
        val requestResponses = event.selectedRequestResponses()

        if (requestResponses.isNotEmpty()) {
            val httpService = requestResponses[0].httpService()
            if (httpService != null) {
                val rawHost = httpService.host()
                val cleanHost = DomainUtils.sanitiseHost(rawHost)
                if (cleanHost.isNotEmpty()) {
                    val menuItem = JMenuItem("Send '$cleanHost' to SubTracker")
                    menuItem.addActionListener {
                        mainTab.setDomain(cleanHost)
                        api.logging().logToOutput("[SubTracker] Sent '$cleanHost' to SubTracker tab from context menu.")
                    }
                    items.add(menuItem)
                }
            }
        }

        return items
    }
}
