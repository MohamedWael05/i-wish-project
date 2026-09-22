package com.iwish.client;

import javax.swing.*;
import java.awt.*;

public class RoundedButton extends JButton {

    private Color baseColor;
    private final Color hoverColor;
    private final Color textColor;

    public RoundedButton(String text, Color baseColor, Color textColor) {
        super(text);
        this.baseColor = baseColor;
        this.hoverColor = baseColor.darker();
        this.textColor = textColor;
        setFont(UiTheme.FONT_SUBHEAD);
        setForeground(textColor);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = getModel().isRollover() ? hoverColor : baseColor;
        if (!isEnabled()) fill = Color.LIGHT_GRAY;
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
        g2.dispose();
        super.paintComponent(g);
    }
}
