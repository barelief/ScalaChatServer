// SaveDataModule.scala
// Handles persistence of chat messages to PostgreSQL database

package bare.chatserver

import java.sql.{Connection, DriverManager, PreparedStatement}
import com.typesafe.config.ConfigFactory

object SaveDataModule {
 // Load database configuration from application.conf
 private val config = ConfigFactory.load()
 private val dbConfig = config.getConfig("db")
 private val url = dbConfig.getString("url")
 private val user = dbConfig.getString("user") 
 private val password = dbConfig.getString("password")

 // Save chat message to database with sender, content and timestamp
 def saveData(sender: String, message: String): Unit = {
   var connection: Connection = null
   var preparedStatement: PreparedStatement = null

   try {
     // Establish database connection
     connection = DriverManager.getConnection(url, user, password)
     val timestamp = System.currentTimeMillis() / 1000 

     // Prepare and execute insert statement
     val sql = "INSERT INTO messages (sender, message, timestamp) VALUES (?, ?, ?)"
     preparedStatement = connection.prepareStatement(sql)
     preparedStatement.setString(1, sender)
     preparedStatement.setString(2, message)
     preparedStatement.setLong(3, timestamp)

     val rowsAffected = preparedStatement.executeUpdate()
     println(s"Inserted $rowsAffected row(s) with sender: $sender, message: $message")

   } catch {
     case e: Exception =>
       println(s"Error saving data: ${e.getMessage}")
   } finally {
     // Clean up database resources
     if (preparedStatement != null) preparedStatement.close()
     if (connection != null) connection.close() 
   }
 }
}
