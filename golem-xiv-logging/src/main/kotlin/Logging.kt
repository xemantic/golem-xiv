package com.xemantic.ai.golem.logging

import org.slf4j.bridge.SLF4JBridgeHandler


/**
 * Initializes Java Util Logging to slf4j bridge.
 * This function should be called once, as early as possible during application startup.
 *
 * Note: Neo4j is using JUL internally.
 */
fun initializeLogging() {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
}
