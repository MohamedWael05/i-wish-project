package com.iwish.client;

import java.awt.Color;
import java.awt.Font;

public class UiTheme {
    public static final Color PRIMARY      = new Color(0x6C5CE7); // soft violet
    public static final Color PRIMARY_DARK = new Color(0x5A4BD6);
    public static final Color ACCENT       = new Color(0xFF7675); // coral pink
    public static final Color ACCENT_GREEN = new Color(0x00B894); // success green
    public static final Color BACKGROUND   = new Color(0xF5F3FF);
    public static final Color CARD_BG      = Color.WHITE;
    public static final Color TEXT_DARK    = new Color(0x2D3436);
    public static final Color TEXT_MUTED   = new Color(0x636E72);
    public static final Color WARN         = new Color(0xE17055);

    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEAD  = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 12);

    private UiTheme() {}
}
