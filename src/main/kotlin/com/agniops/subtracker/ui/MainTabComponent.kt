package com.agniops.subtracker.ui

import burp.api.montoya.MontoyaApi
import com.agniops.subtracker.api.AgniOpsClient
import com.agniops.subtracker.api.ScanResult
import com.agniops.subtracker.util.DomainUtils
import com.google.gson.GsonBuilder
import java.awt.*
import java.io.File
import java.io.FileWriter
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.*
import javax.swing.table.DefaultTableModel

class MainTabComponent(private val api: MontoyaApi) {

    val uiComponent: JComponent = JPanel(BorderLayout(0, 0))
    private val client = AgniOpsClient(api)
    private val isScanning = AtomicBoolean(false)
    private var lastResult: ScanResult? = null
    private var currentFullHost: String = ""

    private lateinit var txtApiKey: JPasswordField
    private lateinit var txtDomain: JTextField
    private lateinit var chkRoot: JCheckBox
    private lateinit var chkShowKey: JCheckBox
    private lateinit var btnScan: JButton
    private lateinit var lblStatus: JLabel
    private lateinit var lblQuota: JLabel
    private lateinit var tableModel: DefaultTableModel
    private lateinit var table: JTable

    companion object {
        private const val PREF_API_KEY = "AgniOps_API_Key"
        private const val PREF_ROOT_MODE = "AgniOps_Root_Domain_Mode"
        private const val MAX_TABLE_ROWS = 5000
    }

    init {
        buildUI()
    }

    fun setDomain(host: String) {
        val cleanHost = DomainUtils.sanitiseHost(host)
        currentFullHost = cleanHost
        updateDomainInputField()
    }

    private fun updateDomainInputField() {
        if (currentFullHost.isEmpty()) return
        val isRoot = chkRoot.isSelected
        val domain = if (isRoot) DomainUtils.extractRootDomain(currentFullHost) else currentFullHost
        txtDomain.text = domain
    }

    private fun buildUI() {
        val bgMain    = Color(35, 37, 46)
        val bgPanel   = Color(43, 45, 57)
        val bgInput   = Color(24, 25, 32)
        val borderCol = Color(60, 64, 80)
        val textMain  = Color(241, 245, 249)
        val textMuted = Color(148, 163, 184)
        val accentInd = Color(99, 102, 241)

        uiComponent.background = bgMain

        // 1. Header Panel
        val headerPanel = JPanel(BorderLayout()).apply {
            background = Color(24, 24, 37)
            border = BorderFactory.createEmptyBorder(12, 18, 12, 18)
        }

        val titleLabel = JLabel("AgniOps SubTracker").apply {
            font = Font("SansSerif", Font.BOLD, 16)
            foreground = Color.WHITE
        }

        val subLabel = JLabel("Powered by AgniOps Intelligence Node  v1.0.0 (Kotlin / Montoya API)").apply {
            font = Font("SansSerif", Font.PLAIN, 11)
            foreground = Color(165, 180, 252)
        }

        val headerTextPanel = JPanel(GridLayout(2, 1)).apply {
            isOpaque = false
            add(titleLabel)
            add(subLabel)
        }
        headerPanel.add(headerTextPanel, BorderLayout.WEST)

        // 2. Controls Panel
        val ctrlContainer = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            background = bgPanel
            border = BorderFactory.createMatteBorder(0, 0, 1, 0, borderCol)
        }

