package io.unip_aps.tiete.client;

import java.util.Scanner;

import io.unip_aps.tiete.util.Protocol;

public class ClienteCLI {

    private final ChatClient client = new ChatClient();
    private final Scanner scanner   = new Scanner(System.in);
    private volatile boolean loggedIn = false;
    private String username;

    public static void main(String[] args) {
        new ClienteCLI().run();
    }

    private void run() {
        banner();

        String host = prompt("Servidor [localhost]: ");
        if (host.isBlank()) host = "localhost";

        String portStr = prompt("Porta [12345]: ");
        int port = portStr.isBlank() ? 12345 : Integer.parseInt(portStr.trim());

        System.out.print("Conectando...");
        if (!client.connect(host, port)) {
            System.out.println(" FALHA. Servidor inacessível em " + host + ":" + port);
            return;
        }
        System.out.println(" OK\n");

        client.setListener(new ChatClient.MessageListener() {
            @Override public void onMessage(String msg)  { handleServerMessage(msg); }
            @Override public void onDisconnect()         { System.out.println("\n[!] Conexão encerrada pelo servidor."); System.exit(0); }
        });

        // Login ou cadastro
        authLoop();

        // Loop principal de chat
        chatLoop();
    }

    private void authLoop() {
        while (!loggedIn) {
            System.out.println("  [1] Entrar    [2] Cadastrar    [0] Sair");
            String op = prompt("Opção: ").trim();
            switch (op) {
                case "1" -> doLogin();
                case "2" -> doRegister();
                case "0" -> { client.disconnect(); System.exit(0); }
                default  -> System.out.println("Opção inválida.");
            }
        }
    }

    private void doLogin() {
        String user = prompt("Usuário: ").trim();
        String pass = prompt("Senha:   ").trim();
        client.login(user, pass);
        waitForAuth();
    }

    private void doRegister() {
        String user = prompt("Novo usuário: ").trim();
        String pass = prompt("Nova senha:   ").trim();
        client.register(user, pass);
        sleep(600);
    }

    // bloqueia até receber LOGIN_OK ou LOGIN_FAIL
    private void waitForAuth() {
        long deadline = System.currentTimeMillis() + 5000;
        while (!loggedIn && System.currentTimeMillis() < deadline) sleep(100);
        if (!loggedIn) System.out.println("[!] Sem resposta do servidor.");
    }

    private void chatLoop() {
        help();
        while (true) {
            String line = scanner.nextLine();
            if (line == null) break;
            line = line.trim();

            if (line.isBlank()) continue;

            switch (line) {
                case "/sair"     -> { client.disconnect(); System.out.println("Até logo!"); return; }
                case "/ajuda"    -> help();
                case "/painel"   -> client.requestOcorrencias(line);
                case "/ocorr"    -> registrarOcorrencia();
                default          -> {
                    if (line.startsWith("/")) System.out.println("[?] Comando desconhecido. Digite /ajuda.");
                    else client.sendChat(line);
                }
            }
        }
    }

    private void registrarOcorrencia() {
        System.out.println("\n── Nova Ocorrência ──────────────────────────");
        String industria   = prompt("Indústria:           ");
        System.out.println("Tipo de poluente:");
        System.out.println("  1. Efluente Líquido  2. Resíduo Sólido  3. Produto Químico");
        System.out.println("  4. Metais Pesados    5. Esgoto Industrial  6. Outro");
        String[] tipos = {"", "Efluente Líquido", "Resíduo Sólido", "Produto Químico",
                              "Metais Pesados", "Esgoto Industrial", "Outro"};
        int tipoIdx;
        try { tipoIdx = Integer.parseInt(prompt("Tipo [1-6]: ").trim()); }
        catch (NumberFormatException e) { tipoIdx = 6; }
        String poluente = (tipoIdx >= 1 && tipoIdx <= 6) ? tipos[tipoIdx] : "Outro";

        int nivel;
        try { nivel = Integer.parseInt(prompt("Nível de risco [1-5]: ").trim()); }
        catch (NumberFormatException e) { nivel = 1; }
        nivel = Math.max(1, Math.min(5, nivel));

        String localizacao = prompt("Localização (ex: km 45 - Mogi das Cruzes): ");
        String descricao   = prompt("Descrição:           ");

        if (industria.isBlank() || localizacao.isBlank()) {
            System.out.println("[!] Indústria e localização são obrigatórios.");
            return;
        }

        client.sendOcorrencia(industria, poluente, nivel, localizacao, descricao);
        System.out.println("────────────────────────────────────────────\n");
    }

    private void handleServerMessage(String message) {
        int pipe = message.indexOf('|');
        String type    = pipe == -1 ? message : message.substring(0, pipe);
        String payload = pipe == -1 ? "" : message.substring(pipe + 1);

        switch (type) {
            case Protocol.LOGIN_OK      -> { username = payload; loggedIn = true; System.out.println("\n[OK] Bem-vindo, " + username + "!\n"); }
            case Protocol.LOGIN_FAIL    -> System.out.println("[ERRO] " + payload);
            case Protocol.REGISTER_OK   -> System.out.println("[OK] Cadastro realizado. Faça login.");
            case Protocol.REGISTER_FAIL -> System.out.println("[ERRO] " + payload);

            case Protocol.CHAT_MSG -> {
                int p2 = payload.indexOf('|');
                String user = p2 == -1 ? payload : payload.substring(0, p2);
                String text = p2 == -1 ? "" : payload.substring(p2 + 1);
                System.out.println("[chat] " + user + ": " + text);
            }

            case Protocol.USER_JOIN  -> System.out.println("[>>>] " + payload + " entrou.");
            case Protocol.USER_LEAVE -> System.out.println("[<<<] " + payload + " saiu.");

            case Protocol.OCORRENCIA_NEW -> {
                String[] p = payload.split("\\|", 8);
                if (p.length >= 6) {
                    System.out.println("\n[ALERTA] Nova ocorrência #" + p[0]);
                    System.out.println("         Inspetor : " + p[1]);
                    System.out.println("         Indústria: " + p[2]);
                    System.out.println("         Poluente : " + p[3]);
                    System.out.println("         Risco    : " + p[4] + "/5");
                    System.out.println("         Local    : " + p[5]);
                    if (p.length >= 7 && !p[6].isBlank())
                        System.out.println("         Descrição: " + p[6]);
                }
            }

            case Protocol.OCORRENCIA_ITEM -> {
                String[] p = payload.split("\\|", 8);
                if (p.length >= 8)
                    System.out.printf("  #%-4s %-19s %-12s %-20s risco:%s  %s%n",
                        p[0], p[7], p[1], p[2], p[4], p[5]);
            }

            case Protocol.OCORRENCIAS_END -> System.out.println("  (fim das ocorrências)\n");
            case Protocol.ERROR           -> System.out.println("[!] " + payload);
        }
    }

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine();
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    private void banner() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║                                              ║");
        System.out.println("║      Chat de Monitoramento Ambiental         ║");
        System.out.println("║                                              ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    private void help() {
        System.out.println("\nComandos disponíveis:");
        System.out.println("  /ocorr   — registrar nova ocorrência de poluição");
        System.out.println("  /painel  — listar todas as ocorrências");
        System.out.println("  /ajuda   — mostrar esta ajuda");
        System.out.println("  /sair    — desconectar");
        System.out.println("  <texto>  — enviar mensagem no chat\n");
    }
}