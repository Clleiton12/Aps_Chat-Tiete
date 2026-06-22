package io.unip_aps.tiete.client;

import javax.swing.*;

import io.unip_aps.tiete.server.ChatServer;

public class App {
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equalsIgnoreCase("server")) {
            new ChatServer().start();
            return;
        } else if (args.length > 0 && args[0].equalsIgnoreCase("user")) {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception ignored) {}

                new LoginUserFrame((username, client) ->
                        new MainUserFrame(username, client).setVisible(true)
                ).setVisible(true);
            });
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception ignored) {}

                new LoginFrame((username, client) ->
                        new MainFrame(username, client).setVisible(true)
                ).setVisible(true);
            });
        }

    }
}