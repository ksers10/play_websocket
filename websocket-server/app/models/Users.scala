package models

/**
  * This singelton holds the List of registered users, their types
  * as well as methods for authenticating and authorizing users.
  */
object Users {
  private val _registeredUsers = Map(
    "user1234" -> "password1234",
    "user4321" -> "password4321",
    "admin" -> "secretpassword",
    "admin2" -> "secretpassword2"
  )
  private val _userGroups = Map(
    "user" -> Set("user1234", "user4321"),
    "admin" -> Set("admin", "admin2")
  )

  def authenticate(username: String, password: String): Either[String, String] = {
    val authenticated = _registeredUsers.exists { case (user, pass) => (user, pass) == (username, password) }
    if (authenticated) {
      Right(_authorize(username))
    } else Left("login_failed")
  }

  private def _authorize(user: String): String = {
    _userGroups.find { case (_, users) => users.contains(user) } match {
      case Some((userType, _)) => userType
      case None => ""
    }
  }

}
