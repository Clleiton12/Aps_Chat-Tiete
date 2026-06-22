package io.unip_aps.tiete.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import io.unip_aps.tiete.model.Ocorrencia;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:tiete.db";
    private static DatabaseManager instance;

    private DatabaseManager() {
        initDB();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initDB() {
        // Força o carregamento do driver SQLite — falha rápido se o jar não estiver no classpath
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "\n\n[ERRO FATAL] Driver SQLite não encontrado!\n" +
                "Solução: no IntelliJ, clique com o botão direito em pom.xml → 'Add as Maven Project'\n" +
                "e aguarde o download das dependências.\n", e);
        }
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                username        TEXT UNIQUE NOT NULL,
                password_hash   TEXT NOT NULL,
                created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createOcorrencias = """
            CREATE TABLE IF NOT EXISTS ocorrencias (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                inspector       TEXT NOT NULL,
                industria       TEXT NOT NULL,
                tipo_poluente   TEXT NOT NULL,
                nivel_risco     INTEGER NOT NULL,
                localizacao     TEXT NOT NULL,
                descricao       TEXT,
                timestamp       DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createMessages = """
            CREATE TABLE IF NOT EXISTS messages (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                username  TEXT NOT NULL,
                message   TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createOcorrencias);
            stmt.execute(createMessages);
            System.out.println("[DB] Banco inicializado: tiete.db");
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao inicializar banco: " + e.getMessage());
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public void saveMessage(String username, String message) {
        String sql = "INSERT INTO messages (username, message) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao salvar mensagem: " + e.getMessage());
        }
    }

    public List<String[]> getMessageDates() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT DATE(timestamp, 'localtime') as day, COUNT(*) as cnt FROM messages GROUP BY day ORDER BY day DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(new String[]{rs.getString("day"), String.valueOf(rs.getInt("cnt"))});
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao buscar datas: " + e.getMessage());
        }
        return result;
    }

    public List<String[]> getMessagesByDate(String date) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT username, strftime('%H:%M', timestamp, 'localtime') as time, message FROM messages WHERE DATE(timestamp, 'localtime') = ? ORDER BY timestamp ASC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(new String[]{rs.getString("username"), rs.getString("time"), rs.getString("message")});
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao buscar mensagens: " + e.getMessage());
        }
        return result;
    }

    public String registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.executeUpdate();
            return null; // null = sucesso
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                return "Nome de usuário já existe";
            }
            return "Erro no banco de dados: " + e.getMessage();
        }
    }

    public boolean authenticateUser(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("password_hash").equals(hashPassword(password));
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erro na autenticação: " + e.getMessage());
        }
        return false;
    }

    public int saveOcorrencia(String inspector, String industria, String tipoPoluente,
                               int nivelRisco, String localizacao, String descricao) {
        String sql = """
            INSERT INTO ocorrencias (inspector, industria, tipo_poluente, nivel_risco, localizacao, descricao, timestamp)
            VALUES (?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))
            """;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, inspector);
            ps.setString(2, industria);
            ps.setString(3, tipoPoluente);
            ps.setInt(4, nivelRisco);
            ps.setString(5, localizacao);
            ps.setString(6, descricao);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao salvar ocorrência: " + e.getMessage());
        }
        return -1;
    }

    public Ocorrencia getOcorrenciaById(int id) {
        String sql = "SELECT * FROM ocorrencias WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rowToOcorrencia(rs);
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao buscar ocorrência: " + e.getMessage());
        }
        return null;
    }

    public List<Ocorrencia> getAllOcorrencias() {
        List<Ocorrencia> list = new ArrayList<>();
        String sql = "SELECT * FROM ocorrencias ORDER BY timestamp DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rowToOcorrencia(rs));
        } catch (SQLException e) {
            System.err.println("[DB] Erro ao listar ocorrências: " + e.getMessage());
        }
        return list;
    }

    private Ocorrencia rowToOcorrencia(ResultSet rs) throws SQLException {
        return new Ocorrencia(
            rs.getInt("id"),
            rs.getString("inspector"),
            rs.getString("industria"),
            rs.getString("tipo_poluente"),
            rs.getInt("nivel_risco"),
            rs.getString("localizacao"),
            rs.getString("descricao"),
            rs.getString("timestamp")
        );
    }
}