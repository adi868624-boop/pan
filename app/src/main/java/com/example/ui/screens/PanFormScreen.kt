package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.RetailerEntity
import com.example.ui.components.DocumentPhotoPickerCard
import com.example.ui.components.SignaturePadDialog
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.SaffronOrange
import com.example.ui.viewmodel.PanViewModel
import java.util.Calendar

@Composable
fun PanFormScreen(
    viewModel: PanViewModel,
    retailer: RetailerEntity,
    onSuccessSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showSignatureDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var submittedRefId by remember { mutableStateOf("") }

    // Date of birth picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
            viewModel.updateDob(formattedDate)
        },
        calendar.get(Calendar.YEAR) - 20,
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showSignatureDialog) {
        SignaturePadDialog(
            onDismiss = { showSignatureDialog = false },
            onSignatureCaptured = { bitmap ->
                viewModel.setSignatureBitmap(bitmap)
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                viewModel.resetForm()
            },
            shape = RoundedCornerShape(20.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(EmeraldGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Application Submitted!\nआवेदन सफलतापूर्वक भेजा गया",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = NavyPrimary
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your customer application has been saved to the secure date/time folder on Admin panel.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Application Reference No:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = submittedRefId,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SaffronOrange
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetForm()
                        onSuccessSubmitted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Next Customer / अगला फॉर्म भरें")
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(16.dp)
    ) {
        // Retailer Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = NavyPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "NEW PAN APPLICATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SaffronOrange,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "नया पैन कार्ड दस्तावेज़ फॉर्म",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Retailer: ${retailer.name} (${retailer.shopName})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Application Type Selector
        Text(
            text = "Select PAN Application Type / आवेदन प्रकार",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = formState.applicationType == "NEW_49A",
                onClick = { viewModel.updateApplicationType("NEW_49A") },
                label = { Text("New PAN (49A)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = formState.applicationType == "CORRECTION",
                onClick = { viewModel.updateApplicationType("CORRECTION") },
                label = { Text("Correction / सुधार (CR)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = formState.applicationType == "MINOR",
                onClick = { viewModel.updateApplicationType("MINOR") },
                label = { Text("Minor / नाबालिग") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = NavyPrimary,
                    selectedLabelColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 1: Aadhaar Card Photos (Front & Back)
        Text(
            text = "1. Aadhaar Card Photos / आधार कार्ड फोटो",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocumentPhotoPickerCard(
            title = "Aadhaar Card Front",
            hindiTitle = "आधार कार्ड आगे का फोटो",
            subtitle = "Clear photo showing name, DOB and photo",
            icon = Icons.Default.Badge,
            filePath = formState.aadharFrontPath,
            isRequired = true,
            onImageCapturedOrPicked = { uri -> viewModel.setAadharFrontImage(uri) },
            onRemoveImage = { viewModel.removeImage("aadharFront") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DocumentPhotoPickerCard(
            title = "Aadhaar Card Back",
            hindiTitle = "आधार कार्ड पीछे का फोटो",
            subtitle = "Clear photo showing complete address and QR",
            icon = Icons.Default.Badge,
            filePath = formState.aadharBackPath,
            isRequired = true,
            onImageCapturedOrPicked = { uri -> viewModel.setAadharBackImage(uri) },
            onRemoveImage = { viewModel.removeImage("aadharBack") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 2: Voter ID Card Photos (Front & Back)
        Text(
            text = "2. Voter ID Card Photos / वोटर आईडी कार्ड फोटो",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocumentPhotoPickerCard(
            title = "Voter ID Card Front",
            hindiTitle = "वोटर आईडी कार्ड आगे का फोटो",
            subtitle = "Optional / वैकल्पिक पहचान प्रमाण",
            icon = Icons.Default.HowToVote,
            filePath = formState.voterFrontPath,
            isRequired = false,
            onImageCapturedOrPicked = { uri -> viewModel.setVoterFrontImage(uri) },
            onRemoveImage = { viewModel.removeImage("voterFront") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        DocumentPhotoPickerCard(
            title = "Voter ID Card Back",
            hindiTitle = "वोटर आईडी कार्ड पीछे का फोटो",
            subtitle = "Optional / वैकल्पिक पहचान प्रमाण",
            icon = Icons.Default.HowToVote,
            filePath = formState.voterBackPath,
            isRequired = false,
            onImageCapturedOrPicked = { uri -> viewModel.setVoterBackImage(uri) },
            onRemoveImage = { viewModel.removeImage("voterBack") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 3: Existing PAN Card Photo (Optional / If Correction or Reprint)
        Text(
            text = "3. PAN Card Photo / पैन कार्ड फोटो",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocumentPhotoPickerCard(
            title = "Existing PAN Card Photo",
            hindiTitle = "पुराने पैन कार्ड की फोटो",
            subtitle = "Required if correction or reprint / सुधार या दोबारा बनवाने के लिए",
            icon = Icons.Default.CreditCard,
            filePath = formState.panPhotoPath,
            isRequired = formState.applicationType == "CORRECTION",
            onImageCapturedOrPicked = { uri -> viewModel.setPanPhoto(uri) },
            onRemoveImage = { viewModel.removeImage("panPhoto") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 4: Applicant Signature / Thumb Impression
        Text(
            text = "4. Signature / हस्ताक्षर फोटो या लाइव साइन",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocumentPhotoPickerCard(
            title = "Applicant Signature / Thumb",
            hindiTitle = "हस्ताक्षर फोटो खींचें या स्क्रीन पर साइन करें",
            subtitle = "White paper sign photo or screen finger signature",
            icon = Icons.Default.Draw,
            filePath = formState.signaturePath,
            isRequired = true,
            allowDigitalSign = true,
            onImageCapturedOrPicked = { uri -> viewModel.setSignatureImageUri(uri) },
            onDigitalSignClick = { showSignatureDialog = true },
            onRemoveImage = { viewModel.removeImage("signature") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 5: Passport Size Photo
        Text(
            text = "5. Passport Photo / पासपोर्ट साइज फोटो",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))

        DocumentPhotoPickerCard(
            title = "Passport Size Photo",
            hindiTitle = "पासपोर्ट साइज रंगीन फोटो",
            subtitle = "Recent colored passport photo with clear face",
            icon = Icons.Default.Face,
            filePath = formState.passportPhotoPath,
            isRequired = true,
            onImageCapturedOrPicked = { uri -> viewModel.setPassportPhoto(uri) },
            onRemoveImage = { viewModel.removeImage("passportPhoto") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(20.dp))

        // SECTION 6: Applicant Personal Particulars
        Text(
            text = "6. Personal & Family Details / व्यक्तिगत विवरण",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Customer Full Name
        OutlinedTextField(
            value = formState.customerName,
            onValueChange = { viewModel.updateCustomerName(it) },
            label = { Text("Customer Full Name (ग्राहक का पूरा नाम) *") },
            placeholder = { Text("As per Aadhaar Card") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Father's Name
        OutlinedTextField(
            value = formState.fatherName,
            onValueChange = { viewModel.updateFatherName(it) },
            label = { Text("Father's Name (पिता का नाम) *") },
            placeholder = { Text("Father's full name to be printed on PAN") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Mother's Name
        OutlinedTextField(
            value = formState.motherName,
            onValueChange = { viewModel.updateMotherName(it) },
            label = { Text("Mother's Name (माता का नाम) *") },
            placeholder = { Text("Mother's full name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Date of Birth
        OutlinedTextField(
            value = formState.dob,
            onValueChange = { viewModel.updateDob(it) },
            label = { Text("Date of Birth (जन्म तिथि - DD/MM/YYYY) *") },
            placeholder = { Text("DD/MM/YYYY") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = NavyPrimary) },
            trailingIcon = {
                IconButton(onClick = { datePickerDialog.show() }) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = NavyPrimary)
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Mobile Number
        OutlinedTextField(
            value = formState.mobile,
            onValueChange = { if (it.length <= 10) viewModel.updateMobile(it) },
            label = { Text("Mobile Number (मोबाइल नंबर) *") },
            placeholder = { Text("10 digit customer mobile") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Email ID
        OutlinedTextField(
            value = formState.email,
            onValueChange = { viewModel.updateEmail(it) },
            label = { Text("Email ID (ईमेल आईडी)") },
            placeholder = { Text("customer@example.com (For e-PAN)") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                focusedLabelColor = NavyPrimary
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Physical Card Checkbox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { viewModel.updatePhysicalCardRequired(!formState.physicalCardRequired) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = formState.physicalCardRequired,
                onCheckedChange = { viewModel.updatePhysicalCardRequired(it) },
                colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Physical PAN Card Required",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "प्लास्टिक कार्ड डाक द्वारा घर पर भेजा जाएगा",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = formState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = formState.errorMessage ?: "",
                        color = Color(0xFF991B1B),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big Submit Button
        Button(
            onClick = {
                viewModel.submitApplication { success, ref ->
                    if (success) {
                        submittedRefId = ref
                        showSuccessDialog = true
                    }
                }
            },
            enabled = !formState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
            if (formState.isSubmitting) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Saving & Uploading Data...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SUBMIT TO ADMIN / एडमिन को भेजें",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
