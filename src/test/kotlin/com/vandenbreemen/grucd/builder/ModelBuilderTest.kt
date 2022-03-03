package com.vandenbreemen.grucd.builder

import com.vandenbreemen.grucd.model.Type
import com.vandenbreemen.grucd.parse.ParseJava
import com.vandenbreemen.grucd.parse.ParseKotlin
import org.amshove.kluent.fail
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.jupiter.api.Test

internal class ModelBuilderTest {

    @Test
    fun `should detect use of java by kotlin`() {

        val kotlinTypes = ParseKotlin().parse("src/test/resources/kotlin/to/java/KotlinClass.kt")
        val javaTypes = ParseJava().parse("src/test/resources/kotlin/to/java/JavaClass.java")

        val allTypes = mutableListOf<Type>()
        allTypes.run {
            addAll(kotlinTypes)
            addAll(javaTypes)
        }

        val model = ModelBuilder().build(allTypes)

        model.types.firstOrNull { t->t.name == "KotlinClass" }?.let { kotlin->
            kotlin.fields.shouldNotBeEmpty()
            kotlin.fields[0].typeName shouldBeEqualTo "JavaClass"
        } ?: fail("Kotlin class uses java type JavaClass")

        model.unusedTypes.firstOrNull { t->t.name == "JavaClass" }?.let {
            fail("Found java class as un-used when it is accessed by Kotlin")
        }

    }

}