package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PanApplicationEntity
import com.example.data.model.RetailerEntity
import com.example.data.repository.ApplicationCounts
import com.example.data.repository.PanRepository
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface UserSession {
    data object None : UserSession
    data class RetailerSession(val retailer: RetailerEntity) : UserSession
    data object AdminSession : UserSession
}

data class PanFormState(
    val customerName: String = "",
    val fatherName: String = "",
    val motherName: String = "",
    val mobile: String = "",
    val email: String = "",
    val dob: String = "",
    val applicationType: String = "NEW_49A",
    val physicalCardRequired: Boolean = true,
    val aadharFrontPath: String = "",
    val aadharBackPath: String = "",
    val voterFrontPath: String = "",
    val voterBackPath: String = "",
    val panPhotoPath: String = "",
    val signaturePath: String = "",
    val passportPhotoPath: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successRefId: String? = null
)

class PanViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PanRepository

    private val _userSession = MutableStateFlow<UserSession>(UserSession.None)
    val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

    private val _formState = MutableStateFlow(PanFormState())
    val formState: StateFlow<PanFormState> = _formState.asStateFlow()

    private val _adminPin = MutableStateFlow("1234")
    val adminPin: StateFlow<String> = _adminPin.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _selectedApplication = MutableStateFlow<PanApplicationEntity?>(null)
    val selectedApplication: StateFlow<PanApplicationEntity?> = _selectedApplication.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = PanRepository(db.retailerDao(), db.panApplicationDao())
    }

    val allRetailers: StateFlow<List<RetailerEntity>> = repository.allRetailers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApplications: StateFlow<List<PanApplicationEntity>> = repository.allApplications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredApplications: StateFlow<List<PanApplicationEntity>> = combine(
        allApplications,
        _searchQuery,
        _statusFilter
    ) { apps, query, filter ->
        apps.filter { app ->
            val matchesFilter = when (filter) {
                "ALL" -> true
                "DONE" -> app.status == "COMPLETED" || app.status == "ACK_GENERATED"
                else -> app.status == filter
            }
            val matchesQuery = query.isBlank() ||
                    app.customerName.contains(query, ignoreCase = true) ||
                    app.mobile.contains(query, ignoreCase = true) ||
                    app.applicationRef.contains(query, ignoreCase = true) ||
                    app.retailerName.contains(query, ignoreCase = true) ||
                    app.retailerShopName.contains(query, ignoreCase = true) ||
                    app.fatherName.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setRetailerSession(retailer: RetailerEntity) {
        _userSession.value = UserSession.RetailerSession(retailer)
    }

    fun setAdminSession() {
        _userSession.value = UserSession.AdminSession
    }

    fun logout() {
        _userSession.value = UserSession.None
        _selectedApplication.value = null
        resetForm()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun selectApplication(app: PanApplicationEntity?) {
        _selectedApplication.value = app
    }

    // Retailer Authentication
    fun loginRetailer(mobile: String, pin: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (mobile.isBlank() || pin.isBlank()) {
                onResult(false, "Please enter mobile number and PIN / मोबाइल नंबर और पिन दर्ज करें")
                return@launch
            }
            val retailer = repository.authenticateRetailer(mobile, pin)
            if (retailer == null) {
                onResult(false, "Invalid Mobile or PIN / गलत मोबाइल नंबर या पिन")
            } else if (!retailer.isActive) {
                onResult(false, "Your retailer account is inactive. Please contact Admin. / आपका खाता निष्क्रिय है, एडमिन से संपर्क करें")
            } else {
                _userSession.value = UserSession.RetailerSession(retailer)
                onResult(true, null)
            }
        }
    }

    // Admin Authentication
    fun loginAdmin(pin: String, onResult: (Boolean, String?) -> Unit) {
        if (pin.trim() == _adminPin.value) {
            _userSession.value = UserSession.AdminSession
            onResult(true, null)
        } else {
            onResult(false, "Incorrect Admin PIN / गलत एडमिन पिन")
        }
    }

    fun updateAdminPin(newPin: String) {
        _adminPin.value = newPin
    }

    // Admin: Retailer Management
    fun createRetailer(
        name: String,
        shopName: String,
        mobile: String,
        pin: String,
        address: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (name.isBlank() || shopName.isBlank() || mobile.length < 10 || pin.length < 4) {
                onError("Please fill all required retailer fields properly / कृपया सभी विवरण सही से भरें")
                return@launch
            }
            val existing = repository.getRetailerByMobile(mobile)
            if (existing != null) {
                onError("A retailer with this mobile already exists / इस नंबर पर पहले से रिटेलर मौजूद है")
                return@launch
            }
            repository.insertRetailer(
                RetailerEntity(
                    name = name.trim(),
                    shopName = shopName.trim(),
                    mobile = mobile.trim(),
                    pin = pin.trim(),
                    address = address.trim(),
                    isActive = true
                )
            )
            onSuccess()
        }
    }

    fun toggleRetailerActive(retailer: RetailerEntity) {
        viewModelScope.launch {
            repository.updateRetailer(retailer.copy(isActive = !retailer.isActive))
        }
    }

    fun updateRetailer(retailer: RetailerEntity) {
        viewModelScope.launch {
            repository.updateRetailer(retailer)
        }
    }

    fun deleteRetailer(retailer: RetailerEntity) {
        viewModelScope.launch {
            repository.deleteRetailer(retailer)
        }
    }

    // Form Field Updates
    fun updateCustomerName(name: String) = _formState.update { it.copy(customerName = name, errorMessage = null) }
    fun updateFatherName(name: String) = _formState.update { it.copy(fatherName = name, errorMessage = null) }
    fun updateMotherName(name: String) = _formState.update { it.copy(motherName = name, errorMessage = null) }
    fun updateMobile(mobile: String) = _formState.update { it.copy(mobile = mobile, errorMessage = null) }
    fun updateEmail(email: String) = _formState.update { it.copy(email = email, errorMessage = null) }
    fun updateDob(dob: String) = _formState.update { it.copy(dob = dob, errorMessage = null) }
    fun updateApplicationType(type: String) = _formState.update { it.copy(applicationType = type) }
    fun updatePhysicalCardRequired(required: Boolean) = _formState.update { it.copy(physicalCardRequired = required) }

    // Form Image Updates
    fun setAadharFrontImage(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "aadhar_front"
            )
            _formState.update { it.copy(aadharFrontPath = path, errorMessage = null) }
        }
    }

    fun setAadharBackImage(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "aadhar_back"
            )
            _formState.update { it.copy(aadharBackPath = path, errorMessage = null) }
        }
    }

    fun setVoterFrontImage(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "voter_front"
            )
            _formState.update { it.copy(voterFrontPath = path) }
        }
    }

    fun setVoterBackImage(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "voter_back"
            )
            _formState.update { it.copy(voterBackPath = path) }
        }
    }

    fun setPanPhoto(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "pan_existing"
            )
            _formState.update { it.copy(panPhotoPath = path) }
        }
    }

    fun setPassportPhoto(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "passport_photo"
            )
            _formState.update { it.copy(passportPhotoPath = path, errorMessage = null) }
        }
    }

    fun setSignatureImageUri(uri: Uri) {
        viewModelScope.launch {
            val path = ImageStorageHelper.copyUriToAppStorage(
                getApplication(),
                uri,
                "temp_${System.currentTimeMillis()}",
                "signature"
            )
            _formState.update { it.copy(signaturePath = path, errorMessage = null) }
        }
    }

    fun setSignatureBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            val path = ImageStorageHelper.saveBitmapToFile(
                getApplication(),
                bitmap,
                "temp_${System.currentTimeMillis()}",
                "signature"
            )
            _formState.update { it.copy(signaturePath = path, errorMessage = null) }
        }
    }

    fun removeImage(field: String) {
        when (field) {
            "aadharFront" -> _formState.update { it.copy(aadharFrontPath = "") }
            "aadharBack" -> _formState.update { it.copy(aadharBackPath = "") }
            "voterFront" -> _formState.update { it.copy(voterFrontPath = "") }
            "voterBack" -> _formState.update { it.copy(voterBackPath = "") }
            "panPhoto" -> _formState.update { it.copy(panPhotoPath = "") }
            "passportPhoto" -> _formState.update { it.copy(passportPhotoPath = "") }
            "signature" -> _formState.update { it.copy(signaturePath = "") }
        }
    }

    // Submit Application
    fun submitApplication(onComplete: (Boolean, String) -> Unit) {
        val state = _formState.value
        val session = _userSession.value as? UserSession.RetailerSession
        if (session == null) {
            onComplete(false, "Retailer session not active / कृपया पुनः लॉगिन करें")
            return
        }

        // Validation
        if (state.aadharFrontPath.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Please capture/upload Aadhaar Front Photo / आधार कार्ड के आगे का फोटो आवश्यक है") }
            onComplete(false, "Aadhaar Front photo is required")
            return
        }
        if (state.aadharBackPath.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Please capture/upload Aadhaar Back Photo / आधार कार्ड के पीछे का फोटो आवश्यक है") }
            onComplete(false, "Aadhaar Back photo is required")
            return
        }
        if (state.signaturePath.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Please capture/draw Signature / हस्ताक्षर (साइन) फोटो आवश्यक है") }
            onComplete(false, "Signature is required")
            return
        }
        if (state.passportPhotoPath.isEmpty()) {
            _formState.update { it.copy(errorMessage = "Please capture Passport Size Photo / पासपोर्ट साइज फोटो आवश्यक है") }
            onComplete(false, "Passport photo is required")
            return
        }
        if (state.fatherName.isBlank()) {
            _formState.update { it.copy(errorMessage = "Please enter Father's Name / पिता का नाम दर्ज करें") }
            onComplete(false, "Father's name is required")
            return
        }
        if (state.motherName.isBlank()) {
            _formState.update { it.copy(errorMessage = "Please enter Mother's Name / माता का नाम दर्ज करें") }
            onComplete(false, "Mother's name is required")
            return
        }
        if (state.mobile.length < 10) {
            _formState.update { it.copy(errorMessage = "Please enter valid 10-digit Mobile Number / मान्य 10 अंकों का मोबाइल नंबर दर्ज करें") }
            onComplete(false, "Valid Mobile Number is required")
            return
        }
        if (state.customerName.isBlank()) {
            _formState.update { it.copy(errorMessage = "Please enter Applicant's Full Name / ग्राहक का पूरा नाम दर्ज करें") }
            onComplete(false, "Applicant Name is required")
            return
        }

        _formState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                val dateCode = SimpleDateFormat("yyMMdd", Locale.getDefault()).format(Date(timestamp))
                val randomCode = UUID.randomUUID().toString().take(4).uppercase()
                val refId = "PAN-$dateCode-$randomCode"

                val folderName = "${dateCode}_${state.customerName.replace(" ", "_")}_$randomCode"

                val finalAadharFront = copyFileToPermanentFolder(state.aadharFrontPath, folderName, "aadhar_front")
                val finalAadharBack = copyFileToPermanentFolder(state.aadharBackPath, folderName, "aadhar_back")
                val finalVoterFront = copyFileToPermanentFolder(state.voterFrontPath, folderName, "voter_front")
                val finalVoterBack = copyFileToPermanentFolder(state.voterBackPath, folderName, "voter_back")
                val finalPanPhoto = copyFileToPermanentFolder(state.panPhotoPath, folderName, "pan_existing")
                val finalSignature = copyFileToPermanentFolder(state.signaturePath, folderName, "signature")
                val finalPassport = copyFileToPermanentFolder(state.passportPhotoPath, folderName, "passport")

                val application = PanApplicationEntity(
                    applicationRef = refId,
                    retailerId = session.retailer.id,
                    retailerName = session.retailer.name,
                    retailerShopName = session.retailer.shopName,
                    retailerMobile = session.retailer.mobile,
                    customerName = state.customerName.trim(),
                    fatherName = state.fatherName.trim(),
                    motherName = state.motherName.trim(),
                    mobile = state.mobile.trim(),
                    email = state.email.trim(),
                    dob = state.dob.trim(),
                    applicationType = state.applicationType,
                    physicalCardRequired = state.physicalCardRequired,
                    aadharFrontPath = finalAadharFront,
                    aadharBackPath = finalAadharBack,
                    voterFrontPath = finalVoterFront,
                    voterBackPath = finalVoterBack,
                    panPhotoPath = finalPanPhoto,
                    signaturePath = finalSignature,
                    passportPhotoPath = finalPassport,
                    status = "PENDING",
                    createdAt = timestamp,
                    updatedAt = timestamp
                )

                repository.insertApplication(application)

                _formState.update {
                    it.copy(
                        isSubmitting = false,
                        successRefId = refId
                    )
                }
                onComplete(true, refId)
            } catch (e: Exception) {
                _formState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Submission failed: ${e.localizedMessage}"
                    )
                }
                onComplete(false, e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun copyFileToPermanentFolder(sourcePath: String, folderName: String, prefix: String): String {
        if (sourcePath.isBlank()) return ""
        val sourceFile = java.io.File(sourcePath)
        if (!sourceFile.exists()) return sourcePath
        val destDir = java.io.File(getApplication<Application>().filesDir, "pan_records/$folderName")
        if (!destDir.exists()) destDir.mkdirs()
        val destFile = java.io.File(destDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        sourceFile.copyTo(destFile, overwrite = true)
        return destFile.absolutePath
    }

    fun resetForm() {
        _formState.value = PanFormState()
    }

    // Admin: Update Application Status
    fun updateApplicationStatus(
        id: Long,
        newStatus: String,
        ackNumber: String,
        panNumber: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.updateApplicationStatus(id, newStatus, ackNumber, panNumber, remarks)
            // Update selected application in memory too
            val updated = repository.getApplicationById(id)
            _selectedApplication.value = updated
        }
    }

    fun deleteApplication(app: PanApplicationEntity) {
        viewModelScope.launch {
            repository.deleteApplication(app)
            _selectedApplication.value = null
        }
    }
}
