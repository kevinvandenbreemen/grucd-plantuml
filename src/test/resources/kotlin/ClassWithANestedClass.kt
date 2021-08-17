package kotlin

class ClassWithANestedClass {

    class NestedClass {

        val someValue: String = ""

        fun test(){}
        fun test2(){}

    }

    fun makeMyNested(): NestedClass {
        return NestedClass()
    }

}