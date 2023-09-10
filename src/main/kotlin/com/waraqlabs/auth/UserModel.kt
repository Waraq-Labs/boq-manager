package com.waraqlabs.auth

import com.waraqlabs.plugins.Database

object UserModel {
    fun doesUserExist(email: String): Boolean {
        val st = Database.connection.prepareStatement("SELECT count(*) FROM users WHERE email = ?")
        st.setString(1, email)

        val result = st.executeQuery()
        result.next()
        val totalMatchedRows = result.getInt("count")

        return totalMatchedRows == 1
    }
}