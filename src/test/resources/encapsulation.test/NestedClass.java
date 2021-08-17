class MainClass {

    class NestedClass {
        public String getName() {
            return "Nested";
        }
    }

    public NestedClass newNestedClass() {
        return new NestedClass();
    }
    public String getMainName() {
        return "Main";
    }

}