        // API Key Row
        val keyPanel = JPanel(FlowLayout(FlowLayout.LEFT, 12, 10)).apply { isOpaque = false }
        val lblKey = JLabel("AgniOps API Key:").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            foreground = textMain
        }

        txtApiKey = JPasswordField(26).apply {
            font = Font("Monospaced", Font.PLAIN, 12)
            background = bgInput
            foreground = textMain
            caretColor = textMain
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderCol, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            )
        }

        val savedKey = api.persistence().preferences().getString(PREF_API_KEY)
        if (!savedKey.isNullOrEmpty()) {
            txtApiKey.text = savedKey
        }

        val btnSaveKey = JButton("Save Key").apply {
            font = Font("SansSerif", Font.BOLD, 11)
            background = accentInd
            foreground = Color.WHITE
            isFocusPainted = false
            addActionListener {
                val key = String(txtApiKey.password).trim()
                if (key.isNotEmpty()) {
                    api.persistence().preferences().setString(PREF_API_KEY, key)
                    lblStatus.text = "API Key saved in Burp extension preferences."
                    lblStatus.foreground = Color(52, 211, 153)
                } else {
                    api.persistence().preferences().deleteString(PREF_API_KEY)
                    lblStatus.text = "API Key cleared."
                    lblStatus.foreground = Color(248, 113, 113)
                }
            }
        }

        chkShowKey = JCheckBox("Show").apply {
            font = Font("SansSerif", Font.PLAIN, 11)
            foreground = textMuted
            isOpaque = false
            addActionListener {
                txtApiKey.echoChar = if (isSelected) '\u0000' else '*'
            }
        }

        keyPanel.add(lblKey)
        keyPanel.add(txtApiKey)
        keyPanel.add(chkShowKey)
        keyPanel.add(btnSaveKey)

        // Scan Input Row
        val scanPanel = JPanel(FlowLayout(FlowLayout.LEFT, 12, 10)).apply { isOpaque = false }
        val lblDomain = JLabel("Target Domain:").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            foreground = textMain
        }

        txtDomain = JTextField(22).apply {
            font = Font("Monospaced", Font.BOLD, 13)
            background = bgInput
            foreground = Color(129, 140, 248)
            caretColor = textMain
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderCol, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            )
        }

        chkRoot = JCheckBox("Root domain mode (extract example.com from open.example.com)").apply {
            font = Font("SansSerif", Font.PLAIN, 11)
            foreground = textMuted
            isOpaque = false
            val savedRoot = api.persistence().preferences().getString(PREF_ROOT_MODE)
            isSelected = savedRoot != "false"
            addActionListener {
                api.persistence().preferences().setString(PREF_ROOT_MODE, if (isSelected) "true" else "false")
                updateDomainInputField()
            }
        }

        btnScan = JButton("Scan Subdomains").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            background = accentInd
            foreground = Color.WHITE
            isFocusPainted = false
            addActionListener { startScan() }
        }

        scanPanel.add(lblDomain)
        scanPanel.add(txtDomain)
        scanPanel.add(btnScan)

        val rootPanel = JPanel(FlowLayout(FlowLayout.LEFT, 12, 2)).apply {
            isOpaque = false
            add(chkRoot)
        }

        ctrlContainer.add(keyPanel)
        ctrlContainer.add(scanPanel)
        ctrlContainer.add(rootPanel)

        // Status Bar
        val statusPanel = JPanel(FlowLayout(FlowLayout.LEFT, 18, 8)).apply {
            background = Color(28, 30, 38)
            border = BorderFactory.createMatteBorder(1, 0, 1, 0, borderCol)
        }
        lblStatus = JLabel("Enter a domain and click Scan Subdomains.").apply {
            font = Font("SansSerif", Font.BOLD, 12)
            foreground = textMuted
        }
        statusPanel.add(lblStatus)

        val topWrapper = JPanel(BorderLayout()).apply {
            add(headerPanel, BorderLayout.NORTH)
            add(ctrlContainer, BorderLayout.CENTER)
            add(statusPanel, BorderLayout.SOUTH)
        }

        // 3. Results JTable
        tableModel = DefaultTableModel(arrayOf("#", "Subdomain", "IP Address", "Cloudflare"), 0)
        table = JTable(tableModel).apply {
            font = Font("Monospaced", Font.PLAIN, 12)
            rowHeight = 26
            background = bgInput
            foreground = textMain
            gridColor = borderCol
            tableHeader.apply {
                font = Font("SansSerif", Font.BOLD, 12)
                background = Color(28, 30, 38)
                foreground = textMain
            }
            columnModel.getColumn(0).apply { preferredWidth = 45; maxWidth = 60 }
            columnModel.getColumn(1).preferredWidth = 340
            columnModel.getColumn(2).preferredWidth = 160
            columnModel.getColumn(3).apply {
                preferredWidth = 100
                cellRenderer = CloudflareRenderer()
            }
        }

        val tableScroll = JScrollPane(table).apply {
            background = bgInput
            viewport.background = bgInput
            border = BorderFactory.createEmptyBorder()
        }

        // 4. Footer & Export Bar
        val footerPanel = JPanel(BorderLayout()).apply {
            background = Color(28, 30, 38)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, borderCol),
                BorderFactory.createEmptyBorder(8, 18, 8, 18)
            )
        }

        lblQuota = JLabel("Quota: - / 1000").apply {
            font = Font("SansSerif", Font.BOLD, 11)
            foreground = textMuted
        }

        val exportPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 6, 0)).apply { isOpaque = false }
        val lblExp = JLabel("Export: ").apply {
            foreground = textMuted
            font = Font("SansSerif", Font.PLAIN, 11)
        }

        val btnExpJson = JButton("JSON")
        val btnExpCsv  = JButton("CSV")
        val btnExpTxt  = JButton("TXT")

        listOf(btnExpJson, btnExpCsv, btnExpTxt).forEach { b ->
            b.font = Font("SansSerif", Font.BOLD, 11)
            b.background = bgPanel
            b.foreground = textMain
            b.isFocusPainted = false
        }

        btnExpJson.addActionListener { exportResults("json") }
        btnExpCsv.addActionListener { exportResults("csv") }
        btnExpTxt.addActionListener { exportResults("txt") }

        exportPanel.add(lblExp)
        exportPanel.add(btnExpJson)
        exportPanel.add(btnExpCsv)
        exportPanel.add(btnExpTxt)

        footerPanel.add(lblQuota, BorderLayout.WEST)
        footerPanel.add(exportPanel, BorderLayout.EAST)

        uiComponent.add(topWrapper, BorderLayout.NORTH)
        uiComponent.add(tableScroll, BorderLayout.CENTER)
        uiComponent.add(footerPanel, BorderLayout.SOUTH)
    }

    private fun startScan() {
        if (isScanning.get()) return

        val domain = txtDomain.text.trim().lowercase()
        val sanitisedDomain = DomainUtils.sanitiseHost(domain)

        val (valid, err) = DomainUtils.validateDomain(sanitisedDomain)
        if (!valid) {
            lblStatus.text = "Error: $err"
            lblStatus.foreground = Color(239, 68, 68)
            return
        }

        val apiKey = String(txtApiKey.password).trim()
        if (apiKey.isEmpty()) {
            lblStatus.text = "Error: AgniOps API Key is required. Please set it above and click Save Key."
            lblStatus.foreground = Color(239, 68, 68)
            return
        }

        isScanning.set(true)
        btnScan.isEnabled = false
        lblStatus.text = "Scanning subdomains for $sanitisedDomain..."
        lblStatus.foreground = Color(79, 70, 229)
        tableModel.rowCount = 0

        Thread {
            try {
                val result = client.scanSubdomains(sanitisedDomain, apiKey)
                SwingUtilities.invokeLater { onScanSuccess(result) }
            } catch (e: Exception) {
                SwingUtilities.invokeLater { onScanError(e.message ?: "Scan failed.") }
            }
        }.start()
    }

    private fun onScanSuccess(result: ScanResult) {
        isScanning.set(false)
        btnScan.isEnabled = true
        lastResult = result

        val subdomains = result.subdomains ?: emptyList()
        tableModel.rowCount = 0

        val displayList = if (subdomains.size > MAX_TABLE_ROWS) subdomains.take(MAX_TABLE_ROWS) else subdomains
        displayList.forEachIndexed { index, entry ->
            val sub = entry.subdomain?.take(255) ?: ""
            val ip = entry.ip?.take(64) ?: ""
            val cf = if (entry.cloudflare) "Yes" else "No"
            if (sub.isNotEmpty()) {
                tableModel.addRow(arrayOf(index + 1, sub, if (ip.isNotEmpty()) ip else "-", cf))
            }
        }

        if (subdomains.size > MAX_TABLE_ROWS) {
            api.logging().logToOutput("[SubTracker] Warning: Result truncated to $MAX_TABLE_ROWS rows for UI performance.")
        }

        val qRem = result.meta?.quotaRemaining ?: "-"
        val qTot = result.meta?.dailyQuota ?: "1000"
        lblQuota.text = "Quota remaining: $qRem / $qTot"

        val count = if (result.count > 0) result.count else subdomains.size
        lblStatus.text = "Scan complete! Found $count subdomain(s) for ${result.domain ?: ""}"
        lblStatus.foreground = Color(16, 185, 129)
    }

    private fun onScanError(errorMsg: String) {
        isScanning.set(false)
        btnScan.isEnabled = true
        lblStatus.text = errorMsg
        lblStatus.foreground = Color(239, 68, 68)
    }

    private fun exportResults(fmt: String) {
        val result = lastResult
        val subdomains = result?.subdomains
        if (result == null || subdomains.isNullOrEmpty()) {
            lblStatus.text = "No results to export. Run a scan first."
            lblStatus.foreground = Color(239, 68, 68)
            return
        }

        val chooser = JFileChooser()
        val domain = result.domain ?: "subdomains"
        chooser.selectedFile = File("${domain}_subdomains.$fmt")

        val parentWindow = SwingUtilities.getWindowAncestor(uiComponent)
        val ret = chooser.showSaveDialog(parentWindow)
        if (ret == JFileChooser.APPROVE_OPTION) {
            val targetFile = chooser.selectedFile
            try {
                FileWriter(targetFile).use { writer ->
                    when (fmt) {
                        "json" -> {
                            val gson = GsonBuilder().setPrettyPrinting().create()
                            writer.write(gson.toJson(result))
                        }
                        "csv" -> {
                            writer.write("\"Subdomain\",\"IP Address\",\"Cloudflare\"\n")
                            subdomains.forEach { s ->
                                val cf = if (s.cloudflare) "Yes" else "No"
                                writer.write("\"${s.subdomain ?: ""}\",\"${s.ip ?: ""}\",\"$cf\"\n")
                            }
                        }
                        else -> { // txt
                            subdomains.forEach { s ->
                                if (!s.subdomain.isNullOrEmpty()) {
                                    writer.write("${s.subdomain}\n")
                                }
                            }
                        }
                    }
                }
                lblStatus.text = "Exported successfully to ${targetFile.absolutePath}"
                lblStatus.foreground = Color(16, 185, 129)
            } catch (e: Exception) {
                api.logging().logToError("[SubTracker] Export failed: ${e.message}")
                lblStatus.text = "Failed to export results."
                lblStatus.foreground = Color(239, 68, 68)
            }
        }
    }
}
