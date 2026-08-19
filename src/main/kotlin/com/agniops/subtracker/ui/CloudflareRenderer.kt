package com.agniops.subtracker.ui

import java.awt.Color
import java.awt.Component
import javax.swing.JTable
import javax.swing.SwingConstants
import javax.swing.table.DefaultTableCellRenderer

class CloudflareRenderer : DefaultTableCellRenderer() {

    init {
        horizontalAlignment = SwingConstants.CENTER
        isOpaque = true
    }

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        val valStr = value?.toString() ?: ""
        val bgDark = Color(24, 25, 32)

        if (valStr.contains("Yes")) {
            foreground = Color(251, 191, 36) // Bright amber text
            background = Color(58, 35, 15)   // Dark amber badge container
        } else if (valStr.contains("No")) {
            foreground = Color(148, 163, 184) // Slate gray
            background = bgDark
        } else {
            foreground = Color(241, 245, 249)
            background = bgDark
        }

        if (isSelected && table != null) {
            background = table.selectionBackground
            foreground = table.selectionForeground
        }

        return this
    }
}
