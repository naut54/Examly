package com.octal.examly.utils

object Constants {
    const val DATABASE_NAME = "exam_database"
    const val DATABASE_VERSION = 1

    const val PREFS_NAME = "exam_prefs"
    const val KEY_USER_ID = "user_id"
    const val KEY_USERNAME = "username"
    const val KEY_USER_ROLE = "user_role"

    const val MIN_USERNAME_LENGTH = 3
    const val MIN_PASSWORD_LENGTH = 6
    const val MIN_ANSWERS_PER_QUESTION = 2

    const val PASSING_SCORE = 50.0
    const val DEFAULT_TIME_LIMIT = 60

    const val DATE_FORMAT = "dd/MM/yyyy"
    const val TIME_FORMAT = "HH:mm:ss"
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm"

    const val ERROR_EMPTY_USERNAME = "El nombre de usuario no puede estar vacío"
    const val ERROR_EMPTY_PASSWORD = "La contraseña no puede estar vacía"
    const val ERROR_SHORT_USERNAME = "El nombre de usuario debe tener al menos $MIN_USERNAME_LENGTH caracteres"
    const val ERROR_SHORT_PASSWORD = "La contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
    const val ERROR_INVALID_CREDENTIALS = "Credenciales inválidas"
    const val ERROR_USER_NOT_FOUND = "Usuario no encontrado"
    const val ERROR_USER_EXISTS = "El usuario ya existe"
    const val ERROR_NETWORK = "Error de conexión"
    const val ERROR_UNKNOWN = "Error desconocido"

    const val DEBOUNCE_TIME = 300L
    const val SPLASH_DELAY = 2000L
}