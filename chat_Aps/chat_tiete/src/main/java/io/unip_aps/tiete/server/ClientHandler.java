package io.unip_aps.tiete.server;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;

import io.unip_aps.tiete.db.DatabaseManager;
import io.unip_aps.tiete.model.Ocorrencia;
import io.unip_aps.tiete.util.Protocol;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;
    private boolean loggedIn = false;
    SensitiveWordBs sensitiveWordBs;

    public ClientHandler(Socket socket, ChatServer server, SensitiveWordBs sensitiveWordBs) {
        this.socket = socket;
        this.server = server;
        this.sensitiveWordBs = sensitiveWordBs;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);

            String line;
            while ((line = in.readLine()) != null) {
                handleMessage(line.trim());
            }
        } catch (IOException e) {
            // cliente desconectou
        } finally {
            disconnect();
        }
    }

    private void handleMessage(String line) {
        int pipe = line.indexOf('|');
        String type = pipe == -1 ? line : line.substring(0, pipe);
        String data = pipe == -1 ? "" : line.substring(pipe + 1);

        if (Protocol.LOGIN.equals(type)) {
            handleLogin(data);
        } else if (Protocol.REGISTER.equals(type)) {
            handleRegister(data);
        } else if (Protocol.CHAT.equals(type)) {
            handleChat(data);
        } else if (Protocol.OCORRENCIA.equals(type)) {
            handleOcorrencia(data);
        } else if (Protocol.GET_OCORRENCIAS.equals(type) || Protocol.OCORRENCIAS_GET.equals(type)) {
            handleGetOcorrencias();
        } else if (Protocol.GET_MESSAGE_DATES.equals(type)) {
            handleGetMessageDates();
        } else if (Protocol.GET_MESSAGES.equals(type)) {
            handleGetMessages(data);
        }
    }

    private void handleLogin(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length < 2) {
            sendMessage(Protocol.LOGIN_FAIL + "|Dados inválidos");
            return;
        }
        String user = parts[0].trim();
        String pass = parts[1];

        if (DatabaseManager.getInstance().authenticateUser(user, pass)) {
            this.username = user;
            this.loggedIn = true;
            sendMessage(Protocol.LOGIN_OK + "|" + username);
            server.broadcast(Protocol.USER_JOIN + "|" + username, this);
            System.out.println("[LOGIN] " + username + " autenticado");
        } else {
            sendMessage(Protocol.LOGIN_FAIL + "|Usuário ou senha incorretos");
        }
    }

    private void handleRegister(String data) {
        String[] parts = data.split("\\|", 2);
        if (parts.length < 2) {
            sendMessage(Protocol.REGISTER_FAIL + "|Dados inválidos");
            return;
        }
        String user = parts[0].trim();
        String pass = parts[1];

        if (user.isBlank() || pass.isBlank()) {
            sendMessage(Protocol.REGISTER_FAIL + "|Campos não podem ser vazios");
            return;
        }
        if (user.contains("|")) {
            sendMessage(Protocol.REGISTER_FAIL + "|Nome de usuário inválido");
            return;
        }

        String erro = DatabaseManager.getInstance().registerUser(user, pass);
        if (erro == null) {
            sendMessage(Protocol.REGISTER_OK);
            System.out.println("[REGISTER] Novo usuário: " + user);
        } else {
            sendMessage(Protocol.REGISTER_FAIL + "|" + erro);
        }
    }

    private void handleChat(String message) {
        if (!loggedIn) return;
        String mensagemFiltrada =sensitiveWordBs.replace(message);
        DatabaseManager.getInstance().saveMessage(username, mensagemFiltrada);

        String broadcast = Protocol.CHAT_MSG + "|" + username + "|" + mensagemFiltrada;
        sendMessage(broadcast);
        server.broadcast(broadcast, this);
    }

    private void handleGetMessageDates() {
        if (!loggedIn) return;
        for (String[] d : DatabaseManager.getInstance().getMessageDates()) {
            sendMessage(Protocol.MESSAGE_DATE + "|" + d[0] + "|" + d[1]);
        }
        sendMessage(Protocol.MESSAGE_DATES_END);
    }

    private void handleGetMessages(String date) {
        if (!loggedIn) return;
        for (String[] m : DatabaseManager.getInstance().getMessagesByDate(date)) {
            sendMessage(Protocol.MESSAGE_ITEM + "|" + m[0] + "|" + m[1] + "|" + m[2]);
        }
        sendMessage(Protocol.MESSAGES_END + "|" + date);
    }

    private void handleOcorrencia(String data) {
        if (!loggedIn) return;
        // formato: industria|tipoPoluente|nivelRisco|localizacao|descricao
        String[] parts = data.split("\\|", 5);
        if (parts.length < 5) {
            sendMessage(Protocol.ERROR + "|Dados de ocorrência incompletos");
            return;
        }

        String industria    = parts[0];
        String tipoPoluente = parts[1];
        int nivelRisco;
        try { nivelRisco = Integer.parseInt(parts[2]); } catch (NumberFormatException e) { nivelRisco = 1; }
        String localizacao  = parts[3];
        String descricao    = parts[4];

        DatabaseManager db = DatabaseManager.getInstance();
        int id = db.saveOcorrencia(username, industria, tipoPoluente, nivelRisco, localizacao, descricao);

        if (id > 0) {
            Ocorrencia saved = db.getOcorrenciaById(id);
            if (saved != null) {
                System.out.println("[OCORRÊNCIA] #" + id + " registrada por " + username + " — " + industria);
                server.broadcastAll(Protocol.OCORRENCIA_NEW + "|" + saved.toProtocolString());
            }
        } else {
            sendMessage(Protocol.ERROR + "|Falha ao salvar ocorrência no banco");
        }
    }

    private void handleGetOcorrencias() {
        if (!loggedIn) return;
        List<Ocorrencia> list = DatabaseManager.getInstance().getAllOcorrencias();
        for (Ocorrencia o : list) {
            sendMessage(Protocol.OCORRENCIA_ITEM + "|" + o.toProtocolString());
        }
        sendMessage(Protocol.OCORRENCIAS_END);
    }

    public void sendMessage(String message) {
        if (out == null) return;

        if (message.startsWith(Protocol.OCORRENCIA_ITEM) || message.startsWith("OCORRENCIA")){
            out.println(message);
            return;
        } 
        // Lógica de filtragem de palavras sensíveis para mensagens de chat
        String[] tokens = message.split("\\|", 3);
        if (tokens.length <3){
            out.println(message); // manda a mensagem sem filtragem se não for do formato esperado
            return;
        }

        String protocolType = tokens[0];
        String user = tokens[1];
        String messageContent = tokens[2];

        // Aplica o filtro de palavras sensíveis apenas para mensagens de chat
        if (this.sensitiveWordBs.contains(messageContent)){
            messageContent = "*****"; // substitui o conteúdo por asteriscos
            System.out.println("Palavra sensível detectada na mensagem de " + user + ". Conteúdo filtrado.");
        }
        // Remota e envia a mensagem (filtrada ou original) para o cliente
        out.println(protocolType + "|" + user + "|" + messageContent);
    }

    public boolean isLoggedIn() { return loggedIn; }
    public String getUsername() { return username; }

    private void disconnect() {
        if (loggedIn && username != null) {
            server.broadcast(Protocol.USER_LEAVE + "|" + username, this);
            System.out.println("[LOGOUT] " + username + " desconectado");
        }
        server.removeClient(this);
        try { socket.close(); } catch (IOException ignored) {}
    }
}