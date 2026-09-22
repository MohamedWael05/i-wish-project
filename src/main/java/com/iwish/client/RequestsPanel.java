package com.iwish.client;

import com.iwish.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RequestsPanel extends JPanel {

    private final DefaultListModel<FriendInfo> model = new DefaultListModel<>();
    private final JList<FriendInfo> list = new JList<>(model);

    public RequestsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(UiTheme.BACKGROUND);

        JLabel header = new JLabel("Pending Friend Requests");
        header.setFont(UiTheme.FONT_HEADER);
        header.setForeground(UiTheme.TEXT_DARK);
        add(header, BorderLayout.NORTH);

        list.setFont(UiTheme.FONT_BODY);
        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            JLabel lab = new JLabel("  \u2709  " + value.getFullName() + "  (@" + value.getUsername() + ") wants to be friends");
            lab.setOpaque(true);
            lab.setFont(UiTheme.FONT_BODY);
            lab.setBorder(new EmptyBorder(10, 4, 10, 4));
            lab.setBackground(isSelected ? new Color(0xE6E0FA) : Color.WHITE);
            return lab;
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0DAF5)));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setOpaque(false);
        RoundedButton acceptBtn = new RoundedButton("Accept", UiTheme.ACCENT_GREEN, Color.WHITE);
        acceptBtn.addActionListener(e -> respond(true));
        RoundedButton declineBtn = new RoundedButton("Decline", UiTheme.WARN, Color.WHITE);
        declineBtn.addActionListener(e -> respond(false));
        RoundedButton refreshBtn = new RoundedButton("Refresh", UiTheme.TEXT_MUTED, Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());
        buttons.add(acceptBtn);
        buttons.add(declineBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        ServerCall.run(this, new Request(Actions.GET_PENDING_REQUESTS), resp -> {
            if (!resp.isSuccess()) return;
            model.clear();
            List<FriendInfo> pending = (List<FriendInfo>) resp.getData();
            for (FriendInfo fi : pending) model.addElement(fi);
        });
    }

    private void respond(boolean accept) {
        FriendInfo fi = list.getSelectedValue();
        if (fi == null) {
            ServerCall.showInfo(this, "Select a request first.");
            return;
        }
        Request req = new Request(Actions.RESPOND_FRIEND_REQUEST)
                .put("friendshipId", fi.getFriendshipId())
                .put("accept", String.valueOf(accept));
        ServerCall.run(this, req, resp -> {
            if (resp.isSuccess()) refresh(); else ServerCall.showError(this, resp.getMessage());
        });
    }
}
