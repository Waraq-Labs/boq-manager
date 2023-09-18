package com.waraqlabs.boq_manager.projects

import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectEditFormTests {
    val now = Clock.System::now

    @Test
    fun `test with valid data`() {
        val project = Project(1, "Test project 1", true, now())
        val location = Location(1, "Test location 1", 1, now())
        val products = listOf(
            Product(1, "Test product 1", now(), 1),
            Product(2, "Test product 2", now(), 1),
            Product(3, "Test product 3", now(), 1),
        )
        val testParams = parametersOf(
            "boq-location-${location.id}-product-id[]" to listOf(
                "${products[0].id}",
                "${products[1].id}",
                "${products[2].id}",
            ),
            "boq-location-${location.id}-qty-in-store[]" to listOf(
                "10",
                "100",
                "1000",
            ),
            "boq-location-${location.id}-qty-to-install[]" to listOf(
                "5",
                "50",
                "500",
            )
        )
        val form = ProjectEditForm(testParams, project, listOf(location), products)

        assert(form.isValid().first)
    }

    @Test
    fun `test with missing data`() {
        val project = Project(1, "Test project 1", true, now())
        val location = Location(1, "Test location 1", 1, now())
        val products = listOf(
            Product(1, "Test product 1", now(), 1),
            Product(2, "Test product 2", now(), 1),
            Product(3, "Test product 3", now(), 1),
        )
        val testParams = parametersOf(
            "boq-location-${location.id}-product-id[]" to listOf(
                "${products[0].id}",
                "${products[1].id}",
            ),
            "boq-location-${location.id}-qty-in-store[]" to listOf(
                "10",
                "100",
                "1000",
            ),
            "boq-location-${location.id}-qty-to-install[]" to listOf(
                "5",
                "50",
                "500",
            )
        )
        val form = ProjectEditForm(testParams, project, listOf(location), products)

        assert(!form.isValid().first)
        assertEquals(form.isValid().second, "The data list counts do not match.")
    }

    @Test
    fun `test with location belonging to a different project`() {
        val project = Project(1, "Test project 1", true, now())
        val location = Location(1, "Test location 1", 2, now())
        val products = listOf(
            Product(1, "Test product 1", now(), 1),
            Product(2, "Test product 2", now(), 1),
            Product(3, "Test product 3", now(), 1),
        )
        val testParams = parametersOf(
            "boq-location-${location.id}-product-id[]" to listOf(
                "${products[0].id}",
                "${products[1].id}",
                "${products[2].id}",
            ),
            "boq-location-${location.id}-qty-in-store[]" to listOf(
                "10",
                "100",
                "1000",
            ),
            "boq-location-${location.id}-qty-to-install[]" to listOf(
                "5",
                "50",
                "500",
            )
        )
        val form = ProjectEditForm(testParams, project, listOf(location), products)

        assert(!form.isValid().first)
        assertEquals(form.isValid().second, "Location does not belong to the given project.")
    }

    @Test
    fun `test with invalid product ids`() {
        val project = Project(1, "Test project 1", true, now())
        val location = Location(1, "Test location 1", 1, now())
        val products = listOf(
            Product(1, "Test product 1", now(), 1),
            Product(2, "Test product 2", now(), 1),
            Product(3, "Test product 3", now(), 1),
        )
        val testParams = parametersOf(
            "boq-location-${location.id}-product-id[]" to listOf(
                "${products[0].id}",
                "${products[1].id}",
                "100",
            ),
            "boq-location-${location.id}-qty-in-store[]" to listOf(
                "10",
                "100",
                "1000",
            ),
            "boq-location-${location.id}-qty-to-install[]" to listOf(
                "5",
                "50",
                "500",
            )
        )
        val form = ProjectEditForm(testParams, project, listOf(location), products)

        assert(!form.isValid().first)
        assertEquals(form.isValid().second, "Invalid product id(s).")
    }

    @Test
    fun `test with invalid quantities`() {
        val project = Project(1, "Test project 1", true, now())
        val location = Location(1, "Test location 1", 1, now())
        val products = listOf(
            Product(1, "Test product 1", now(), 1),
            Product(2, "Test product 2", now(), 1),
            Product(3, "Test product 3", now(), 1),
        )
        val testParams = parametersOf(
            "boq-location-${location.id}-product-id[]" to listOf(
                "${products[0].id}",
                "${products[1].id}",
                "${products[2].id}",
            ),
            "boq-location-${location.id}-qty-in-store[]" to listOf(
                "0",
                "100",
                "1000",
            ),
            "boq-location-${location.id}-qty-to-install[]" to listOf(
                "5",
                "50",
                "0",
            )
        )
        val form = ProjectEditForm(testParams, project, listOf(location), products)

        assert(!form.isValid().first)
        assertEquals(form.isValid().second, "All quantities listed must be greater than 0.")
    }
}