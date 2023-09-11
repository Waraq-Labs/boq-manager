package com.waraqlabs.boq_manager.plugins

import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.mustache.*
import io.ktor.util.*
import java.io.File
import java.io.StringWriter

val NoCacheMustache: ApplicationPlugin<MustacheConfig> = createApplicationPlugin("NoCacheMustache", ::MustacheConfig) {
    @OptIn(InternalAPI::class)
    on(BeforeResponseTransform(MustacheContent::class)) { _, content ->
        val mustacheFactory = DefaultMustacheFactory(File("src/main/resources/templates/"))

        with(content) {
            val writer = StringWriter()
            mustacheFactory.compile(content.template).execute(writer, model)

            val result = TextContent(text = writer.toString(), contentType)
            if (etag != null) {
                result.versions += EntityTagVersion(etag!!)
            }
            result
        }
    }
}

fun Application.configureTemplating() {
    // TODO: Install cache/no-cache version based on config/environment
    install(NoCacheMustache)
}
