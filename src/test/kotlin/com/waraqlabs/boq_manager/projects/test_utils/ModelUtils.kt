package com.waraqlabs.boq_manager.projects.test_utils

import com.waraqlabs.boq_manager.plugins.Database


fun clearDb() {
    Database.connection.prepareStatement("TRUNCATE projects CASCADE").execute()
    Database.connection.prepareStatement("TRUNCATE project_locations CASCADE").execute()
}