package prm.project2.ui.login

/**
 * Data validation state of the login form.
 */
data class LoginFormState(
    val emailError: Int? = null,
    val passwordError: Int? = null,
    val isEmailValid: Boolean = false,
    val isPasswordValid: Boolean = false
) {
    val isDataValid: Boolean
        get() = isEmailValid && isPasswordValid
}