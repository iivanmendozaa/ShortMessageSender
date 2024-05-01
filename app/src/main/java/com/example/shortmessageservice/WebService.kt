package com.example.shortmessageservice
import fi.iki.elonen.NanoHTTPD
import java.net.SocketException

class WebService  : NanoHTTPD(8080) {

    private var messageSender: MessageSender = MessageSender()

    override fun serve(session: IHTTPSession): Response {
        val timeoutMillis = 10000 // Adjust this value as needed
      //  session.set

        return when {
            (session.method == Method.GET) -> handleGetRequest(session)
            (session.method == Method.POST && session.uri == "/sendMessage") -> handlePostEndpoint1(session)
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found")
        }
    }
    private fun handleGetRequest(session: IHTTPSession): Response {
        val uri = session.uri
        return when (uri) {
            "/health" -> handleHealthCheck()
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found")
        }
    }
    private fun handlePostEndpoint1(session: IHTTPSession): Response {
        try {

            // Read the request body as a string
            // Read the request body as a string
            val phoneNumber = session.parms.get("phoneNumber")
            val message = session.parms.get("message")

            // Check if the request body is empty or null
            if (phoneNumber.isNullOrEmpty()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Missing phoneNumber Parameter"
                )
            }
            if (message.isNullOrEmpty()) {
                return newFixedLengthResponse(
                    Response.Status.BAD_REQUEST,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Missing message Parameter"
                )
            }


            messageSender.sendSMS(phoneNumber,message)
            // Respond with a simple message indicating the received parameters
            return newFixedLengthResponse("Message Sent")
        } catch (e: SocketException) {
            println("SocketException WA");

            // Handle SocketException gracefully
            e.printStackTrace()
            // Return an empty response or appropriate error response
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Socket closed")
        } catch (e: Exception) {
            // Handle other exceptions
            e.printStackTrace()
            // Return an appropriate error response
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "Error handling request")
        }
    }

    private fun handleHealthCheck(): Response {
        // Respond with a simple message indicating that the server is healthy
        return newFixedLengthResponse("Server is up and running!")
    }

     fun startWebServer() {

        try {
            this@WebService.start()

            println("Web server started")
        } catch (e: Exception) {
            println("Error starting web server: ${e.message}")
        }
    }

    fun stopWebServer() {
        this@WebService.stop()
        println("Web server stopped")
    }



}

