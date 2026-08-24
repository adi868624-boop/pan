package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PanApplicationEntity
import com.example.data.model.RetailerEntity
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SaffronOrange
import com.example.ui.theme.StatusApproved
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusRejected
import com.example.ui.viewmodel.PanViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerMainScreen(
    viewModel: PanViewModel,
    retailer: RetailerEntity,
    modifier: Modifier = Modifier
) {
    var selectedNavIndex by remember { mutableIntStateOf(0) }
    val allApps by viewModel.allApplications.collectAsStateWithLifecycle()
    val myApps = remember(allApps, retailer.id) {
        allApps.filter { it.retailerId == retailer.id }
    }

    var selectedAppForDetail by remember { mutableStateOf<PanApplicationEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SaffronOrange.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = SaffronOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = retailer.shopName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Retailer: ${retailer.name} • ${retailer.mobile}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
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
                    selected = selectedNavIndex == 0,
                    onClick = { selectedNavIndex = 0 },
                    icon = {
                        Icon(Icons.Default.NoteAdd, contentDescription = "New Form")
                    },
                    label = {
                        Text("New Form / नया फॉर्म", fontWeight = if (selectedNavIndex == 0) FontWeight.Bold else FontWeight.Normal)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFE0EBF7)
                    )
                )

                NavigationBarItem(
                    selected = selectedNavIndex == 1,
                    onClick = { selectedNavIndex = 1 },
                    icon = {
                        Icon(Icons.Default.Assignment, contentDescription = "Submissions")
                    },
                    label = {
                        Text(
                            "My History (${myApps.size})",
                            fontWeight = if (selectedNavIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = Color(0xFFE0EBF7)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedNavIndex == 0) {
                PanFormScreen(
                    viewModel = viewModel,
                    retailer = retailer,
                    onSuccessSubmitted = {
                        selectedNavIndex = 1
                    }
                )
            } else {
                RetailerHistoryView(
                    applications = myApps,
                    onSelectApp = { selectedAppForDetail = it }
                )
            }

            if (selectedAppForDetail != null) {
                RetailerApplicationDetailDialog(
                    app = selectedAppForDetail!!,
                    onDismiss = { selectedAppForDetail = null }
                )
            }
        }
    }
}

@Composable
fun RetailerHistoryView(
    applications: List<PanApplicationEntity>,
    onSelectApp: (PanApplicationEntity) -> Unit
) {
    if (applications.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Submissions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "आपने अभी तक कोई आवेदन नहीं भेजा है। 'New Form' टैब से ग्राहक का दस्तावेज़ अपलोड करें।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "My Submissions History / मेरे भेजे गए आवेदन (${applications.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            }

            items(applications, key = { it.id }) { app ->
                RetailerAppHistoryCard(
                    app = app,
                    onClick = { onSelectApp(app) }
                )
            }
        }
    }
}

@Composable
fun RetailerAppHistoryCard(
    app: PanApplicationEntity,
    onClick: () -> Unit
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(app.createdAt))

    val statusColor = when (app.status) {
        "COMPLETED" -> StatusApproved
        "REJECTED" -> StatusRejected
        "ACK_GENERATED" -> Color(0xFF0288D1)
        else -> StatusPending
    }

    val statusLabel = when (app.status) {
        "COMPLETED" -> "Completed / पैन जारी"
        "REJECTED" -> "Rejected / अस्वीकृत"
        "ACK_GENERATED" -> "Ack Ready / रसीद जारी"
        "IN_REVIEW" -> "Under Review / जाँच जारी"
        else -> "Pending Admin / प्रतीक्षारत"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = app.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ref: ${app.applicationRef}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = app.mobile,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569)
                    )
                }

                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            }

            if (app.ackNumber.isNotEmpty() || app.panNumber.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (app.ackNumber.isNotEmpty()) {
                            Text(
                                text = "Ack No: ${app.ackNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                        if (app.panNumber.isNotEmpty()) {
                            Text(
                                text = "PAN: ${app.panNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }
                }
            }

            if (app.adminRemarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Remarks: ${app.adminRemarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (app.status == "REJECTED") StatusRejected else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun RetailerApplicationDetailDialog(
    app: PanApplicationEntity,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${app.customerName}'s Application",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Ref ID: ${app.applicationRef}", fontWeight = FontWeight.Medium)
                Text("Father Name: ${app.fatherName}")
                Text("Mother Name: ${app.motherName.ifEmpty { "N/A" }}")
                Text("Mobile: ${app.mobile}")
                Text("Email: ${app.email.ifEmpty { "N/A" }}")
                Text("DOB: ${app.dob.ifEmpty { "N/A" }}")
                Text("Status: ${app.status}", fontWeight = FontWeight.Bold, color = SaffronOrange)
                if (app.ackNumber.isNotEmpty()) {
                    Text("Ack Slip No: ${app.ackNumber}", color = EmeraldGreen, fontWeight = FontWeight.Bold)
                }
                if (app.panNumber.isNotEmpty()) {
                    Text("Allotted PAN: ${app.panNumber}", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
                if (app.adminRemarks.isNotEmpty()) {
                    Text("Admin Remarks: ${app.adminRemarks}")
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Close / बंद करें")
            }
        }
    )
}
