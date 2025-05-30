package by.antohakon.telegrambotbyantoxa;

public class TelegramUser {

    private Long chatId;
    private String firstName;
    private String lastName;

    public TelegramUser() {
    }

    public TelegramUser(Long id, String firstName, String lastName) {
        this.chatId = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "TelegramUser{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}
