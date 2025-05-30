package by.antohakon.telegrambotbyantoxa;

public class UserData {

    private String name;
    private String email;
    private int number;

    public UserData() {
    }

    public UserData(String name, String email, int number) {
        this.name = name;
        this.email = email;
        this.number = number;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Имя : ").append(getName()).append(" ,\n")
                .append("email : ").append(getEmail()).append(",\n")
                .append("number : ").append(getNumber())
                .toString();
    }
}
