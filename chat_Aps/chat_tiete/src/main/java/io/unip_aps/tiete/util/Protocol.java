package io.unip_aps.tiete.util;

public class Protocol {

    public static final int PORT = 12345;

    // Cliente → Servidor
    public static final String LOGIN            = "LOGIN";
    public static final String REGISTER         = "REGISTER";
    public static final String CHAT             = "CHAT";
    public static final String OCORRENCIA       = "OCORRENCIA";
    public static final String GET_OCORRENCIAS  = "GET_OCORRENCIAS";

    // Cliente → Servidor (mensagens)
    public static final String GET_MESSAGE_DATES = "GET_MESSAGE_DATES";
    public static final String GET_MESSAGES      = "GET_MESSAGES";

    // Servidor → Cliente
    public static final String LOGIN_OK          = "LOGIN_OK";
    public static final String LOGIN_FAIL        = "LOGIN_FAIL";
    public static final String REGISTER_OK       = "REGISTER_OK";
    public static final String REGISTER_FAIL     = "REGISTER_FAIL";
    public static final String CHAT_MSG          = "CHAT_MSG";
    public static final String OCORRENCIA_NEW    = "OCORRENCIA_NEW";
    public static final String OCORRENCIA_ITEM   = "OCORRENCIA_ITEM";
    public static final String OCORRENCIAS_END   = "OCORRENCIAS_END";
    public static final String USER_JOIN         = "USER_JOIN";
    public static final String USER_LEAVE        = "USER_LEAVE";
    public static final String MESSAGE_DATE      = "MESSAGE_DATE";
    public static final String MESSAGE_DATES_END = "MESSAGE_DATES_END";
    public static final String MESSAGE_ITEM      = "MESSAGE_ITEM";
    public static final String MESSAGES_END      = "MESSAGES_END";
    public static final String ERROR             = "ERROR";

    public static final String OCORRENCIAS_GET = "OCORRENCIAS_GET";
}
