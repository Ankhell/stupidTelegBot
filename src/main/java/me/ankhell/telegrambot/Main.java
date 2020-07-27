package me.ankhell.telegrambot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {

    private static final String PROXY_HOST = "113.196.140.162" /* proxy host */;
    private static final Integer PROXY_PORT = 8888 /* proxy port */;
    private static final boolean PROXY_ENABLED = false;

    public static void main(String[] args) {
        try {

            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Set up Http proxy
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            if (PROXY_ENABLED) {
                botOptions.setProxyHost(PROXY_HOST);
                botOptions.setProxyPort(PROXY_PORT);
                // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
                botOptions.setProxyType(DefaultBotOptions.ProxyType.HTTP);
            }
            // Register your newly created AbilityBot
            Bot bot = new Bot(botOptions);

            botsApi.registerBot(bot);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}