package io.unip_aps.tiete.client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import io.unip_aps.tiete.util.Protocol;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame implements ChatClient.MessageListener {

    private static final Color GREEN_DARK  = new Color(27, 94, 32);
    private static final Color GREEN_MED   = new Color(56, 142, 60);
    private static final Color GREEN_LIGHT = new Color(232, 245, 233);
    private static final Color BLUE_RIVER  = new Color(2, 119, 189);
    private static final Color RED_ALERT   = new Color(183, 28, 28);

    private final ChatClient client;
    private final String username;

    // Chat
    private JTextField chatInput;
    private JPanel cardsContainer;
    private JPanel messagesContainer;
    private JScrollPane messagesScroll;
    private JLabel chatTitleLabel;
    private JPanel inputPanel;
    private String selectedDate;
    private String todayDate;
    private final Map<String, JPanel> dayCardMap = new LinkedHashMap<>();
    private final Map<String, Integer> dayCountMap = new HashMap<>();

    // Ocorrência
    private JTextField industriaField;
    private JComboBox<String> polutenteCombo;
    private JSlider nivelSlider;
    private JLabel nivelLabel;
    private JTextField localizacaoField;
    private JTextArea descricaoArea;


    // Painel
    private DefaultTableModel tableModel;
    private JButton btnAtualizar;


    public MainFrame(String username, ChatClient client) {
        this.username = username;
        this.client = client;

        this.todayDate = LocalDate.now().toString();
        this.selectedDate = this.todayDate; // Seleciona hoje por padrão

        client.setListener(this);
        initUI();

        client.requestOccurrences(this.todayDate);
        client.requestMessageDates();
    }

    private void initUI() {
        setTitle("Rio Tietê — " + username);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(950, 680);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        add(root);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                client.disconnect();
                System.exit(0);
            }
        });
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GREEN_DARK);
        header.setBorder(new EmptyBorder(10, 20, 0, 20));

        JLabel titulo = new JLabel("Sistema de Monitoramento — Rio Tietê");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        titulo.setForeground(Color.WHITE);

        JLabel userLbl = new JLabel("Inspetor: " + username + "  ");
        userLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        userLbl.setForeground(new Color(165, 214, 167));

        header.add(titulo,  BorderLayout.WEST);
        header.add(userLbl, BorderLayout.EAST);

        JPanel stripe = new JPanel();
        stripe.setBackground(BLUE_RIVER);
        stripe.setPreferredSize(new Dimension(0, 4));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(GREEN_DARK);
        wrapper.add(header, BorderLayout.CENTER);
        wrapper.add(stripe, BorderLayout.SOUTH);
        return wrapper;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("  Chat  ",             buildChatTab());
        tabs.addTab("  Nova Ocorrência  ",  buildOcorrenciaTab());
        tabs.addTab("  Painel  ",           buildPainelTab());
        return tabs;
    }

    // ── TAB CHAT ──────────────────────────────────────────────────────────────

    private JPanel buildChatTab() {
        todayDate = LocalDate.now().toString();

        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildConversasPanel(), buildChatViewPanel());
        split.setDividerLocation(220);
        split.setDividerSize(4);
        split.setResizeWeight(0);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildConversasPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 0));

        JLabel title = new JLabel("  CONVERSAS");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(Color.WHITE);
        title.setBackground(GREEN_DARK);
        title.setOpaque(true);
        title.setBorder(new EmptyBorder(12, 8, 12, 8));

        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(new Color(245, 248, 245));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(title,  BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildChatViewPanel() {

        PainelComFundo panel =
                new PainelComFundo("C:\\Users\\cleit\\OneDrive\\Documentos\\APS_Redes_2026\\chat_Aps\\logo_tiete_final.jpg");

        panel.setLayout(new BorderLayout());

        chatTitleLabel = new JLabel("  Selecione uma conversa");

        messagesContainer = new JPanel();
        messagesContainer.setLayout(new BoxLayout(messagesContainer, BoxLayout.Y_AXIS));
        messagesContainer.setOpaque(false);

        JPanel msgWrapper = new JPanel(new BorderLayout());
        msgWrapper.setOpaque(false);
        msgWrapper.add(messagesContainer, BorderLayout.NORTH);

        messagesScroll = new JScrollPane(msgWrapper);
        messagesScroll.setOpaque(false);
        messagesScroll.getViewport().setOpaque(false);

        inputPanel = buildInputRow();

        panel.add(messagesScroll, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildInputRow() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, GREEN_LIGHT),
                new EmptyBorder(8, 10, 8, 10)));

        chatInput = new JTextField();
        chatInput.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chatInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GREEN_MED),
                new EmptyBorder(6, 8, 6, 8)));
        chatInput.setEnabled(false);

        JButton enviarBtn = btn("Enviar", GREEN_DARK);
        enviarBtn.setPreferredSize(new Dimension(90, 36));
        enviarBtn.setEnabled(false);

        panel.add(chatInput, BorderLayout.CENTER);
        panel.add(enviarBtn, BorderLayout.EAST);

        enviarBtn.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());
        panel.putClientProperty("sendBtn", enviarBtn);
        return panel;
    }

    private JPanel createDayCard(String date, int count) {
        boolean isToday = date.equals(todayDate);

        JPanel card = new JPanel(new BorderLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 220)),
                new EmptyBorder(10, 12, 10, 12)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel dateLabel = new JLabel(isToday ? "● HOJE  " + formatDate(date) : formatDate(date));
        dateLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        dateLabel.setForeground(isToday ? BLUE_RIVER : GREEN_DARK);
        dateLabel.setOpaque(false);

        JLabel countLabel = new JLabel(countText(count));
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        countLabel.setForeground(Color.GRAY);
        countLabel.setOpaque(false);

        JPanel texts = new JPanel(new GridLayout(2, 1, 0, 2));
        texts.setOpaque(false);
        texts.add(dateLabel);
        texts.add(countLabel);

        card.add(texts, BorderLayout.CENTER);
        card.putClientProperty("countLabel", countLabel);
        card.putClientProperty("date", date);

        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { selectDayCard(date, card); }
            @Override public void mouseEntered(MouseEvent e) {
                if (!date.equals(selectedDate)) card.setBackground(new Color(232, 245, 233));
            }
            @Override public void mouseExited(MouseEvent e) {
                if (!date.equals(selectedDate)) card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    private void selectDayCard(String date, JPanel card) {
        if (selectedDate != null) {
            JPanel prev = dayCardMap.get(selectedDate);
            if (prev != null) {
                prev.setBackground(Color.WHITE);
                prev.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 230, 220)),
                        new EmptyBorder(10, 12, 10, 12)));
            }
        }

        selectedDate = date;
        card.setBackground(new Color(227, 242, 253));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 1, 0, BLUE_RIVER),
                new EmptyBorder(10, 9, 10, 12)));

        boolean isToday = date.equals(todayDate);
        chatTitleLabel.setText("  Chat — " + formatDate(date) + (isToday ? "  •  Ao vivo" : "  •  Histórico"));

        chatInput.setEnabled(isToday);
        JButton sendBtn = (JButton) inputPanel.getClientProperty("sendBtn");
        if (sendBtn != null) sendBtn.setEnabled(isToday);

        messagesContainer.removeAll();
        messagesContainer.revalidate();
        messagesContainer.repaint();

        client.requestMessages(date);
    }

    private void addOrUpdateDayCard(String date, int count) {
        if (dayCardMap.containsKey(date)) {
            updateDayCardCount(date, count);
        } else {
            JPanel card = createDayCard(date, count);
            dayCardMap.put(date, card);
            cardsContainer.add(card);
            cardsContainer.revalidate();
            cardsContainer.repaint();
        }
    }

    private void updateDayCardCount(String date, int count) {
        JPanel card = dayCardMap.get(date);
        if (card == null) return;
        JLabel lbl = (JLabel) card.getClientProperty("countLabel");
        if (lbl != null) lbl.setText(countText(count));
    }

    private void ensureTodayCard() {
        if (!dayCardMap.containsKey(todayDate)) addOrUpdateDayCard(todayDate, 0);
    }

    private void addMessageCard(String user, String time, String message) {
        boolean isMe = user.equals(username);

        Color bubbleColor = isMe ? new Color(0, 92, 75) : new Color(32, 44, 51);
        Color textColor = Color.WHITE;
        Color timeColor = isMe ? new Color(165, 210, 190) : new Color(170, 178, 183);
        Color nameColor = isMe ? new Color(144, 238, 144) : getUserColor(user);

        JPanel bubble = new JPanel(new GridBagLayout()){
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bubbleColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // Borda arredondada
                super.paintComponent(g);
            }
        };
        bubble.setOpaque(false);
        bubble.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);

        // [A] Nome do Inspetor
        c.gridx = 0; c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.  NORTHWEST;
        JLabel nameLbl = new JLabel(user);
        nameLbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        nameLbl.setForeground(nameColor);
        bubble.add(nameLbl, c);

        // [B] Texto da mensagem
        c.gridx = 0; c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        String msgHtml = message.replace("\n", "<br>"); // Preserva quebras de linha
        JLabel msgLbl = new JLabel("<html><body style='width: 260px; font-family: SansSerif; font-size: 11px; color: white;'>"
                + msgHtml + "</body></html>");

        bubble.add(msgLbl, c);

        // [C] Horário
        c.gridx = 1; c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.SOUTHEAST;
        c.insets = new Insets(4, 15, 0, 0);
        JLabel timeLbl = new JLabel(time);
        timeLbl.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLbl.setForeground(timeColor);
        bubble.add(timeLbl, c);

        // 4. Cria a "linha" da conversa para empurrar o balão para o lado correto

        int alinhamento = isMe ? FlowLayout.RIGHT : FlowLayout.LEFT;
        JPanel rowPanel = new JPanel(new FlowLayout(alinhamento, 15, 4)){
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        rowPanel.setOpaque(false); // Deixa o fundo transparente para mostrar a cor da conversa
        rowPanel.add(bubble);

        // 5. Adiciona a linha ao container de mensagens
        messagesContainer.add(rowPanel);

        // 6. Atualiza a interface
        messagesContainer.revalidate();
        messagesContainer.repaint();



    }

    private void addSystemMessage(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4)) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        row.setBackground(Color.WHITE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lbl.setForeground(new Color(150, 150, 150));
        row.add(lbl);

        messagesContainer.add(row);
        messagesContainer.revalidate();
        messagesContainer.repaint();
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = messagesScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private String formatDate(String isoDate) {
        try {
            String[] p = isoDate.split("-");
            return p[2] + "/" + p[1] + "/" + p[0];
        } catch (Exception e) { return isoDate; }
    }

    private String countText(int n) {
        return n + " mensagem" + (n != 1 ? "s" : "");
    }

    private Color getUserColor(String user) {
        Color[] palette = {
                new Color(2, 119, 189),
                new Color(27, 94, 32),
                new Color(106, 27, 154),
                new Color(183, 84, 0),
                new Color(0, 105, 92),
                new Color(173, 20, 87),
        };
        return palette[Math.abs(user.hashCode()) % palette.length];
    }

    // ── TAB OCORRÊNCIA ────────────────────────────────────────────────────────
    // --- Adicionar Imagem ---

    private JPanel buildOcorrenciaTab() {
        PainelComFundo panel = new PainelComFundo("/home/enzo/Downloads/teste/logo_tiete_final.jpg");
        panel.setLayout(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 60, 20, 60));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(7, 5, 7, 5);

        //--- Titulo ---
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        JLabel secTitle = new JLabel("Registrar Nova Ocorrência de Poluição");
        secTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        secTitle.setForeground(new Color(144, 238,144));
        panel.add(secTitle, g);

        g.gridy = 1;
        panel.add(new JSeparator(), g);

        // --- Campo indústria ---
        g.gridwidth = 1; g.gridy = 2; g.gridx = 0; g.weightx = 0.3;
        JLabel lblInd = new JLabel("Industria");
        lblInd.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblInd.setForeground(Color.WHITE);
        panel.add(lblInd, g);

        g.gridx = 1; g.weightx = 0.7;
        industriaField = new JTextField(20);
        styleInput(industriaField);
        panel.add(industriaField, g);

        // --- Campo poluente ---
        g.gridx = 0; g.gridy = 3; g.weightx = 0.3;
        JLabel lblPol = new JLabel("Tipo de poluente:");
        lblPol.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPol.setForeground(Color.WHITE);
        panel.add(lblPol, g);

        g.gridx = 1; g.weightx = 0.7;
        polutenteCombo = new JComboBox<>(new String[]{
                "Efluente Líquido", "Resíduo Sólido", "Produto Químico",
                "Metais Pesados", "Esgoto Industrial", "Óleo / Graxas", "Outro"
        });
        panel.add(polutenteCombo, g);

        // --- Campo nível de risco ---
        g.gridx = 0; g.gridy = 4; g.weightx = 0.3;
        JLabel lblRisco = new JLabel("Nível de risco:");
        lblRisco.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRisco.setForeground(Color.WHITE);
        panel.add(lblRisco, g);

        g.gridx = 1; g.weightx = 0.7;
        nivelSlider = new JSlider(1, 5, 1);
        nivelSlider.setOpaque(false); // Deixa o fundo transparente para mostrar a imagem
        nivelSlider.setForeground(Color.WHITE);
        nivelSlider.setMajorTickSpacing(1);
        nivelSlider.setPaintTicks(true);
        nivelSlider.setPaintLabels(true);

        nivelLabel = new JLabel("  1 — Baixo");
        nivelLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        nivelLabel.setForeground(Color.YELLOW);

        nivelSlider.addChangeListener(e -> updateNivelLabel());

        JPanel nivelRow = new JPanel(new BorderLayout(8, 0));
        nivelRow.setOpaque(false); // 
        nivelRow.add(nivelSlider, BorderLayout.CENTER);
        nivelRow.add(nivelLabel,  BorderLayout.EAST);
        panel.add(nivelRow, g);

        // --- Campo localização ---
        g.gridx = 0; g.gridy = 5; g.weightx = 0.3;
        JLabel lblLocal = new JLabel("Localização (km):");
        lblLocal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblLocal.setForeground(Color.WHITE);
        panel.add(lblLocal, g);

        g.gridx = 1; g.weightx = 0.7;
        localizacaoField = new JTextField();
        styleInput(localizacaoField);
        panel.add(localizacaoField, g);

        // --- Campo descrição ---
        g.gridx = 0; g.gridy = 6; g.weightx = 0.3;
        JLabel lblDesc = new JLabel("Descrição:");
        lblDesc.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDesc.setForeground(Color.WHITE);
        panel.add(lblDesc, g);

        g.gridx = 1; g.weightx = 0.7;
        descricaoArea = new JTextArea(4, 20);
        descricaoArea.setLineWrap(true);
        descricaoArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descricaoArea);
        panel.add(descScroll, g);

        // --- Botão registrar ---
        g.gridx = 0; g.gridy = 7; g.gridwidth = 2;
        g.insets = new Insets(16, 5, 5, 5);
        JButton registrarBtn = btn("Registrar Ocorrência", RED_ALERT);
        registrarBtn.setPreferredSize(new Dimension(0, 44));
        panel.add(registrarBtn, g);

        registrarBtn.addActionListener(e -> sendOcorrencia());

        return panel;
    }

    // ── TAB PAINEL ────────────────────────────────────────────────────────────

    private JPanel buildPainelTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controls.setBackground(Color.WHITE);

        btnAtualizar = btn("Atualizar", BLUE_RIVER);
        controls.add(btnAtualizar);
        btnAtualizar.addActionListener(e -> refreshEntries());

        JLabel legend = new JLabel("  Legenda de risco:  ");
        legend.setFont(new Font("SansSerif", Font.PLAIN, 12));
        controls.add(legend);
        controls.add(riskChip("Crítico (5)", new Color(255, 205, 205)));
        controls.add(riskChip("Grave (4)",   new Color(255, 224, 178)));
        controls.add(riskChip("Alto (3)",    new Color(255, 249, 196)));
        controls.add(riskChip("Baixo (1-2)", new Color(232, 245, 233)));

        String[] cols = {"#", "Data/Hora", "Inspetor", "Indústria", "Poluente", "Risco", "Localização", "Descrição"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.setGridColor(new Color(220, 220, 220));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(GREEN_DARK);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false); // Trava o arrastar
        table.getTableHeader().setResizingAllowed(true);  // Deixa mudar só a largura

        int[] widths = {35, 130, 90, 120, 110, 50, 120, 200};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) {
                    int nivel = 1;
                    try { nivel = Integer.parseInt(tableModel.getValueAt(row, 5).toString()); }
                    catch (Exception ignored) {}
                    c.setBackground(switch (nivel) {
                        case 5 -> new Color(255, 205, 205);
                        case 4 -> new Color(255, 224, 178);
                        case 3 -> new Color(255, 249, 196);
                        default -> row % 2 == 0 ? Color.WHITE : new Color(240, 248, 240);
                    });
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(GREEN_MED));

        panel.add(controls, BorderLayout.NORTH);
        panel.add(scroll,   BorderLayout.CENTER);

        return panel;


    }


    // ── Ações ─────────────────────────────────────────────────────────────────

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (msg.isBlank()) return;
        client.sendChat(msg);
        chatInput.setText("");
    }

    private void sendOcorrencia() {
        String industria   = industriaField.getText().trim();
        String poluente    = (String) polutenteCombo.getSelectedItem();
        int nivel          = nivelSlider.getValue();
        String localizacao = localizacaoField.getText().trim();
        String descricao   = descricaoArea.getText().trim();

        if (industria.isBlank()) {
            JOptionPane.showMessageDialog(this, "Informe o nome da indústria.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (localizacao.isBlank() || localizacao.startsWith("Ex:")) {
            JOptionPane.showMessageDialog(this, "Informe a localização.", "Campo obrigatório", JOptionPane.WARNING_MESSAGE);
            return;
        }

        client.sendOcorrencia(industria, poluente, nivel, localizacao, descricao);

        industriaField.setText("");
        descricaoArea.setText("");
        nivelSlider.setValue(1);
        resetPlaceholder(localizacaoField, "Ex: km 45 — Mogi das Cruzes");

        JOptionPane.showMessageDialog(this,
                "Ocorrência registrada e transmitida para todos os inspetores.",
                "Ocorrência Enviada", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── MessageListener ───────────────────────────────────────────────────────

    @Override
    public void onMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            int pipe = message.indexOf('|');
            String type    = pipe == -1 ? message : message.substring(0, pipe);
            String payload = pipe == -1 ? "" : message.substring(pipe + 1);

            switch (type) {
                case Protocol.CHAT_MSG -> {
                    int p2 = payload.indexOf('|');
                    String user = p2 == -1 ? payload : payload.substring(0, p2);
                    String text = p2 == -1 ? "" : payload.substring(p2 + 1);

                    int cnt = dayCountMap.getOrDefault(todayDate, 0) + 1;
                    dayCountMap.put(todayDate, cnt);
                    updateDayCardCount(todayDate, cnt);

                    if (todayDate.equals(selectedDate)) {
                        String time = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"))
                                .format(DateTimeFormatter.ofPattern("HH:mm"));
                        addMessageCard(user, time, text);
                    }
                }
                case Protocol.MESSAGE_DATE -> {
                    String[] p = payload.split("\\|", 2);
                    if (p.length >= 2) {
                        int count = 0;
                        try { count = Integer.parseInt(p[1]); } catch (NumberFormatException ignored) {}
                        dayCountMap.put(p[0], count);
                        addOrUpdateDayCard(p[0], count);
                    }
                }
                case Protocol.MESSAGE_DATES_END -> {
                    ensureTodayCard();
                    if (selectedDate == null) {
                        JPanel todayCard = dayCardMap.get(todayDate);
                        if (todayCard != null) selectDayCard(todayDate, todayCard);
                    }
                }
                case Protocol.MESSAGE_ITEM -> {
                    String[] p = payload.split("\\|", 3);
                    if (p.length >= 3) addMessageCard(p[0], p[1], p[2]);
                }
                case Protocol.MESSAGES_END -> {
                    SwingUtilities.invokeLater(() -> {
                        JScrollBar bar = messagesScroll.getVerticalScrollBar();
                        bar.setValue(bar.getMaximum());
                    });
                }
                case Protocol.USER_JOIN -> {
                    if (todayDate.equals(selectedDate)) addSystemMessage("— " + payload + " entrou no sistema —");
                }
                case Protocol.USER_LEAVE -> {
                    if (todayDate.equals(selectedDate)) addSystemMessage("— " + payload + " saiu do sistema —");
                }
                case Protocol.OCORRENCIA_NEW -> {
                    String[] p = payload.split("\\|", 3);
                    String inspector = p.length > 1 ? p[1] : "?";
                    if (todayDate.equals(selectedDate))
                        addSystemMessage("⚠ Nova ocorrência registrada por " + inspector + " — veja o Painel");
                    addTableRow(payload);
                }
                case Protocol.OCORRENCIA_ITEM -> {
                    // Divide a mensagem: id|inspetor|industria|poluente|risco|local|desc|data [cite: 202]
                    String[] dados = payload.split("\\|", -1);
                    if (dados.length >= 8) {
                        addTableRow(payload);
                    }
                }
                case Protocol.OCORRENCIAS_END -> {
                    // Fim da transmissão das ocorrências pelo servidor [cite: 204]
                }

            }
        });
    }

    @Override
    public void onDisconnect() {
        SwingUtilities.invokeLater(() -> {
            if (messagesContainer != null && todayDate != null && todayDate.equals(selectedDate))
                addSystemMessage("— Conexão encerrada com o servidor —");
            JOptionPane.showMessageDialog(this,
                    "A conexão com o servidor foi perdida.",
                    "Desconectado", JOptionPane.ERROR_MESSAGE);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addTableRow(String data) {
        // O split separa os dados pelo símbolo |
        String[] p = data.split("\\|", -1);

        if (p.length >= 8) {
            // Isso aqui garante que a tabela atualize "ao vivo" na frente do usuário
            SwingUtilities.invokeLater(() -> {
                try {
                    tableModel.addRow( new Object[]{
                            p[0], // id
                            p[7], // data/hora
                            p[1], // inspetor
                            p[2], // indústria
                            p[3], // poluente
                            p[4], // risco
                            p[5], // localizacao
                            p[6], // descricao
                    });
                } catch (Exception e) {
                    System.err.println("Erro ao adicionar linha na tabela: " + e.getMessage());
                }
            });
        }
    }


    private void updateNivelLabel() {
        int v = nivelSlider.getValue();
        String[] names  = {"", "Baixo", "Moderado", "Alto", "Grave", "Crítico"};
        Color[]  colors = {null,
                new Color(27, 130, 27), new Color(180, 130, 0),
                new Color(220, 100, 0), new Color(180, 30, 0), Color.RED
        };
        nivelLabel.setText("  " + v + " — " + names[v]);
        nivelLabel.setForeground(colors[v]);
    }

    private JLabel formLbl(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        return l;
    }

    private void styleInput(JTextField f) {
        f.setFont(new Font("SansSerif", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 189, 189)),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void placeholder(JTextField f, String hint) {
        f.setText(hint);
        f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (f.getForeground() == Color.GRAY) { f.setText(""); f.setForeground(Color.BLACK); }
            }
            @Override public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) { f.setText(hint); f.setForeground(Color.GRAY); }
            }
        });
    }

    private void resetPlaceholder(JTextField f, String hint) {
        f.setText(hint);
        f.setForeground(Color.GRAY);
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JLabel riskChip(String text, Color bg) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));
        return l;
    }

    // ── Ação botão atualizar ───────────────────────────────────────────────────────────────
    private void refreshEntries() {
        // 1. Limpa a tabela para não encavalar dados
        if (tableModel != null) {
            tableModel.setRowCount(0);
        }

        // 2. Avisa o servidor que você quer a lista da data selecionada
        if (client != null) {
            client.requestOccurrences(selectedDate);
            System.out.println("DEBUG: Solicitando ocorrências para " + selectedDate);
        }
    }

    // Esta classe é um "Painel com fundos"
    class PainelComFundo extends JPanel {
        private Image imagem;

        public PainelComFundo(String caminho) {
            this.imagem = new ImageIcon(caminho).getImage();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(imagem, 0, 0, getWidth(), getHeight(), this);

            g.setColor(new Color(0, 0, 0, 80));
            g.fillRect(0,0, getWidth(), getHeight());
        }

    }


}