package com.example.schnappser

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


fun getConnectionParameters(context: Context): String {
    val sb = StringBuilder()
    sb.append("servername=").append(context.getString(R.string.servername))
    sb.append("&dbname=").append(context.getString(R.string.dbname))
    sb.append("&username=").append(context.getString(R.string.username))
    sb.append("&password=").append(context.getString(R.string.password))
    return sb.toString()
}

fun getBoundariesParameters(mMap: GoogleMap): String {
    val position = mMap.cameraPosition.target
    val zoom = mMap.cameraPosition.zoom.toDouble()
    val northBound = position.latitude + 1.0 / Math.pow(2.0, zoom - 1) / 0.005
    val southBound = position.latitude - 1.0 / Math.pow(2.0, zoom - 1) / 0.005
    val westBound = position.longitude - 1.0 / Math.pow(2.0, zoom - 1) / 0.005
    val eastBound = position.longitude + 1.0 / Math.pow(2.0, zoom - 1) / 0.005

    val sb = StringBuilder()
    sb.append("northBound=").append(northBound).append("&southBound=").append(southBound)
    sb.append("&westBound=").append(westBound).append("&eastBound=").append(eastBound)
    return sb.toString()
}

fun setSchnappserParametres(id: String, name: String, latLng: LatLng, next_latLng: LatLng?): String {
    val sb = StringBuilder()
    sb.append("id=").append(id).append("&name=").append(name).append("&status=").append(1)
    sb.append("&latitude=").append(latLng.latitude).append("&longitude=").append(latLng.longitude)
    if (next_latLng == null) {
        sb.append("&next_latitude=NULL").append("&next_longitude=NULL")
    } else {
        sb.append("&next_latitude=").append(next_latLng.latitude).append("&next_longitude=")
            .append(next_latLng.longitude)
    }
    return sb.toString()
}

fun setUserParametres(id_user: String, id_schnappser: String?, latLng: LatLng): String {
    val sb = StringBuilder()
    sb.append("id_user=").append(id_user)
    if (id_schnappser!=null) {
        sb.append("&id_schnappser=").append(id_schnappser).append("&status=").append(1)
    }
    else{
        sb.append("&id_schnappser=null").append("&status=").append(0)
    }
    sb.append("&latitude=").append(latLng.latitude).append("&longitude=").append(latLng.longitude)
    return sb.toString()
}

fun sendPostRequest(context: Context, url: String, urlParameters: String, toJSON: Boolean): MutableList<JSONObject> {

    val urlParams = if (urlParameters == "") {
        getConnectionParameters(context)
    } else {
        getConnectionParameters(context) + "&" + urlParameters
    }

    val mURL = URL(url)

    with(mURL.openConnection() as HttpURLConnection) {
        // optional default is GET
        requestMethod = "POST"
        println(urlParams)
        val wr = OutputStreamWriter(outputStream)
        wr.write(urlParams)
        wr.flush()

        println("URL : $url")
        println("Response Code : $responseCode")

        var response = StringBuffer()
        BufferedReader(InputStreamReader(inputStream)).use {
            var inputLine = it.readLine()
            while (inputLine != null) {
                response.append(inputLine)
                inputLine = it.readLine()
            }
            it.close()
            println("Response : $response")
        }

        val responseJSONArray = mutableListOf<JSONObject>()

        if (response.toString() == "0 results" || response.toString() == "New record created successfully") {
            return responseJSONArray
        }

        if (toJSON) {
            val responseString = response.toString().substring(0, response.length - 1)
            val responseArray = responseString.split("\\|".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

            for (resp in responseArray) {
                responseJSONArray.add(JSONObject(resp))
            }
        } else
            responseJSONArray.add(JSONObject("{'response': '" + response + "'}"))

        return responseJSONArray
    }
}
