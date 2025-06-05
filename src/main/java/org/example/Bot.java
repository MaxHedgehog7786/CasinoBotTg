package org.example;

import org.example.Data.TokenClass;
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

import java.util.List;

public class Bot extends TelegramLongPollingBot {

    private static final String TOKEN = TokenClass.TOKEN;


    KeyboardButton spin = KeyboardButton.builder()
            .text("Крутить")
            .build();



    ReplyKeyboardMarkup keyboard = ReplyKeyboardMarkup.builder()
            .keyboardRow(new KeyboardRow(List.of(spin)))
            .resizeKeyboard(true)
            .build();

    public Bot() {
        super(TOKEN);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        Long who = message.getFrom().getId();
        String what = message.getText();
        if (message.isCommand()){
            if (what.equals("/start")) {
                onStart(who);
            }
        } else if (what.equals("Крутить")){
            System.out.print(message.getFrom().getUserName());
            spinRoulette(who);
        }
    }

    @Override
    public String getBotUsername() {
        return "MIREA Casino";
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

    public void spinRoulette(Long who){
        ReplyKeyboardRemove remove = ReplyKeyboardRemove.builder().removeKeyboard(true).build();
        SendDice sticker = SendDice.builder()
                .chatId(who)
                .emoji("\uD83C\uDFB0")
                .replyMarkup(remove)
                .build();
        try {
            Message resultMessage = execute(sticker);
            int value = resultMessage.getDice().getValue();
            String message;
            switch (value){
                case 1:
                    message = "Поздравляем! Коэфициент вашей победы: 1";
                    break;
                case 22:
                    message = "Поздравляем! Коэфициент вашей победы: 2";
                    break;
                case 43:
                    message = "Поздравляем! Коэфициент вашей победы: 3";
                    break;
                case 64:
                    message = "Поздравляем! Коэфициент вашей победы: 4";
                    break;
                default:
                    message = "Повезет в следующий раз! Додеп и крути!";
            }
            System.out.println(" " + value + " " +message);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sendMessage(who, message);

        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long who, String what){
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
    }

}
