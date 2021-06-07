package prm.project2.ui.login

/**
 * Data validation state of the login form.
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