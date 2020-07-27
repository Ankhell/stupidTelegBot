package me.ankhell.telegrambot;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Bot extends TelegramLongPollingBot {

    public static final String BOT_NAME = "";
    public static final String BOT_TOKEN = "";

    Map<Integer, String> names = new HashMap<>();

    protected Bot(DefaultBotOptions options) {
        super(options);
    }

    /**
     * Метод для приема сообщений.
     *
     * @param update Содержит сообщение от пользователя.
     */
    @Override
    public void onUpdateReceived(Update update) {
        Integer senderID = update.getMessage().getFrom().getId();
//        if (!names.containsKey(senderID)) {
//            names.put(senderID, update.getMessage().getFrom().getFirstName());
//        }

        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setFromChatId(update.getMessage().getChatId());
        forwardMessage.setChatId("");
        forwardMessage.setMessageId(update.getMessage().getMessageId());
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        String messageText = update.getMessage().getText().toLowerCase();
        Pattern pattern = Pattern.compile("как мне", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(messageText);
        if (messageText.toLowerCase().matches(".*х.*у.*й.*")) {
            messageText = ("<s>" + messageText + "</s>").replace("х", "</s><b>X</b><s>")
                                                   .replace("у", "</s><b>У</b><s>")
                                                   .replace("й", "</s><b>Й</b><s>");
            sendString(update,
                       "Нет, ну вы видели? " + update.getMessage()
                                                     .getFrom()
                                                     .getFirstName() + " сказал слово хуй! " + messageText,
                       true
            );
        }
        if (matcher.find()) {
            String queryToGoogle = messageText
                    .substring(matcher.end())
                    .replaceAll("[?!.]", "")
                    .trim()
                    .replaceAll("\\s", "%20");
            System.out.println(queryToGoogle);
            try {
                List<String> links = Jsoup.connect("https://www.google.com.au/search?q=" + queryToGoogle)
                                          .get()
                                          .select("div[class=\"r\"]")
                                          .select("a")
                                          .stream()
                                          .map(element -> element.attr("href"))
                                          .filter(s -> !s.equals("#"))
                                          .filter(s -> s.startsWith("http"))
                                          .filter(s -> !s.contains("translate"))
                                          .filter(s -> !s.contains("webcache.google"))
                                          .collect(Collectors.toList());
                Random random = new Random();
                String randomLink = links.get(random.nextInt(links.size() - 1));
                if (names.containsKey(senderID)) {
                    randomLink = "Вот так, " + names.get(senderID) + ": " + randomLink;
                }
                sendString(update, randomLink, true);
                boolean tRue = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (messageText.contains("/setname")) {
            String name = update.getMessage().getText().replace("/setname", "").trim();
            names.put(senderID, name);
        }
        if (messageText.contains("/myname")) {
            if (!names.containsKey(senderID)) {
                sendString(update, "Я ещё не знаю твоего имени", true);
            } else {
                sendString(update, "Твоё имя " + names.get(senderID), true);
            }
        }
        if (messageText.contains("/help") || update.getMessage().getText().contains("/start")) {
            sendString(update, "<b>Доступные команды:</b>\n /setname [Имя] - " +
                    "позволяет боту запомнить Ваше имя\n /myname - бот назовёт Вас по имени", false);

        }
/*        if (update.hasMessage() && update.getMessage().hasText()&&update.getMessage().getText().contains("@xuificatorBot")) {
            String messageToSend = Arrays.stream(update.getMessage().getText().replaceAll("@xuificatorBot","").trim().split("\\s++"))
                                         .map(s -> "Хуе" + s + " ")
                                         .reduce("", String::concat);
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                                                    .setChatId(update.getMessage().getChatId())
                                                    .setText(messageToSend);*/

    }

    private void sendString(Update update, String message, boolean reply) {
        SendMessage messageToSend = new SendMessage();
        messageToSend.setChatId(update.getMessage().getChatId());
        messageToSend.enableHtml(true);
        message = message.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)").replaceAll("!", "\\\\!");
        messageToSend.setText(message);

        if (reply) {
            messageToSend.setReplyToMessageId(update.getMessage().getMessageId());
        }
        sendMessage(messageToSend);
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message); // Call method to send the message
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод возвращает имя бота, указанное при регистрации.
     *
     * @return имя бота
     */
    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    /**
     * Метод возвращает token бота для связи с сервером Telegram
     *
     * @return token для бота
     */
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }
}