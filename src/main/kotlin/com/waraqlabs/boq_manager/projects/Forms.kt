package com.waraqlabs.boq_manager.projects

import io.ktor.http.*

class ProjectEditForm(val parameters: Parameters, val project: Project, val locations: List<Location>, val products: List<Product>) {
    private fun <T> getDataList(parameterName: String, conversionF: String.() -> T): List<T> {
        val list = parameters.getAll(parameterName)
        return list?.map(conversionF) ?: listOf()
    }

    fun isValid(): Pair<Boolean, String?> {
        if (!locations.all { it.projectId == project.id }) {
            return false to "Location does not belong to the given project."
        }

        val locationIds = locations.map {it.id}.toSet()
        val validProductIds = products.map { it.id }.toSet()

        for (locationId in locationIds) {
            val productIdList = getDataList("boq-location-$locationId-product-id[]", String::toInt)
            val qtyInStoreList = getDataList("boq-location-$locationId-qty-in-store[]", String::toInt)
            val qtyToInstallList = getDataList("boq-location-$locationId-qty-to-install[]", String::toInt)

            if (productIdList.count() != qtyInStoreList.count() && productIdList.count() != qtyToInstallList.count()) {
                return false to "The data list counts do not match."
            }

            if (!productIdList.all {
                    validProductIds.contains(it)
                }) {
                return false to "Invalid product id(s)."
            }

            if (!(qtyInStoreList + qtyToInstallList).all { it > 0 }) {
                return false to "All quantities listed must be greater than 0."
            }
        }

        return true to null
    }
}