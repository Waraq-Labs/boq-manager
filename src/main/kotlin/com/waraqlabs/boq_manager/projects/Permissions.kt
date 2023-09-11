package com.waraqlabs.boq_manager.projects

import com.waraqlabs.boq_manager.auth.UserRole

enum class Permission {
    CreateProject, EditProject, RecordWork, ViewProgressReport
}

/*
    The permissions model in this app is divided into 2 parts. The first; implemented here, is if a given role can take
    the action they are asking for. For example, an admin can take all actions, but a project manager can not create or
    edit projects.

    The second part of the permissions model is which projects a given user has access to. That is decided by the DAO.
    So even though a project manager has the ability to record work, they don't have it on all the projects in the
    database, only a subset of them.
     */
fun isAllowed(role: UserRole, permission: Permission) = when (role) {
    UserRole.ADMIN -> true

    UserRole.PROJECT_MANAGER -> when (permission) {
        Permission.CreateProject -> false
        Permission.EditProject -> false
        Permission.RecordWork -> true
        Permission.ViewProgressReport -> true
    }

    UserRole.CLIENT -> when (permission) {
        Permission.CreateProject -> false
        Permission.EditProject -> false
        Permission.RecordWork -> false
        Permission.ViewProgressReport -> true
    }
}