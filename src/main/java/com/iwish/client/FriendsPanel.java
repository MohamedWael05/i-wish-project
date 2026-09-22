package com.iwish.client;

import com.iwish.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class FriendsPanel extends JPanel {

    private final DefaultListModel<FriendInfo> friendListModel = new DefaultListModel<>();
    private final JList<FriendInfo> friendList = new JList<>(friendListModel);
    private final JTextField searchField = new JTextField();
    private final MainFrame mainFrame;

    public FriendsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(UiTheme.BACKGROUND);

        JLabel header = new JLabel("Friends");
        header.setFont(UiTheme.FONT_HEADER);
        header.setForeground(UiTheme.TEXT_DARK);
        add(header, BorderLayout.NORTH);

        JPanel addRow = new JPanel(new BorderLayout(8, 0));
        addRow.setOpaque(false);
        searchField.setFont(UiTheme.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCD6F7), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        RoundedButton sendReqBtn = new RoundedButton("Send Friend Request", UiTheme.PRIMARY, Color.WHITE);
        sendReqBtn.addActionListener(e -> sendRequest());
        addRow.add(new JLabel("Add by exact username:  "), BorderLayout.WEST);
        addRow.add(searchField, BorderLayout.CENTER);
        addRow.add(sendReqBtn, BorderLayout.EAST);

        friendList.setFont(UiTheme.FONT_BODY);
        friendList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            JLabel l = new JLabel("  \uD83D\uDC64  " + value.getFullName() + "   (@" + value.getUsername() + ")");
            l.setOpaque(true);
            l.setFont(UiTheme.FONT_BODY);
            l.setBorder(new EmptyBorder(8, 4, 8, 4));
            l.setBackground(isSelected ? new Color(0xE6E0FA) : Color.WHITE);
            return l;
        });
        JScrollPane listScroll = new JScrollPane(friendList);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0DAF5)));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(addRow, BorderLayout.NORTH);
        center.add(listScroll, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setOpaque(false);
        RoundedButton viewBtn = new RoundedButton("View Wish List", UiTheme.ACCENT_GREEN, Color.WHITE);
        viewBtn.addActionListener(e -> viewSelectedWishlist());
        RoundedButton removeBtn = new RoundedButton("Remove Friend", UiTheme.WARN, Color.WHITE);
        removeBtn.addActionListener(e -> removeSelected());
        RoundedButton refreshBtn = new RoundedButton("Refresh", UiTheme.TEXT_MUTED, Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());
        buttons.add(viewBtn);
        buttons.add(removeBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        ServerCall.run(this, new Request(Actions.GET_FRIENDS), resp -> {
            if (!resp.isSuccess()) return;
            friendListModel.clear();
            List<FriendInfo> friends = (List<FriendInfo>) resp.getData();
            for (FriendInfo fi : friends) friendListModel.addElement(fi);
        });
    }

    private void sendRequest() {
        String username = searchField.getText().trim();
        if (username.isEmpty()) {
            ServerCall.showInfo(this, "Enter the exact username of the friend you want to add.");
            return;
        }
        Request req = new Request(Actions.SEND_FRIEND_REQUEST).put("username", username);
        ServerCall.run(this, req, resp -> {
            if (resp.isSuccess()) {
                ServerCall.showInfo(this, resp.getMessage());
                searchField.setText("");
            } else {
                ServerCall.showError(this, resp.getMessage());
            }
        });
    }

    private void removeSelected() {
        FriendInfo fi = friendList.getSelectedValue();
        if (fi == null) {
            ServerCall.showInfo(this, "Select a friend first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove " + fi.getFullName() + " from your friends?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Request req = new Request(Actions.REMOVE_FRIEND).put("friendshipId", fi.getFriendshipId());
            ServerCall.run(this, req, resp -> {
                if (resp.isSuccess()) refresh(); else ServerCall.showError(this, resp.getMessage());
            });
        }
    }

    private void viewSelectedWishlist() {
        FriendInfo fi = friendList.getSelectedValue();
        if (fi == null) {
            ServerCall.showInfo(this, "Select a friend first.");
            return;
        }
        new FriendWishlistDialog(SwingUtilities.getWindowAncestor(this), fi).setVisible(true);
    }
}
