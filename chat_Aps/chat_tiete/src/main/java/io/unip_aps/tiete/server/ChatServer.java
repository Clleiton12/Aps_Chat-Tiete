package io.unip_aps.tiete.server;

import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.api.context.InnerSensitiveWordContext;
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.bs.SensitiveWordContext;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;

import io.unip_aps.tiete.util.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {

    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public void start() throws IOException {

        // Caminho do arquivo (no classpath ou absoluto)
//        String path = "src/main/resources/sensitive_word_deny-pt.txt";
        InputStream is = getClass()
                .getResourceAsStream("/sensitive_word_deny-pt.txt");

        if (is == null) {
            throw new RuntimeException("Arquivo não encontrado!");
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(is)
        );

        List<String> denyList = br.lines().toList();
//        String path = "sensitive_word_deny-pt.txt";

        // Carrega as palavras do arquivo
//        List<String> denyList = Files.readAllLines(Paths.get(path));

        // Inicializa o helper com lista customizada
        SensitiveWordBs sensitiveWordBs = SensitiveWordBs.newInstance();
        IWordDeny wordDeny = () -> denyList;

        sensitiveWordBs.wordDeny(wordDeny);
        sensitiveWordBs.init();

        try (ServerSocket serverSocket = new ServerSocket(Protocol.PORT)) {
            System.out.println("===========================================");
            System.out.println("  Servidor Rio Tietê iniciado na porta " + Protocol.PORT);
            System.out.println("===========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[+] Nova conexão: " + clientSocket.getInetAddress().getHostAddress());
                ClientHandler handler = new ClientHandler(clientSocket, this, sensitiveWordBs);
                clients.add(handler);
                Thread t = new Thread(handler);
                t.setDaemon(true);
                t.start();
            }
        }
    }

    // envia para todos exceto o remetente
    public void broadcast(String message, ClientHandler exclude) {
        for (ClientHandler c : clients) {
            if (c != exclude && c.isLoggedIn()) {
                c.sendMessage(message);
            }
        }
    }

    // envia para todos os logados (inclusive o remetente)
    public void broadcastAll(String message) {
        for (ClientHandler c : clients) {
            if (c.isLoggedIn()) {
                c.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    public static void main(String[] args) throws IOException {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Sao_Paulo"));
        
        new ChatServer().start();
    }
}