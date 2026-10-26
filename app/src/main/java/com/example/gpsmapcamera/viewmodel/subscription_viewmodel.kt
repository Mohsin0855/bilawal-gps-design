package com.example.gpsmapcamera.viewmodel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Plan { YEARLY, WEEKLY }

sealed class SubscriptionEvent {
    data class StartPurchase(val plan: Plan) : SubscriptionEvent()
}

class SubscriptionViewModel : ViewModel() {

    private val _selectedPlan = MutableStateFlow(Plan.YEARLY)
    val selectedPlan: StateFlow<Plan> = _selectedPlan.asStateFlow()

    private val _events = MutableSharedFlow<SubscriptionEvent>()
    val events: SharedFlow<SubscriptionEvent> = _events.asSharedFlow()

    fun selectPlan(plan: Plan) {
        _selectedPlan.value = plan
    }

    suspend fun onContinueClicked() {
        _events.emit(SubscriptionEvent.StartPurchase(_selectedPlan.value))
    }
}