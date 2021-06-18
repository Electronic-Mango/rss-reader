package prm.project2.ui.login

/**
 * Data class storing entered email and password data during login or signup.
 */
data class UserdataFormState(
    val isEmailValid: Boolean = false,
    val emailErrorMessage: Int? = null,
    val isPasswordValid: Boolean = false,
    val passwordErrorMessage: Int? = null,
) {
    val isDataValid: Boolean
        get() = isEmailValid && isPasswordValid
}