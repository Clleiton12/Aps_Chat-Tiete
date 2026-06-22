package io.unip_aps.tiete.client;

import java.io.*;
import java.net.Socket;

import io.unip_aps.tiete.util.Protocol;

public class ChatClient {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;
    private volatile boolean connected = false;

    public interface MessageListener {
        void onMessage(String message);
        void onDisconnect();
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    public boolean connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            connected = true;
            Thread reader = new Thread(this::listen);
            reader.setDaemon(true);
            reader.start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (listener != null) listener.onMessage(line);
            }
        } catch (IOException e) {
            // conexão encerrada
        } finally {
            connected = false;
            if (listener != null) listener.onDisconnect();
        }
    }

    private void send(String message) {
        if (out != null) out.println(message);
    }

    public void login(String username, String password) {
        send(Protocol.LOGIN + "|" + username + "|" + password);
    }

    public void register(String username, String password) {
        send(Protocol.REGISTER + "|" + username + "|" + password);
    }

    public void sendChat(String message) {
        send(Protocol.CHAT + "|" + message);
    }

    public void sendOcorrencia(String industria, String tipoPoluente, int nivelRisco,
                                String localizacao, String descricao) {
        send(Protocol.OCORRENCIA + "|" + industria + "|" + tipoPoluente + "|"
                + nivelRisco + "|" + localizacao + "|" + descricao);
    }

    public void requestOcorrencias(String string) {
        send(Protocol.GET_OCORRENCIAS);
    }

    public void requestMessageDates() {
        send(Protocol.GET_MESSAGE_DATES);
    }

    public void requestMessages(String date) {
        send(Protocol.GET_MESSAGES + "|" + date);
    }

    public void disconnect() {
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    public boolean isConnected() { return connected; }

    public void requestOccurrences(String selectedDate) {
        send(Protocol.GET_OCORRENCIAS + "|" + selectedDate);

        System.out.println("CLIENTE -> Pedindo ocorrências de: " + selectedDate);
    }
}