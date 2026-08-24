package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.PanApplicationEntity
import com.example.data.model.RetailerEntity
import com.example.ui.components.ImagePreviewDialog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected
import com.example.ui.viewmodel.PanViewModel
import com.example.util.PdfExportHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: PanViewModel,
    modifier: Modifier = Modifier
) {
    var currentAdminTab by remember { mutableIntStateOf(0) } // 0: Submissions / Folders, 1: Retailers
    val allApps by viewModel.allApplications.collectAsStateWithLifecycle()
    val filteredApps by viewModel.filteredApplications.collectAsStateWithLifecycle()
    val retailers by viewModel.allRetailers.collectAsStateWithLifecycle()
    val selectedApp by viewModel.selectedApplication.collectAsStateWithLifecycle()

    var showAddRetailerDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    // Counts
    val pendingCount = remember(allApps) { allApps.count { it.status == "PENDING" || it.status == "IN_REVIEW" } }
    val doneCount = remember(allApps) { allApps.count { it.status == "COMPLETED" || it.status == "ACK_GENERATED" } }
    val totalCount = allApps.size

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SaffronOrange, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Control Panel",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "PAN Management & Retailer Desk",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showChangePinDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Change PIN",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentAdminTab == 0,
                    onClick = {
                        currentAdminTab = 0
                        viewModel.selectApplication(null)
                    },
                    icon = {
                        Icon(Icons.Default.Folder, contentDescription = "Submissions")
                    },
                    label = {
                        Text("Folders & Submissions", fontWeight = if (currentAdminTab == 0) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFE0EBF7)
                    )
                )

                NavigationBarItem(
                    selected = currentAdminTab == 1,
                    onClick = {
                        currentAdminTab = 1
                        viewModel.selectApplication(null)
                    },
                    icon = {
                        Icon(Icons.Default.Group, contentDescription = "Retailers")
                    },
                    label = {
                        Text("Retailers (${retailers.size})", fontWeight = if (currentAdminTab == 1) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFE0EBF7)
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentAdminTab == 1) {
                FloatingActionButton(
                    onClick = { showAddRetailerDialog = true },
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Retailer")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ New Retailer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedApp != null) {
                // Customer Dossier Full Details View
                AdminCustomerDossierView(
                    app = selectedApp!!,
                    viewModel = viewModel,
                    onBack = { viewModel.selectApplication(null) }
                )
            } else if (currentAdminTab == 0) {
                // Submissions Folder View
                AdminSubmissionsView(
                    allApps = allApps,
                    filteredApps = filteredApps,
                    pendingCount = pendingCount,
                    doneCount = doneCount,
                    totalCount = totalCount,
                    retailersCount = retailers.size,
                    viewModel = viewModel,
                    onSelectApp = { viewModel.selectApplication(it) }
                )
            } else {
                // Retailer Management View
                AdminRetailersView(
                    retailers = retailers,
                    allApps = allApps,
                    viewModel = viewModel,
                    onAddNewRetailer = { showAddRetailerDialog = true }
                )
            }

            if (showAddRetailerDialog) {
                AddRetailerDialog(
                    onDismiss = { showAddRetailerDialog = false },
                    onSave = { name, shop, mobile, pin, address ->
                        viewModel.createRetailer(
                            name = name,
                            shopName = shop,
                            mobile = mobile,
                            pin = pin,
                            address = address,
                            onSuccess = { showAddRetailerDialog = false },
                            onError = { /* handled inside */ }
                        )
                    }
                )
            }

            if (showChangePinDialog) {
                ChangeAdminPinDialog(
                    currentPin = viewModel.adminPin.collectAsStateWithLifecycle().value,
                    onDismiss = { showChangePinDialog = false },
                    onPinChanged = { newPin ->
                        viewModel.updateAdminPin(newPin)
                        showChangePinDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun AdminSubmissionsView(
    allApps: List<PanApplicationEntity>,
    filteredApps: List<PanApplicationEntity>,
    pendingCount: Int,
    doneCount: Int,
    totalCount: Int,
    retailersCount: Int,
    viewModel: PanViewModel,
    onSelectApp: (PanApplicationEntity) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Metrics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Submissions",
                    hindi = "कुल आवेदन",
                    value = totalCount.toString(),
                    color = NavyPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pending Review",
                    hindi = "लंबित आवेदन",
                    value = pendingCount.toString(),
                    color = SaffronOrange,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Completed",
                    hindi = "पूर्ण आवेदन",
                    value = doneCount.toString(),
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search by Customer, Mobile, Ref ID, Retailer...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )
        }

        // Status Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = currentFilter == "ALL",
                    onClick = { viewModel.setStatusFilter("ALL") },
                    label = { Text("All (${allApps.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == "PENDING",
                    onClick = { viewModel.setStatusFilter("PENDING") },
                    label = { Text("Pending (${allApps.count { it.status == "PENDING" }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SaffronOrange,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == "DONE",
                    onClick = { viewModel.setStatusFilter("DONE") },
                    label = { Text("Done (${doneCount})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = currentFilter == "REJECTED",
                    onClick = { viewModel.setStatusFilter("REJECTED") },
                    label = { Text("Rejected (${allApps.count { it.status == "REJECTED" }})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusRejected,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Section Title
        item {
            Text(
                text = "CUSTOMER FOLDERS & DOCKETS / ग्राहक फ़ोल्डर सूची (${filteredApps.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                letterSpacing = 0.5.sp
            )
        }

        if (filteredApps.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Submissions Found",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Retailer submissions with date and time will automatically appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredApps, key = { it.id }) { app ->
                AdminFolderDocketCard(
                    app = app,
                    onClick = { onSelectApp(app) }
                )
            }
        }
    }
}

@Composable
fun AdminFolderDocketCard(
    app: PanApplicationEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(app.createdAt))
    val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(app.createdAt))

    val statusColor = when (app.status) {
        "COMPLETED" -> StatusApproved
        "REJECTED" -> StatusRejected
        "ACK_GENERATED" -> Color(0xFF0288D1)
        else -> StatusPending
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE2E8F0))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Folder Icon + Customer & Ref + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NavyPrimary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = app.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Ref: ${app.applicationRef}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Details Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Father: ${app.fatherName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF334155),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Retailer: ${app.retailerName} (${app.retailerShopName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Quick Status Highlights
            if (app.ackNumber.isNotEmpty() || app.panNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (app.ackNumber.isNotEmpty()) {
                        Text(
                            text = "Ack: ${app.ackNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                    }
                    if (app.panNumber.isNotEmpty()) {
                        Text(
                            text = "PAN: ${app.panNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminCustomerDossierView(
    app: PanApplicationEntity,
    viewModel: PanViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var previewImagePath by remember { mutableStateOf<String?>(null) }
    var previewImageTitle by remember { mutableStateOf("") }

    var isExportingPdf by remember { mutableStateOf(false) }

    // Status editing state
    var selectedStatus by remember { mutableStateOf(app.status) }
    var ackNumberInput by remember { mutableStateOf(app.ackNumber) }
    var panNumberInput by remember { mutableStateOf(app.panNumber) }
    var remarksInput by remember { mutableStateOf(app.adminRemarks) }
    var showSaveSuccess by remember { mutableStateOf(false) }

    if (previewImagePath != null) {
        ImagePreviewDialog(
            filePath = previewImagePath!!,
            title = previewImageTitle,
            onDismiss = { previewImagePath = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NavyPrimary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${app.customerName}'s Dossier",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(app.createdAt))
                Text(
                    text = "Ref: ${app.applicationRef} • Submitted: $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64748B)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Bar (Download / Share PDF, Call, WhatsApp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFBFDBFE))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Text(
                    text = "DOSSIER EXPORT & ACTIONS / डाउनलोड एवं शेयर",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download & Share PDF Button
                    Button(
                        onClick = {
                            isExportingPdf = true
                            PdfExportHelper.generateAndShareDossier(context, app) { pdfFile ->
                                isExportingPdf = false
                                if (pdfFile != null) {
                                    PdfExportHelper.sharePdfFile(
                                        context,
                                        pdfFile,
                                        "PAN_Dossier_${app.customerName}"
                                    )
                                } else {
                                    Toast.makeText(context, "Could not generate PDF", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        enabled = !isExportingPdf,
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        if (isExportingPdf) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generating...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download PDF Dossier", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Call Customer
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${app.mobile}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open dialer", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(0.7f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp)
                    }

                    // WhatsApp Customer
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/91${app.mobile}"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot open WhatsApp", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: All Captured Document Photos Grid
        Text(
            text = "CAPTURED DOCUMENTS / संलग्न दस्तावेज़ फोटो",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Text(
            text = "Click any photo to zoom in full screen / ज़ूम करने के लिए फोटो पर क्लिक करें",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Document Photos Flow Grid
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminPhotoThumbnailCard(
                label = "Aadhaar Card (Front)",
                filePath = app.aadharFrontPath,
                onClick = {
                    previewImagePath = app.aadharFrontPath
                    previewImageTitle = "Aadhaar Card Front"
                },
                modifier = Modifier.fillMaxWidth(0.48f)
            )

            AdminPhotoThumbnailCard(
                label = "Aadhaar Card (Back)",
                filePath = app.aadharBackPath,
                onClick = {
                    previewImagePath = app.aadharBackPath
                    previewImageTitle = "Aadhaar Card Back"
                },
                modifier = Modifier.fillMaxWidth(0.48f)
            )

            if (app.voterFrontPath.isNotEmpty()) {
                AdminPhotoThumbnailCard(
                    label = "Voter ID (Front)",
                    filePath = app.voterFrontPath,
                    onClick = {
                        previewImagePath = app.voterFrontPath
                        previewImageTitle = "Voter ID Front"
                    },
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }

            if (app.voterBackPath.isNotEmpty()) {
                AdminPhotoThumbnailCard(
                    label = "Voter ID (Back)",
                    filePath = app.voterBackPath,
                    onClick = {
                        previewImagePath = app.voterBackPath
                        previewImageTitle = "Voter ID Back"
                    },
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }

            if (app.panPhotoPath.isNotEmpty()) {
                AdminPhotoThumbnailCard(
                    label = "Old PAN Card Photo",
                    filePath = app.panPhotoPath,
                    onClick = {
                        previewImagePath = app.panPhotoPath
                        previewImageTitle = "Old PAN Card Photo"
                    },
                    modifier = Modifier.fillMaxWidth(0.48f)
                )
            }

            AdminPhotoThumbnailCard(
                label = "Passport Size Photo",
                filePath = app.passportPhotoPath,
                onClick = {
                    previewImagePath = app.passportPhotoPath
                    previewImageTitle = "Passport Size Photo"
                },
                modifier = Modifier.fillMaxWidth(0.48f)
            )

            AdminPhotoThumbnailCard(
                label = "Applicant Signature",
                filePath = app.signaturePath,
                onClick = {
                    previewImagePath = app.signaturePath
                    previewImageTitle = "Applicant Signature"
                },
                modifier = Modifier.fillMaxWidth(0.48f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Customer Particulars Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Applicant Details / ग्राहक विवरण",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow("Customer Full Name", app.customerName)
                DetailRow("Father's Name", app.fatherName)
                DetailRow("Mother's Name", app.motherName.ifEmpty { "N/A" })
                DetailRow("Date of Birth", app.dob.ifEmpty { "N/A" })
                DetailRow("Mobile Number", app.mobile)
                DetailRow("Email Address", app.email.ifEmpty { "N/A" })
                DetailRow("Application Type", "${app.applicationType} (${if (app.physicalCardRequired) "Physical Card" else "e-PAN Only"})")
                DetailRow("Retailer Info", "${app.retailerName} • ${app.retailerShopName} (${app.retailerMobile})")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Admin Status & Disposition Update
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF8F3)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(SaffronOrange.copy(alpha = 0.4f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Update Processing Status / स्थिति अपडेट करें",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SaffronOrange
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Status Chips
                Text("Select Status:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == "PENDING",
                        onClick = { selectedStatus = "PENDING" },
                        label = { Text("Pending") }
                    )
                    FilterChip(
                        selected = selectedStatus == "IN_REVIEW",
                        onClick = { selectedStatus = "IN_REVIEW" },
                        label = { Text("In Review") }
                    )
                    FilterChip(
                        selected = selectedStatus == "ACK_GENERATED",
                        onClick = { selectedStatus = "ACK_GENERATED" },
                        label = { Text("Ack Ready") }
                    )
                    FilterChip(
                        selected = selectedStatus == "COMPLETED",
                        onClick = { selectedStatus = "COMPLETED" },
                        label = { Text("Completed") }
                    )
                    FilterChip(
                        selected = selectedStatus == "REJECTED",
                        onClick = { selectedStatus = "REJECTED" },
                        label = { Text("Rejected") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Acknowledgment Slip Number Input
                OutlinedTextField(
                    value = ackNumberInput,
                    onValueChange = { ackNumberInput = it },
                    label = { Text("PAN Acknowledgment / Slip Number (रसीद संख्या)") },
                    placeholder = { Text("e.g. 881023456789012") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Generated PAN Number Input
                OutlinedTextField(
                    value = panNumberInput,
                    onValueChange = { if (it.length <= 10) panNumberInput = it.uppercase() },
                    label = { Text("Allotted PAN Number (आवंटित पैन कार्ड नंबर)") },
                    placeholder = { Text("e.g. ABCDE1234F") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Admin Remarks
                OutlinedTextField(
                    value = remarksInput,
                    onValueChange = { remarksInput = it },
                    label = { Text("Remarks / कारण / नोट") },
                    placeholder = { Text("e.g. Form submitted on NSDL/UTI, slip attached") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.updateApplicationStatus(
                            id = app.id,
                            newStatus = selectedStatus,
                            ackNumber = ackNumberInput.trim(),
                            panNumber = panNumberInput.trim(),
                            remarks = remarksInput.trim()
                        )
                        showSaveSuccess = true
                        Toast.makeText(context, "Status Updated Successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Status & Update Retailer / सुरक्षित करें", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun AdminPhotoThumbnailCard(
    label: String,
    filePath: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFCBD5E1))
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (filePath.isNotEmpty() && File(filePath).exists()) {
                AsyncImage(
                    model = File(filePath),
                    contentDescription = label,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Not Attached",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Bottom Label Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdminRetailersView(
    retailers: List<RetailerEntity>,
    allApps: List<PanApplicationEntity>,
    viewModel: PanViewModel,
    onAddNewRetailer: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "RETAILER MANAGEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = SaffronOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "रिटेलर पंजीकरण एवं नियंत्रण",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Only Admin can create & manage retailers",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = onAddNewRetailer,
                        colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("+ Add")
                    }
                }
            }
        }

        item {
            Text(
                text = "REGISTERED RETAILERS / पंजीकृत रिटेलर्स (${retailers.size})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B)
            )
        }

        items(retailers, key = { it.id }) { retailer ->
            val retailerAppsCount = remember(allApps, retailer.id) {
                allApps.count { it.retailerId == retailer.id }
            }
            AdminRetailerCard(
                retailer = retailer,
                submissionsCount = retailerAppsCount,
                onToggleActive = { viewModel.toggleRetailerActive(retailer) },
                onDelete = { viewModel.deleteRetailer(retailer) }
            )
        }
    }
}

@Composable
fun AdminRetailerCard(
    retailer: RetailerEntity,
    submissionsCount: Int,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Retailer / रिटेलर हटाएँ") },
            text = { Text("Are you sure you want to delete ${retailer.name} (${retailer.shopName})?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = StatusRejected)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (retailer.isActive) Color(0xFFE2E8F0) else Color(0xFFFCA5A5)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (retailer.isActive) EmeraldGreen.copy(alpha = 0.15f) else Color(0xFFFEE2E2),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = if (retailer.isActive) EmeraldGreen else StatusRejected,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = retailer.shopName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Prop: ${retailer.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Switch(
                    checked = retailer.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = EmeraldGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mobile: ${retailer.mobile}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Login PIN: ${retailer.pin}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SaffronOrange,
                        fontWeight = FontWeight.Bold
                    )
                    if (retailer.address.isNotEmpty()) {
                        Text(
                            text = "Location: ${retailer.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(NavyPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$submissionsCount Applications",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Retailer",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddRetailerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, shop: String, mobile: String, pin: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var shopName by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Register New Retailer\nनया रिटेलर पंजीकृत करें",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = shopName,
                    onValueChange = { shopName = it },
                    label = { Text("Shop / Kendra Name (दुकान का नाम) *") },
                    placeholder = { Text("e.g. Gupta Jan Seva Kendra") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Retailer Full Name (रिटेलर का नाम) *") },
                    placeholder = { Text("e.g. Rajesh Gupta") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { if (it.length <= 10) mobile = it },
                    label = { Text("Retailer Mobile Number (10 अंक) *") },
                    placeholder = { Text("10 digit login mobile") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 6) pin = it },
                    label = { Text("Assign Login PIN (लॉगिन पिन) *") },
                    placeholder = { Text("e.g. 1234 or 4321") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Shop Address / City (स्थान)") },
                    placeholder = { Text("e.g. Market Road, Patna") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || shopName.isBlank() || mobile.length < 10 || pin.length < 4) {
                        error = "Please fill all required fields correctly (10-digit mobile, 4-digit PIN)"
                    } else {
                        onSave(name, shopName, mobile, pin, address)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("Create Retailer / बनाएं")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ChangeAdminPinDialog(
    currentPin: String,
    onDismiss: () -> Unit,
    onPinChanged: (String) -> Unit
) {
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var err by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Admin Master PIN", fontWeight = FontWeight.Bold, color = NavyPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current PIN: $currentPin", style = MaterialTheme.typography.bodySmall, color = Color(0xFF64748B))
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("New PIN (नया पिन)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("Confirm New PIN") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
                if (err != null) {
                    Text(err!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin.length < 4) {
                        err = "PIN must be at least 4 digits"
                    } else if (newPin != confirmPin) {
                        err = "PINs do not match"
                    } else {
                        onPinChanged(newPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SaffronOrange)
            ) {
                Text("Update PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MetricCard(
    title: String,
    hindi: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(color.copy(alpha = 0.25f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = hindi,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B),
            modifier = Modifier.weight(1.3f),
            textAlign = TextAlign.End
        )
    }
}
