package com.axeven.profiteerapp.data.ui

/**
 * Manages dialog state for transaction creation screens.
 *
 * This consolidates all dialog-related state into a single object,
 * ensuring only one dialog can be open at a time and providing
 * clear state management for UI components.
 *
 * Business Rule: Only one dialog should be open at a time to
 * avoid UI conflicts and maintain clear user experience.
 */
data class DialogStates(
    val showDatePicker: Boolean = false,
    val showPhysicalWalletPicker: Boolean = false,
    val showLogicalWalletPicker: Boolean = false,
    val showSourceWalletPicker: Boolean = false,
    val showDestinationWalletPicker: Boolean = false
) {

    /**
     * Checks if any dialog is currently open.
     */
    val hasOpenDialog: Boolean
        get() = showDatePicker || showPhysicalWalletPicker || showLogicalWalletPicker ||
                showSourceWalletPicker || showDestinationWalletPicker

    /**
     * Gets the currently open dialog type, if any.
     */
    val openDialogType: DialogType?
        get() = when {
            showDatePicker -> DialogType.DATE_PICKER
            showPhysicalWalletPicker -> DialogType.PHYSICAL_WALLET
            showLogicalWalletPicker -> DialogType.LOGICAL_WALLET
            showSourceWalletPicker -> DialogType.SOURCE_WALLET
            showDestinationWalletPicker -> DialogType.DESTINATION_WALLET
            else -> null
        }

    /**
     * Opens date picker dialog, closing all others.
     */
    fun openDatePicker(): DialogStates {
        return DialogStates(showDatePicker = true)
    }

    /**
     * Opens physical wallet picker dialog, closing all others.
     */
    fun openPhysicalWalletPicker(): DialogStates {
        return DialogStates(showPhysicalWalletPicker = true)
    }

    /**
     * Opens logical wallet picker dialog, closing all others.
     */
    fun openLogicalWalletPicker(): DialogStates {
        return DialogStates(showLogicalWalletPicker = true)
    }

    /**
     * Opens source wallet picker dialog, closing all others.
     */
    fun openSourceWalletPicker(): DialogStates {
        return DialogStates(showSourceWalletPicker = true)
    }

    /**
     * Opens destination wallet picker dialog, closing all others.
     */
    fun openDestinationWalletPicker(): DialogStates {
        return DialogStates(showDestinationWalletPicker = true)
    }

    /**
     * Closes all dialogs.
     */
    fun closeAll(): DialogStates {
        return DialogStates()
    }

    /**
     * Closes a specific dialog type.
     */
    fun close(dialogType: DialogType): DialogStates {
        return when (dialogType) {
            DialogType.DATE_PICKER -> copy(showDatePicker = false)
            DialogType.PHYSICAL_WALLET -> copy(showPhysicalWalletPicker = false)
            DialogType.LOGICAL_WALLET -> copy(showLogicalWalletPicker = false)
            DialogType.SOURCE_WALLET -> copy(showSourceWalletPicker = false)
            DialogType.DESTINATION_WALLET -> copy(showDestinationWalletPicker = false)
        }
    }

    /**
     * Toggles a specific dialog type (opens if closed, closes if open).
     */
    fun toggle(dialogType: DialogType): DialogStates {
        return when (dialogType) {
            DialogType.DATE_PICKER -> {
                if (showDatePicker) closeAll() else openDatePicker()
            }
            DialogType.PHYSICAL_WALLET -> {
                if (showPhysicalWalletPicker) closeAll() else openPhysicalWalletPicker()
            }
            DialogType.LOGICAL_WALLET -> {
                if (showLogicalWalletPicker) closeAll() else openLogicalWalletPicker()
            }
            DialogType.SOURCE_WALLET -> {
                if (showSourceWalletPicker) closeAll() else openSourceWalletPicker()
            }
            DialogType.DESTINATION_WALLET -> {
                if (showDestinationWalletPicker) closeAll() else openDestinationWalletPicker()
            }
        }
    }

    /**
     * Checks if a specific dialog type is open.
     */
    fun isOpen(dialogType: DialogType): Boolean {
        return when (dialogType) {
            DialogType.DATE_PICKER -> showDatePicker
            DialogType.PHYSICAL_WALLET -> showPhysicalWalletPicker
            DialogType.LOGICAL_WALLET -> showLogicalWalletPicker
            DialogType.SOURCE_WALLET -> showSourceWalletPicker
            DialogType.DESTINATION_WALLET -> showDestinationWalletPicker
        }
    }

    /**
     * Gets all currently open dialogs (should be at most one).
     * Useful for debugging or validation.
     */
    val openDialogs: List<DialogType>
        get() = listOfNotNull(
            if (showDatePicker) DialogType.DATE_PICKER else null,
            if (showPhysicalWalletPicker) DialogType.PHYSICAL_WALLET else null,
            if (showLogicalWalletPicker) DialogType.LOGICAL_WALLET else null,
            if (showSourceWalletPicker) DialogType.SOURCE_WALLET else null,
            if (showDestinationWalletPicker) DialogType.DESTINATION_WALLET else null
        )

    /**
     * Validates that at most one dialog is open.
     * Returns error message if validation fails, null if valid.
     */
    fun validateSingleDialog(): String? {
        val openCount = openDialogs.size
        return if (openCount > 1) {
            "Multiple dialogs are open: ${openDialogs.joinToString(", ")}. Only one dialog should be open at a time."
        } else null
    }

    /**
     * Creates state with only wallet-related dialogs, closing others.
     * Useful for transaction type changes that affect available dialogs.
     */
    fun walletDialogsOnly(): DialogStates {
        return copy(
            showDatePicker = false
        )
    }

    /**
     * Creates state with only transfer-related dialogs, closing others.
     * Useful when switching to transfer transaction type.
     */
    fun transferDialogsOnly(): DialogStates {
        return copy(
            showDatePicker = false,
            showPhysicalWalletPicker = false,
            showLogicalWalletPicker = false
        )
    }

    /**
     * Creates state with only regular transaction dialogs, closing transfer dialogs.
     * Useful when switching from transfer to income/expense transaction type.
     */
    fun regularDialogsOnly(): DialogStates {
        return copy(
            showSourceWalletPicker = false,
            showDestinationWalletPicker = false
        )
    }
}

/**
 * Extension functions for more readable dialog management.
 */

/**
 * Creates DialogStates with only the specified dialog open.
 */
fun DialogType.openOnly(): DialogStates {
    return when (this) {
        DialogType.DATE_PICKER -> DialogStates(showDatePicker = true)
        DialogType.PHYSICAL_WALLET -> DialogStates(showPhysicalWalletPicker = true)
        DialogType.LOGICAL_WALLET -> DialogStates(showLogicalWalletPicker = true)
        DialogType.SOURCE_WALLET -> DialogStates(showSourceWalletPicker = true)
        DialogType.DESTINATION_WALLET -> DialogStates(showDestinationWalletPicker = true)
    }
}

/**
 * Human-readable names for dialog types.
 */
fun DialogType.displayName(): String {
    return when (this) {
        DialogType.DATE_PICKER -> "Date Picker"
        DialogType.PHYSICAL_WALLET -> "Physical Wallet Picker"
        DialogType.LOGICAL_WALLET -> "Logical Wallet Picker"
        DialogType.SOURCE_WALLET -> "Source Wallet Picker"
        DialogType.DESTINATION_WALLET -> "Destination Wallet Picker"
    }
}