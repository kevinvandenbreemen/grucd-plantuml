package kotlin

/**
 * Parent class
 */
class ClassWithANestedClass {

    /**
     * Nested class
     */
    class NestedClass {

        val someValue: String = ""

        fun test(){}
        fun test2(){}

    }

    fun makeMyNested(): NestedClass {
        return NestedClass()
    }

}