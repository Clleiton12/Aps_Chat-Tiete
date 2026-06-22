package io.unip_aps.tiete.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    public interface LoginCallback {
        void onLoginSuccess(String username, ChatClient client);
    }

    private static final Color GREEN_DARK  = new Color(27, 94, 32);
    private static final Color GREEN_MED   = new Color(56, 142, 60);
    private static final Color GREEN_LIGHT = new Color(200, 230, 201);
    private static final Color BLUE_RIVER  = new Color(2, 119, 189);

    private final ChatClient client;
    private final LoginCallback callback;

    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn;
    private JButton registerBtn;
    private JLabel statusLabel;

    public LoginFrame(LoginCallback callback) {
        this.callback = callback;
        this.client = new ChatClient();
        initUI();
        client.setListener(new ChatClient.MessageListener() {
            @Override public void onMessage(String msg) {
                SwingUtilities.invokeLater(() -> handleServerMessage(msg));
            }
            @Override public void onDisconnect() {
                SwingUtilities.invokeLater(() -> setStatus("Desconectado do servidor.", Color.RED));
            }
        });
    }

    private void initUI() {
        setTitle("Rio Tietê — Monitoramento Ambiental");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildForm(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        add(root);
        setSize(480, 400);
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN_DARK);
        header.setBorder(new EmptyBorder(22, 30, 0, 30));

        JLabel titulo = new JLabel("RIO TIETÊ");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Sistema de Monitoramento Ambiental");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sub.setForeground(new Color(165, 214, 167));

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 4));
        texts.setBackground(GREEN_DARK);
        texts.add(titulo);
        texts.add(sub);
        header.add(texts, BorderLayout.CENTER);

        JPanel stripe = new JPanel();
        stripe.setBackground(BLUE_RIVER);
        stripe.setPreferredSize(new Dimension(0, 5));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(GREEN_DARK);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(stripe, BorderLayout.SOUTH);
        wrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        return wrapper;
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(24, 50, 10, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 4, 5, 4);

        // Linha servidor / porta
        g.gridx = 0; g.gridy = 0; g.weightx = 0.2;
        panel.add(lbl("Servidor:"), g);
        g.gridx = 1; g.weightx = 0.55;
        hostField = field("localhost");
        panel.add(hostField, g);
        g.gridx = 2; g.weightx = 0.1;
        panel.add(lbl("Porta:"), g);
        g.gridx = 3; g.weightx = 0.15;
        portField = field("12345");
        panel.add(portField, g);

        // Separador
        g.gridx = 0; g.gridy = 1; g.gridwidth = 4;
        JSeparator sep = new JSeparator();
        sep.setForeground(GREEN_LIGHT);
        panel.add(sep, g);

        // Usuário
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 2; g.weightx = 0.2;
        panel.add(lbl("Usuário:"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 0.8;
        userField = field("");
        panel.add(userField, g);

        // Senha
        g.gridwidth = 1;
        g.gridx = 0; g.gridy = 3; g.weightx = 0.2;
        panel.add(lbl("Senha:"), g);
        g.gridx = 1; g.gridwidth = 3; g.weightx = 0.8;
        passField = new JPasswordField();
        styleField(passField);
        panel.add(passField, g);

        // Botões
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2; g.weightx = 0.5;
        loginBtn = btn("Entrar", GREEN_DARK);
        panel.add(loginBtn, g);
        g.gridx = 2; g.gridwidth = 2; g.weightx = 0.5;
        registerBtn = btn("Cadastrar", BLUE_RIVER);
        panel.add(registerBtn, g);

        // Status
        g.gridx = 0; g.gridy = 5; g.gridwidth = 4;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        panel.add(statusLabel, g);

        loginBtn.addActionListener(e -> doLogin());
        registerBtn.addActionListener(e -> doRegister());
        passField.addActionListener(e -> doLogin());

        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(GREEN_LIGHT);
        footer.setBorder(new EmptyBorder(8, 0, 8, 0));
        JLabel label = new JLabel("Desenvolvido por Alunos do UNIP - APS 2026");
        label.setFont(new Font("SansSerif", Font.PLAIN, 11));
        label.setForeground(GREEN_DARK);
        footer.add(label);
        return footer;
    }

    private void doLogin() {
        if (!connectIfNeeded()) return;
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isBlank() || pass.isBlank()) { setStatus("Preencha usuário e senha.", Color.RED); return; }
        setStatus("Aguardando servidor...", Color.GRAY);
        loginBtn.setEnabled(false);
        registerBtn.setEnabled(false);
        client.login(user, pass);
    }

    private void doRegister() {
        if (!connectIfNeeded()) return;
        String user = userField.getText().trim();
        String pass = new String(passField.getPassword());
        if (user.isBlank() || pass.isBlank()) { setStatus("Preencha usuário e senha.", Color.RED); return; }
        client.register(user, pass);
    }

    private boolean connectIfNeeded() {
        if (client.isConnected()) return true;
        String host = hostField.getText().trim();
        int port;
        try { port = Integer.parseInt(portField.getText().trim()); }
        catch (NumberFormatException e) { setStatus("Porta inválida.", Color.RED); return false; }
        if (!client.connect(host, port)) {
            setStatus("Não foi possível conectar ao servidor.", Color.RED);
            return false;
        }
        return true;
    }

    private void handleServerMessage(String message) {
        int pipe = message.indexOf('|');
        String type = pipe == -1 ? message : message.substring(0, pipe);
        String data = pipe == -1 ? "" : message.substring(pipe + 1);

        switch (type) {
            case "LOGIN_OK" -> {
                setStatus("Login realizado!", GREEN_DARK);
                dispose();
                callback.onLoginSuccess(data, client);
            }
            case "LOGIN_FAIL" -> {
                setStatus(data, Color.RED);
                loginBtn.setEnabled(true);
                registerBtn.setEnabled(true);
            }
            case "REGISTER_OK"   -> { setStatus("Cadastro realizado! Faça login.", GREEN_MED); loginBtn.setEnabled(true); registerBtn.setEnabled(true); }
            case "REGISTER_FAIL" -> { setStatus(data, Color.RED); loginBtn.setEnabled(true); registerBtn.setEnabled(true); }
        }
    }

    private void setStatus(String msg, Color color) {
        statusLabel.setText(msg);
        statusLabel.setForeground(color);
    }

    private JLabel lbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField(placeholder);
        styleField(f);
        return f;
    }

    private void styleField(JComponent f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 189, 189)),
            new EmptyBorder(5, 8, 5, 8)
        ));
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }
}