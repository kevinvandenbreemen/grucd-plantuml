package kotlin

class ClassWithANestedClass {

    class NestedClass {

        val someValue: String = ""

    }

    fun makeMyNested(): NestedClass {
        return NestedClass()
    }

}