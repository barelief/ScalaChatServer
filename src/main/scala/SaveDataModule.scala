package bare.chatserver

import java.sql.{Connection, DriverManager, PreparedStatement}
import com.typesafe.config.ConfigFactory

object SaveDataModule {
  // Load configuration from application.conf
  private val config = ConfigFactory.load()
  private val dbConfig = config.getConfig("db")
  private val url = dbConfig.getString("url")
  private val user = dbConfig.getString("user")
  private val password = dbConfig.getString("password")

  def saveData(sender: String, message: String): Unit = {
    var connection: Connection = null
    var preparedStatement: PreparedStatement = null

    try {
      connection = DriverManager.getConnection(url, user, password)
      val timestamp = System.currentTimeMillis() / 1000 // Current Unix timestamp
      val sql = "INSERT INTO messages (sender, message, timestamp) VALUES (?, ?, ?)"
      preparedStatement = connection.prepareStatement(sql)
      preparedStatement.setString(1, sender)
      preparedStatement.setString(2, message)
      preparedStatement.setLong(3, timestamp)

      val rowsAffected = preparedStatement.executeUpdate()
      println(s"Inserted $rowsAffected row(s) with sender: $sender, message: $message, timestamp: $timestamp")
    } catch {
      case e: Exception =>
        println(s"Error occurred while saving data: ${e.getMessage}")
    } finally {
      if (preparedStatement != null) preparedStatement.close()
      if (connection != null) connection.close()
    }
  }
}

