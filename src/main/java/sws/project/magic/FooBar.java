package sws.project.magic;

/**
 *
 */
public class FooBar {
    @Editable(SimpleStringFormGenerator.class)
    private String foo;

    @Editable(SimpleStringFormGenerator.class)
    private String bar;

    private String james;

    public String getFoo() {
        return foo;
    }

    public void setFoo(String foo) {
        this.foo = foo;
    }

    public String getBar() {
        return bar;
    }

    public void setBar(String bar) {
        this.bar = bar;
    }

    public String getJames() {
        return james;
    }

    public void setJames(String james) {
        this.james = james;
    }
}
