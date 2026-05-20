package com.cmsc137.network;

/**
 * Standardizes the String-based protocol over TCP to prevent parsing errors.
 */
public class NetworkProtocol {
    // Server -> Client
    public static final String ASSIGN_ID = "ASSIGN_ID";
    public static final String SPAWN_MOUSE = "SPAWN_MOUSE";
    public static final String SCORE_UPDATE = "SCORE_UPDATE";
    public static final String GAME_OVER = "GAME_OVER";
    public static final String PAW_STRETCH = "PAW_STRETCH";
    public static final String START_GAME = "START_GAME";
    public static final String PLAYER_DISCONNECT = "PLAYER_DISCONNECT";
    
    // Client -> Server
    public static final String CLICK_EVENT = "CLICK_EVENT";
    public static final String HOST_START = "HOST_START";
    
    // Both
    public static final String DISCONNECT = "DISCONNECT";
    public static final String EXIT_TO_MENU = "EXIT_TO_MENU";

    // Helper formatting methods
    public static String formatSpawnMouse(int mouseId, int x, int y) {
        return SPAWN_MOUSE + "," + mouseId + "," + x + "," + y;
    }

    public static String formatClickEvent(int playerId, int x, int y) {
        return CLICK_EVENT + "," + playerId + "," + x + "," + y;
    }

    public static String formatPawStretch(int playerId, int targetX, int targetY) {
        return PAW_STRETCH + "," + playerId + "," + targetX + "," + targetY;
    }

    public static String formatScoreUpdate(int p1, int p2, int p3, int p4) {
        return SCORE_UPDATE + "," + p1 + "," + p2 + "," + p3 + "," + p4;
    }
}