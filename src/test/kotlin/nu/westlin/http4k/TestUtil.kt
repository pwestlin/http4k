package nu.westlin.http4k

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

// TODO petves: Should only exist for tests?
val objectMapper = ObjectMapper().registerKotlinModule()

