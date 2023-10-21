package org.igorgrab;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Bot-Klasse ist die Hauptklasse für die Handhabung des Telegram-Bots. Es verwaltet die Verarbeitung von Nachrichten, Inline-Anfragen und Rückrufen.
 */
public class Bot {
    private final TelegramBot bot = new TelegramBot(("6676118945:AAESQcS77VoevXBWX0_4YMBWQZq78425Eyw"));
    private final String PROCESSING_LABEL = "...";

    private final static List<String> opponentWins = new ArrayList<>() {{ //Liste die Kombinationen, bei denen der Gegner gewinnt
        add("10");
        add("21");
        add("02");
    }};
    private final static Map<String, String> items = new HashMap<>() {{ // Mapping zwischen Zahlen (Benutzerauswahl) und Emoji
        put("0", "👊");
        put("1", "✌\uFE0F");
        put("2", "\uD83E\uDD1A");
    }};

    /**
     * Die Methode „serve()“ beginnt mit der Überwachung auf Aktualisierungen und der Verarbeitung durch den Bot.
     */
    public void serve() {
        bot.setUpdatesListener(updates -> { // Updatelistner bekommt jede 100 milis ein update von Telegram API
            updates.forEach(this::process); // jede update verfahrt die methode "process"
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    /**
     * Die Methode „process()“ verarbeitet Bot-Updates.
     *
     * @param update Telegramm-Update-Objekt
     */
    private void process(Update update) {
        Message message = update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if (message != null && message.viaBot() != null && message.viaBot().username().equals("schnikGame_bot")) {
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if (replyMarkup == null) {
                return;
            }

            InlineKeyboardButton[][] buttons = replyMarkup.inlineKeyboard();

            if (buttons == null) {
                return;
            }

            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();

            if (!buttonLabel.equals(PROCESSING_LABEL)) {
                return;
            }

            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text()) // Bereiten eine Antwort auf eine Nachricht mit Auswahlschaltflächen
                    .replyMarkup(
                            new InlineKeyboardMarkup(
                                    new InlineKeyboardButton("👊")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "0", messageId)),
                                    new InlineKeyboardButton("✌\uFE0F")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "1", messageId)),
                                    new InlineKeyboardButton("\uD83E\uDD1A")
                                            .callbackData(String.format("%d %s %s %s %d", chatId, senderName, senderChose, "2", messageId))
                            )
                    );
        } else if (inlineQuery != null) {  // Bereiten eine Antwort auf die Inline-Anfrage
            InlineQueryResultArticle rock = buildInlineButton("rock", "👊 Rock", "0");
            InlineQueryResultArticle scissors = buildInlineButton("scissors", "✌\uFE0F Scissor", "1");
            InlineQueryResultArticle paper = buildInlineButton("paper", "\uD83E\uDD1A Paper", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), rock, scissors, paper).cacheTime(1);
        } else if (callbackQuery != null) {  // Verarbeiten rückruf vom nutzer
            String[] data = callbackQuery.data().split(" ");
            if (data.length < 4) {
                return;
            }

            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            String opponentName = callbackQuery.from().firstName()+ " Timka";

            if (senderChose.equals(opponentChose)) {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "%s and %s chose %s : Friendship won",
                                senderName,
                                opponentName,
                                items.get(senderChose)
                        ));
            } else if (opponentWins.contains(senderChose + opponentChose)) {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "%s chose %s and %s chose %s : %s win",
                                senderName, items.get(senderChose),
                                opponentName, items.get(opponentChose), opponentName
                        ));
            } else {
                request = new SendMessage(chatId,
                        String.format(
                                "%s chose %s and %s chose %s : %s win",
                                senderName, items.get(senderChose),
                                opponentName, items.get(opponentChose), senderName
                        ));
            }
        }

        if (request != null) { // Anfrage zu dem Telegram-API
            bot.execute(request);
        }
    }

    /**
     * Die Methode buildInlineButton() erstellt ein InlineQueryResultArticle-Objekt für die Inline-Schaltfläche.
     *
     * @param id           Eindeutige Kennung der Schaltfläche
     * @param title        Titel des Buttons
     * @param callbackData Daten zur Verarbeitung des Schaltflächenklicks
     * @return InlineQueryResultArticle-Objekt
     */
    private InlineQueryResultArticle buildInlineButton(String id, String title, String callbackData) {
        return new InlineQueryResultArticle(id, title, "I Want to Play A Game! :)")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callbackData)
                        )
                );
    }
}