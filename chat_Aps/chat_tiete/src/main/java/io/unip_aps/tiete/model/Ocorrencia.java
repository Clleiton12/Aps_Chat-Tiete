package io.unip_aps.tiete.model;

public class Ocorrencia {

    private final int id;
    private final String inspector;
    private final String industria;
    private final String tipoPoluente;
    private final int nivelRisco;
    private final String localizacao;
    private final String descricao;
    private final String timestamp;

    public Ocorrencia(int id, String inspector, String industria, String tipoPoluente,
                      int nivelRisco, String localizacao, String descricao, String timestamp) {
        this.id = id;
        this.inspector = inspector;
        this.industria = industria;
        this.tipoPoluente = tipoPoluente;
        this.nivelRisco = nivelRisco;
        this.localizacao = localizacao;
        this.descricao = descricao != null ? descricao : "";
        this.timestamp = timestamp != null ? timestamp : "";
    }

    // formato: id|inspector|industria|tipoPoluente|nivelRisco|localizacao|descricao|timestamp
    public String toProtocolString() {
        return id + "|" + inspector + "|" + industria + "|" + tipoPoluente + "|"
                + nivelRisco + "|" + localizacao + "|" + descricao + "|" + timestamp;
    }

    public int getId()           { return id; }
    public String getInspector() { return inspector; }
    public String getIndustria() { return industria; }
    public String getTipoPoluente() { return tipoPoluente; }
    public int getNivelRisco()   { return nivelRisco; }
    public String getLocalizacao() { return localizacao; }
    public String getDescricao() { return descricao; }
    public String getTimestamp() { return timestamp; }
}