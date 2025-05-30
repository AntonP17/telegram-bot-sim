package by.antohakon.telegrambotbyantoxa;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private Map<Long, Boolean> inSurvey = new HashMap<>();
    private Map<Long, String> userResponses = new HashMap<>();
    private UserData userData = new UserData();

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient(
                "7769264203:AAFO_3ugX1grxCmqo3SBte43hmrHcDSfmOA"
        );
    }

    //вводим данные из телеграм бота в чате
    @Override
    public void consume(Update update) {
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // 1. Сначала проверяем /start (чтобы сбросить состояние)
            if (messageText.equals("/start")) {

                System.out.println("пользователь ввел /start");

                inSurvey.remove(chatId);     // Сброс состояния опроса
                userResponses.remove(chatId); // Очистка предыдущих ответов
                sendMainMenu(chatId);
                return; // Важно: завершаем обработку
            }

            // 2. Затем проверяем, в процессе ли опроса
            if (inSurvey.getOrDefault(chatId, false)) {

                System.out.println("проверка в опросе или нет");

                handleSurveyResponse(chatId, messageText);
                return;
            }

            // 3. Проверяем, завершён ли опрос ранее
            if (userResponses.containsKey(chatId)) {

                System.out.println("проверка завершен опрос или нет");

                sendMessage(chatId, "❌ Вы уже проходили опрос! Напишите /start для сброса.");
                return;
            }

            // 4. Обработка остальных команд
            switch (messageText) {
                case "/keyboard" -> sendReplyKeyboard(chatId);
                case "Привет" -> sendMyName(chatId, update.getMessage().getFrom());
                case "Картинка" -> sendImage(chatId);
                case "опрос" -> startSurvey(chatId);
                default -> sendMessage(chatId, "Я вас не понимаю");
            }
        } else if (update.hasCallbackQuery()) {
            hanndleCallbackQuerry(update.getCallbackQuery());
        }
    }

    // начинаем опрос и устанавливаем статус в опросе
    private void startSurvey(Long chatId) {

        System.out.println("устанавливаем статус что пользователь в опросе и начинаем спрашивать имя");

        inSurvey.put(chatId, true);
        sendMessage(chatId, "Введите ваше имя:");

        System.out.println("спросили имя ");
    }

    // сам опрос
    private void handleSurveyResponse(Long chatId, String messageText) {
        // Простейшая реализация опроса

      //  UserData userData = new UserData();

        if (userResponses.get(chatId) == null) {
            // Первый ответ - имя

            System.out.println("имя получено " + messageText);

            userResponses.put(chatId, messageText);
            userData.setName(messageText);
            sendMessage(chatId, "Теперь введите ваш email:");

            System.out.println("получаем мыло и проверяем валидность ");

        } else if (!userResponses.get(chatId).contains("@")) {
            // Второй ответ - email
            if (isValidEmail(messageText)) {

                System.out.println("email прошел проверку " + messageText);
                userData.setEmail(messageText);
                userResponses.put(chatId, userResponses.get(chatId) + "|" + messageText);
                sendMessage(chatId, "Теперь введите оценку (1-10):");

                System.out.println("все норм , спрашиваем оценку ");

            } else {

                System.out.println("email НЕ прошел проверку " + messageText);

                sendMessage(chatId, "Пожалуйста, введите корректный email:");
            }
        } else {
            // Третий ответ - оценка

            System.out.println("оценка от пользователя " + messageText);
            userData.setNumber(Integer.parseInt(messageText));
            userResponses.put(chatId, messageText);
            sendMessage(chatId, "Спасибо за опрос!");
            inSurvey.put(chatId, false);
            sendMessage(chatId, "✅ Опрос завершён! Результаты: ...");
            System.out.println(userData);
            sendImageInSurvery(chatId);



            sendMessage(chatId,userData.toString());

//            System.out.println(userResponses);
//            for (Map.Entry<Long, String> entry : userResponses.entrySet()) {
//                System.out.println("Ключ: " + entry.getKey() + ", Значение: " + entry.getValue());
//            }
        }
    }

    // Метод для валидации email
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email.matches(emailRegex);
    }



    private void sendReplyKeyboard(Long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text("Это обычная клавиатура")
                .build();

        List<KeyboardRow> keyboardRows = List.of(
                new KeyboardRow("Привет", "Картинка")
        );

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void hanndleCallbackQuerry(CallbackQuery callbackQuery) {
        var data = callbackQuery.getData();
        var chatId = callbackQuery.getFrom().getId();
        var user = callbackQuery.getFrom();
        switch (data){
            case "my_name" -> sendMyName(chatId, user);
            case "random"  -> sendRandom(chatId);
            case "long_process" -> sendImage(chatId);
            case "inSurvey" -> startSurvey(chatId);
            default -> sendMessage(chatId, "Неизвестная команда");
        }
    }

    private void sendMessage(Long chatId, String messageText) {
        SendMessage message = SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void sendImage(Long chatId) {
        sendMessage(chatId,"Присылаю картинкy");
        new Thread(() -> {
            var urlYandex = "https://get.wallhere.com/photo/mountains-night-nature-snow-winter-mountain-mountainous-landforms-landform-geological-phenomenon-mountain-range-21621.png";
            var imageUrl = "https://picsum.photos/200";
            try {
                URL url = new URL(urlYandex);
                var inputStream = url.openStream();

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(inputStream, "random.jpg"))
                        .caption("Ваша картинка")
                        .build();

                telegramClient.execute(sendPhoto);
            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void sendImageInSurvery(Long chatId) {

        new Thread(() -> {
            var kiskaPhoto = "https://i.pinimg.com/736x/fe/a4/bb/fea4bbb5947049fbb0909d2f5d04ff45.jpg";
            var dogPhoto = "https://wallpapers.com/images/hd/black-and-white-dog-with-brown-eyes-cck4f4jv36dpb09a.jpg";
            var imageUrl = "https://picsum.photos/200";

            try {
                URL url = null;
                 if (chatId == 653808959){
                     url = new URL(kiskaPhoto);
                 } else {
                     url = new URL(dogPhoto);
                 }
                var inputStream = url.openStream();

                SendPhoto sendPhoto = SendPhoto.builder()
                        .chatId(chatId)
                        .photo(new InputFile(inputStream, "random.jpg"))
                        .build();

                telegramClient.execute(sendPhoto);
            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void sendRandom(Long chatId) {
        var randomInt = ThreadLocalRandom.current().nextInt();
        sendMessage(chatId,"Генеррую число " + randomInt);
    }

    // ВОТ ТУТ СОХРАНЕНИЕ В БД МОЖНО И ВОЗВРАТ ДАННХ ЮЗЕРУ
    private void sendMyName(Long chatId, User user) {

        System.out.println("Пользователь: " + user.getFirstName() + " " + user.getLastName()
                + " chatId " + chatId);
        System.out.println(user.toString());

        TelegramUser ourUser = new TelegramUser();
        ourUser.setFirstName(user.getFirstName());
        ourUser.setLastName(user.getLastName());
        ourUser.setChatId(chatId);
        System.out.println(ourUser.toString());

        var text = "Привет!\n\nВас зовут %s\nВаш ник: @%s"
                .formatted(
                        user.getFirstName() + " " + user.getLastName(),
                        user.getUserName()
                );
        sendMessage(chatId,text);


    }

    private void sendMainMenu(Long chatId) {

        SendMessage message = SendMessage.builder()
                .text("Добро пожаловать ! Выберите действие:")
                .chatId(chatId)
                .build();

        var button1 = InlineKeyboardButton.builder()
                .text("Как меня зовут?")
                .callbackData("my_name")
                .build();

        var button2 = InlineKeyboardButton.builder()
                .text("Случайное число")
                .callbackData("random")
                .build();

        var button3 = InlineKeyboardButton.builder()
                .text("Долгий процесс")
                .callbackData("long_process")
                .build();

        var button4 = InlineKeyboardButton.builder()
                .text("Опрос")
                .callbackData("inSurvey")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(button1),
                new InlineKeyboardRow(button2),
                new InlineKeyboardRow(button3),
                new InlineKeyboardRow(button4)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);

        message.setReplyMarkup(markup);

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

    }
}
