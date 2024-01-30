package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WelcomeBot extends TelegramLongPollingBot {

    private final Map<Long, Deal> deals = new HashMap<>();

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getFrom().getUserName();

            switch (message) {
                case "/start_deal":
                    startDeal(chatId);
                    break;
                case "/join":
                    joinDeal(chatId, update.getMessage().getFrom().getUserName());
                    break;
                case "/agree":
                    agreeDeal(chatId, update.getMessage().getFrom().getUserName());
                    break;
                case "/set_arbitrator":
                    setArbitrator(chatId, update.getMessage().getFrom().getUserName());
                    break;
                case "/decline":
                    declineDeal(chatId, userName);
                    break;
                default:
                    sendMessage(chatId, "Неизвестная команда. Используйте /start_deal, /join, /agree.");
            }
        }
    }

    private void setArbitrator(long chatId, String userName) {
        Deal deal = deals.get(chatId);

        if (deal == null) {
            sendMessage(chatId, "Сначала начните сделку командой /start_deal.");
            return;
        }

        if (deal.getArbitrator() == null) {
            deal.setArbitrator(userName);
            sendMessage(chatId, userName + " назначен арбитром сделки.");
        } else {
            sendMessage(chatId, "Арбитр сделки уже назначен.");
        }
    }

    private void startDeal(long chatId) {
        deals.put(chatId, new Deal());
        sendMessage(chatId, "Сделка начата! Первый участник, пожалуйста, отправьте /join");
    }

    private void joinDeal(long chatId, String userName) {
        Deal deal = deals.get(chatId);
        if (deal == null) {
            sendMessage(chatId, "Сначала начните сделку командой /start_deal");
            return;
        }

        if (deal.getParticipant1() == null) {
            deal.setParticipant1(userName);
            sendMessage(chatId, userName + " теперь участник 1. Второй участник, отправьте /join");
        } else if (deal.getParticipant2() == null) {
            deal.setParticipant2(userName);
            sendMessage(chatId, userName + " теперь участник 2. Теперь участники могут согласиться на сделку отправкой /agree");
        } else {
            sendMessage(chatId, "Уже есть два участника в сделке.");
        }
    }
    private void declineDeal(long chatId, String userName) {
        Deal deal = deals.get(chatId);
        if (deal == null) {
            sendMessage(chatId, "Сначала начните сделку командой /start_deal.");
            return;
        }

        if (userName.equals(deal.getArbitrator())) {
            deals.remove(chatId);
            sendMessage(chatId, "Арбитр отказал в сделке. Сделка отменена.");
        } else {
            sendMessage(chatId, "Только арбитр может отказать от сделки.");
        }
    }

    private void agreeDeal(long chatId, String userName) {
        Deal deal = deals.get(chatId);
        if (deal == null && deal.getParticipant2() == null){
            sendMessage(chatId, "Сделка еще не готова к согласованию.");
            return;
        }

        if (userName.equals(deal.getParticipant1()) && userName.equals(deal.getParticipant2())){
            deal.setAgreed(true);
            sendMessage(chatId, "Оба участника согласны на сделку!");
        } else{
            sendMessage(chatId, "Вы не являетесь участником сделки.");
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        // Создаем строки кнопок
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строка кнопок
        KeyboardRow row = new KeyboardRow();
        // Добавляем кнопки в строку
        row.add(new KeyboardButton("/start_deal"));
        row.add(new KeyboardButton("/join"));
        // Добавляем первую строку кнопок в клавиатуру
        keyboard.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton("/agree"));
        row.add(new KeyboardButton("/set_arbitrator"));
        keyboard.add(row);

        row = new KeyboardRow();
        row.add(new KeyboardButton("/decline"));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "UsernameBot";
    }

    @Override
    public String getBotToken() {
        return "YourBotToken";
    }
}