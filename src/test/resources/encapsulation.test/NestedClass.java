/**
 * Main Class
 */
class MainClass {

    /**
     * Nested Class
     */
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