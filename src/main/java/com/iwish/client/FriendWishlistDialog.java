package com.iwish.client;

import com.iwish.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FriendWishlistDialog extends JDialog {

    private final FriendInfo friend;
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Item", "Price", "Funded", "Remaining", "Status", "Note"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private List<WishlistItem> currentItems;

    public FriendWishlistDialog(Window owner, FriendInfo friend) {
        super(owner, friend.getFullName() + "'s Wish List", ModalityType.APPLICATION_MODAL);
        this.friend = friend;
        setSize(680, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        root.setBackground(UiTheme.BACKGROUND);
        setContentPane(root);

        JLabel header = new JLabel("\uD83C\uDF81  " + friend.getFullName() + "'s Wish List");
        header.setFont(UiTheme.FONT_HEADER);
        root.add(header, BorderLayout.NORTH);

        table.setFont(UiTheme.FONT_BODY);
        table.setRowHeight(26);
        table.getTableHeader().setFont(UiTheme.FONT_SUBHEAD);
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setOpaque(false);
        RoundedButton contributeBtn = new RoundedButton("Contribute to Selected Item", UiTheme.PRIMARY, Color.WHITE);
        contributeBtn.addActionListener(e -> contribute());
        RoundedButton closeBtn = new RoundedButton("Close", UiTheme.TEXT_MUTED, Color.WHITE);
        closeBtn.addActionListener(e -> dispose());
        buttons.add(contributeBtn);
        buttons.add(closeBtn);
        root.add(buttons, BorderLayout.SOUTH);

        load();
    }

    private void load() {
        Request req = new Request(Actions.GET_FRIEND_WISHLIST).put("friendId", friend.getUserId());
        ServerCall.run(this, req, resp -> {
            if (!resp.isSuccess()) {
                ServerCall.showError(this, resp.getMessage());
                return;
            }
            currentItems = (List<WishlistItem>) resp.getData();
            tableModel.setRowCount(0);
            for (WishlistItem wi : currentItems) {
                tableModel.addRow(new Object[]{
                        wi.getItemName(), "$" + wi.getPrice(), "$" + wi.getFundedAmount(),
                        "$" + wi.getRemaining(), wi.getStatus(), wi.getNote() == null ? "" : wi.getNote()
                });
            }
        });
    }

    private void contribute() {
        int row = table.getSelectedRow();
        if (row < 0 || currentItems == null || row >= currentItems.size()) {
            ServerCall.showInfo(this, "Select an item first.");
            return;
        }
        WishlistItem wi = currentItems.get(row);
        if (WishlistItem.STATUS_COMPLETED.equals(wi.getStatus())) {
            ServerCall.showInfo(this, "This item is already fully funded. \uD83C\uDF89");
            return;
        }
        new ContributeDialog(this, wi, () -> load()).setVisible(true);
    }
}
