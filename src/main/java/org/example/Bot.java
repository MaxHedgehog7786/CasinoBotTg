package org.example;


import okhttp3.*;
import org.example.data.BotData;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Bot extends TelegramLongPollingBot {

    private static final String TOKEN = BotData.TOKEN;

    private final ExecutorService executorService;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    KeyboardButton spin = KeyboardButton.builder()
            .text("Крутить")
            .build();

    KeyboardButton stat = KeyboardButton.builder()
            .text("Таблица рекордов")
            .build();


    ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(spin, stat)))
            .resizeKeyboard(true)
            .build();

    public Bot() {
        super(TOKEN);
        this.executorService = Executors.newFixedThreadPool(200);
    }

    @Override
    public void onUpdateReceived(Update update) {
        executorService.execute(() -> {
            Message message = update.getMessage();
            Long who = message.getFrom().getId();
            String username = message.getFrom().getUserName();
            String what = message.getText();
            if (message.isCommand()){
                if (what.equals("/start")) {
                    try {
                        addUser(username);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    onStart(who);
                }
            } else if (what.equals("Крутить")){
                System.out.print(username);
                spinRoulette(who, username);
            } else if (what.equals("Таблица рекордов")) {
                try {
                    sendMessage(who, getScoreTable(username));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }


    public void addUser(String username) throws IOException {
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .build();
        Request request = new Request.Builder()
                .url("http://localhost:8080/api/users/addUser")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).execute();
    }

    public String getScoreTable(String username) throws IOException {
            Request request = new Request.Builder()
                    .get()
                    .url("http://localhost:8080/api/users/findAll?username=" + username)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            assert response.body() != null;
            return response.body().string();
    }

    @Override
    public String getBotUsername() {
        return "MIREA Casino Test";
    }

    public void onStart(Long who){
        SendMessage message = SendMessage.builder()
                .text("Добро пожаловать в первое казино РТУ МИРЭА. Крути слоты и выигрывай баллы для БРСки!")
                .chatId(who)
                .replyMarkup(keyboard)
                .build();

        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void spinRoulette(Long who, String username){
        executorService.execute(() -> {
            ReplyKeyboardRemove remove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
            SendDice sticker = SendDice.builder()
                    .chatId(who)
                    .emoji("\uD83C\uDFB0")
                    .replyMarkup(remove)
                    .build();
            try {
                Message resultMessage = execute(sticker);
                int value = resultMessage.getDice().getValue();
                String message = switch (value) {
                    case 1 -> "Поздравляем! Коэфициент вашей победы: 1";
                    case 22 -> "Поздравляем! Коэфициент вашей победы: 2";
                    case 43 -> "Поздравляем! Коэфициент вашей победы: 3";
                    case 64 -> "Поздравляем! Коэфициент вашей победы: 4";
                    default -> "Повезет в следующий раз! Додеп и крути!";
                };
                System.out.println(" " + value + " " +message);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                addScore(username, value);
                sendMessage(who, message);

            } catch (TelegramApiException | IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void addScore(String username, Integer score) throws IOException {
        executorService.execute(() -> {
            RequestBody requestBody = new FormBody.Builder()
                    .add("username", username)
                    .add("score", String.valueOf(score))
                    .build();
            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/users/addScore")
                    .put(requestBody)
                    .build();
            try {
                okHttpClient.newCall(request).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sendMessage(Long who, String what){
        executorService.execute(() -> {
            SendMessage message = SendMessage.builder()
                    .text(what)
                    .chatId(who)
                    .replyMarkup(keyboard)
                    .build();

            try {
                execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
