package jakarta.tutorial.interceptor.ejb;

public class HelloForm {

    @Lowercase
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
