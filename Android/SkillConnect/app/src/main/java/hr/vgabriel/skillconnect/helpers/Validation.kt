package hr.vgabriel.skillconnect.helpers

class Validation {
    companion object {
        private const val EMAIL_REGEX = "^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$"
        private const val PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$"

        fun isValidEmail(email: String): Boolean {
            return email.matches(Regex(EMAIL_REGEX))
        }

        fun isPasswordValid(password: String): Boolean {
            return password.matches(Regex(PASSWORD_REGEX))
        }

        fun getPasswordErrorMessage(): String {
            return "Password must be at least 8 characters long and contain at least one letter and one digit."
        }
    }
}