package com.iwish.client;

import com.iwish.common.Actions;
import com.iwish.common.Notification;
import com.iwish.common.Request;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class NotificationsPanel extends JPanel {

    private final DefaultListModel<Notification> model = new DefaultListModel<>();
    private final JList<Notification> list = new JList<>(model);

    public NotificationsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(UiTheme.BACKGROUND);

        JLabel header = new JLabel("Notifications");
        header.setFont(UiTheme.FONT_HEADER);
        header.setForeground(UiTheme.TEXT_DARK);
        add(header, BorderLayout.NORTH);

        list.setFont(UiTheme.FONT_BODY);
        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            String icon = value.isRead() ? "\u2705" : "\uD83D\uDD14";
            JLabel lab = new JLabel("<html>&nbsp;" + icon + "&nbsp; " + value.getMessage()
                    + "<br>&nbsp;&nbsp;&nbsp;<font color='#636E72' size='2'>" + value.getCreatedAt() + "</font></html>");
            lab.setOpaque(true);
            lab.setFont(UiTheme.FONT_BODY);
            lab.setBorder(new EmptyBorder(8, 4, 8, 4));
            lab.setBackground(isSelected ? new Color(0xE6E0FA) : (value.isRead() ? Color.WHITE : new Color(0xFFF3E0)));
            return lab;
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0DAF5)));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setOpaque(false);
        RoundedButton markReadBtn = new RoundedButton("Mark all as read", UiTheme.ACCENT_GREEN, Color.WHITE);
        markReadBtn.addActionListener(e -> markRead());
        RoundedButton refreshBtn = new RoundedButton("Refresh", UiTheme.TEXT_MUTED, Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());
        buttons.add(markReadBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        ServerCall.run(this, new Request(Actions.GET_NOTIFICATIONS), resp -> {
            if (!resp.isSuccess()) return;
            model.clear();
            List<Notification> items = (List<Notification>) resp.getData();
            for (Notification n : items) model.addElement(n);
        });
    }

    private void markRead() {
        ServerCall.run(this, new Request(Actions.MARK_NOTIFICATIONS_READ), resp -> refresh());
    }
}
