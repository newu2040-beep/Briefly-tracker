package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.ReportExporter
import com.example.data.util.SocialShareHelper
import com.example.ui.models.GoalItemUiState
import com.example.ui.models.ReportsUiState
import com.example.ui.theme.EmeraldMindfulness
import com.example.ui.theme.IndigoPrimary
import java.io.File

enum class ExportFormat {
    PDF, CSV, TXT, SOCIAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportAndShareBottomSheet(
    reportsState: ReportsUiState,
    goals: List<GoalItemUiState>,
    userName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedFormat by remember { mutableStateOf(ExportFormat.PDF) }
    var lastExportedFile by remember { mutableStateOf<File?>(null) }
    var lastExportedUri by remember { mutableStateOf<Uri?>(null) }
    var lastExportType by remember { mutableStateOf<String>("application/pdf") }
    var isCopied by remember { mutableStateOf(false) }

    val shareText = remember(reportsState, goals, userName) {
        SocialShareHelper.createShareMessage(reportsState, goals, userName)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("export_share_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Export & Share Report",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = "PDF, CSV, TXT & Social Media Platforms",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Format Selection Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FormatTabButton(
                        title = "PDF",
                        icon = Icons.Default.PictureAsPdf,
                        isSelected = selectedFormat == ExportFormat.PDF,
                        onClick = { selectedFormat = ExportFormat.PDF },
                        modifier = Modifier.weight(1f)
                    )
                    FormatTabButton(
                        title = "CSV",
                        icon = Icons.Default.TableChart,
                        isSelected = selectedFormat == ExportFormat.CSV,
                        onClick = { selectedFormat = ExportFormat.CSV },
                        modifier = Modifier.weight(1f)
                    )
                    FormatTabButton(
                        title = "TXT",
                        icon = Icons.Default.Description,
                        isSelected = selectedFormat == ExportFormat.TXT,
                        onClick = { selectedFormat = ExportFormat.TXT },
                        modifier = Modifier.weight(1f)
                    )
                    FormatTabButton(
                        title = "Social",
                        icon = Icons.Default.Send,
                        isSelected = selectedFormat == ExportFormat.SOCIAL,
                        onClick = { selectedFormat = ExportFormat.SOCIAL },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Format details card
            when (selectedFormat) {
                ExportFormat.PDF -> {
                    ExportDetailsCard(
                        title = "PDF Progress Report Document",
                        description = "Formatted visual report with executive summary, completion rate, active streaks, goals status table, and category breakdown.",
                        badgeText = "Clean Printable A4 Document",
                        badgeColor = IndigoPrimary,
                        onExport = {
                            val (file, uri) = ReportExporter.exportToPdf(context, reportsState, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "application/pdf"
                            Toast.makeText(context, "✅ PDF Report generated: ${file.name}", Toast.LENGTH_LONG).show()
                        },
                        onShare = {
                            val (file, uri) = ReportExporter.exportToPdf(context, reportsState, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "application/pdf"
                            SocialShareHelper.shareReport(
                                context = context,
                                platform = SocialShareHelper.SocialPlatform.GENERAL,
                                message = shareText,
                                fileUri = uri,
                                mimeType = "application/pdf"
                            )
                        }
                    )
                }
                ExportFormat.CSV -> {
                    ExportDetailsCard(
                        title = "CSV Spreadsheet Data",
                        description = "Comma-separated values formatted for Excel, Google Sheets, or Numbers. Includes goal targets, streaks, subtasks, and timestamps.",
                        badgeText = "Raw Data Export",
                        badgeColor = EmeraldMindfulness,
                        onExport = {
                            val (file, uri) = ReportExporter.exportToCsv(context, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "text/csv"
                            Toast.makeText(context, "✅ CSV Data exported: ${file.name}", Toast.LENGTH_LONG).show()
                        },
                        onShare = {
                            val (file, uri) = ReportExporter.exportToCsv(context, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "text/csv"
                            SocialShareHelper.shareReport(
                                context = context,
                                platform = SocialShareHelper.SocialPlatform.GENERAL,
                                message = shareText,
                                fileUri = uri,
                                mimeType = "text/csv"
                            )
                        }
                    )
                }
                ExportFormat.TXT -> {
                    ExportDetailsCard(
                        title = "Text Summary Report",
                        description = "Formatted plain text report with emoji bullets, streak milestones, goal checkoffs, and daily motivational notes.",
                        badgeText = "Universal Text Format",
                        badgeColor = Color(0xFFF59E0B),
                        onExport = {
                            val (file, uri) = ReportExporter.exportToTxt(context, reportsState, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "text/plain"
                            Toast.makeText(context, "✅ TXT Report saved: ${file.name}", Toast.LENGTH_LONG).show()
                        },
                        onShare = {
                            val (file, uri) = ReportExporter.exportToTxt(context, reportsState, goals, userName)
                            lastExportedFile = file
                            lastExportedUri = uri
                            lastExportType = "text/plain"
                            SocialShareHelper.shareReport(
                                context = context,
                                platform = SocialShareHelper.SocialPlatform.GENERAL,
                                message = shareText,
                                fileUri = uri,
                                mimeType = "text/plain"
                            )
                        }
                    )
                }
                ExportFormat.SOCIAL -> {
                    // Social Media Direct Share Cards
                    Text(
                        text = "Share to Social Media Platforms",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Social Platforms Grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.WHATSAPP,
                            bgColor = Color(0xFF25D366),
                            onShare = {
                                val (_, uri) = ReportExporter.exportToPdf(context, reportsState, goals, userName)
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.WHATSAPP, shareText, uri, "application/pdf")
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.TWITTER,
                            bgColor = Color(0xFF1DA1F2),
                            onShare = {
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.TWITTER, shareText)
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.TELEGRAM,
                            bgColor = Color(0xFF229ED9),
                            onShare = {
                                val (_, uri) = ReportExporter.exportToPdf(context, reportsState, goals, userName)
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.TELEGRAM, shareText, uri, "application/pdf")
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.LINKEDIN,
                            bgColor = Color(0xFF0A66C2),
                            onShare = {
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.LINKEDIN, shareText)
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.INSTAGRAM,
                            bgColor = Color(0xFFE4405F),
                            onShare = {
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.INSTAGRAM, shareText)
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.FACEBOOK,
                            bgColor = Color(0xFF1877F2),
                            onShare = {
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.FACEBOOK, shareText)
                            }
                        )
                        SocialPlatformRow(
                            platform = SocialShareHelper.SocialPlatform.GENERAL,
                            bgColor = IndigoPrimary,
                            onShare = {
                                val (_, uri) = ReportExporter.exportToPdf(context, reportsState, goals, userName)
                                SocialShareHelper.shareReport(context, SocialShareHelper.SocialPlatform.GENERAL, shareText, uri, "application/pdf")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Post Text Preview & Copy Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Shareable Text Preview",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isCopied) EmeraldMindfulness.copy(alpha = 0.15f) else IndigoPrimary.copy(alpha = 0.12f),
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Briefly Progress", shareText)
                                clipboard.setPrimaryClip(clip)
                                isCopied = true
                                Toast.makeText(context, "📋 Copied share text to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = if (isCopied) EmeraldMindfulness else IndigoPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isCopied) "Copied" else "Copy Text",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCopied) EmeraldMindfulness else IndigoPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = shareText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FormatTabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        label = "tabBg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("export_tab_${title.lowercase()}"),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        shadowElevation = if (isSelected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
private fun ExportDetailsCard(
    title: String,
    description: String,
    badgeText: String,
    badgeColor: Color,
    onExport: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons: Generate & Download | Share File
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onExport,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("download_export_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, IndigoPrimary.copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Save",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save File",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = IndigoPrimary
                            )
                        )
                    }
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("share_export_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Report",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialPlatformRow(
    platform: SocialShareHelper.SocialPlatform,
    bgColor: Color,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onShare)
            .testTag("share_to_${platform.displayName.lowercase()}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(bgColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = platform.displayName,
                        tint = bgColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "Share on ${platform.displayName}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = if (platform.packageName != null) "Send direct post / message" else "Open device share dialog",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = bgColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Share →",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = bgColor,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
