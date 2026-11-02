package dev.apktwin

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNameValidationTest {
    private val pattern = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
    @Test fun acceptsNormalPackageId() { assertTrue(pattern.matches("com.example.clone")) }
    @Test fun rejectsMissingDot() { assertFalse(pattern.matches("example")) }
    @Test fun rejectsSpaces() { assertFalse(pattern.matches("com.example bad")) }
}
