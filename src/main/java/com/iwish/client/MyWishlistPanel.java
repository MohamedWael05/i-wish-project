package com.iwish.client;

import com.iwish.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MyWishlistPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Item", "Price", "Funded", "Remaining", "Status", "Note"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private List<WishlistItem> currentItems = new ArrayList<>();

    public MyWishlistPanel() {
        setLayout(new BorderLayout(0, 12));
        setBorder(new EmptyBorder(16, 20, 16, 20));
        setBackground(UiTheme.BACKGROUND);

        JLabel header = new JLabel("My Wish List");
        header.setFont(UiTheme.FONT_HEADER);
        header.setForeground(UiTheme.TEXT_DARK);
        add(header, BorderLayout.NORTH);

        table.setFont(UiTheme.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UiTheme.FONT_SUBHEAD);
        table.setSelectionBackground(new Color(0xE6E0FA));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE0DAF5)));
        add(scroll, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        buttons.setOpaque(false);

        RoundedButton addBtn = new RoundedButton("+ Add Item", UiTheme.PRIMARY, Color.WHITE);
        addBtn.addActionListener(e -> openAddDialog());

        RoundedButton editNoteBtn = new RoundedButton("Edit Note", UiTheme.TEXT_MUTED, Color.WHITE);
        editNoteBtn.addActionListener(e -> editSelectedNote());

        RoundedButton deleteBtn = new RoundedButton("Remove", UiTheme.WARN, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelected());

        RoundedButton refreshBtn = new RoundedButton("Refresh", UiTheme.ACCENT_GREEN, Color.WHITE);
        refreshBtn.addActionListener(e -> refresh());

        buttons.add(addBtn);
        buttons.add(editNoteBtn);
        buttons.add(deleteBtn);
        buttons.add(refreshBtn);
        add(buttons, BorderLayout.SOUTH);
    }

    public void refresh() {
        ServerCall.run(this, new Request(Actions.GET_MY_WISHLIST), resp -> {
            if (!resp.isSuccess()) {
                ServerCall.showError(this, resp.getMessage());
                return;
            }
            currentItems = (List<WishlistItem>) resp.getData();
            tableModel.setRowCount(0);
            for (WishlistItem wi : currentItems) {
                tableModel.addRow(new Object[]{
                        wi.getItemName(),
                        "$" + wi.getPrice(),
                        "$" + wi.getFundedAmount(),
                        "$" + wi.getRemaining(),
                        wi.getStatus(),
                        wi.getNote() == null ? "" : wi.getNote()
                });
            }
        });
    }

    private void openAddDialog() {
        ServerCall.run(this, new Request(Actions.GET_CATALOG_ITEMS), resp -> {
            if (!resp.isSuccess()) {
                ServerCall.showError(this, resp.getMessage());
                return;
            }
            List<CatalogItem> catalog = (List<CatalogItem>) resp.getData();
            if (catalog.isEmpty()) {
                ServerCall.showInfo(this, "The catalog is empty. Ask an admin to add items to catalog_items.");
                return;
            }
            JComboBox<CatalogItem> combo = new JComboBox<>(catalog.toArray(new CatalogItem[0]));
            JTextField noteField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
            panel.add(new JLabel("Choose an item:"));
            panel.add(combo);
            panel.add(new JLabel("Optional note (size, color, etc.):"));
            panel.add(noteField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Add to my Wish List",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                CatalogItem chosen = (CatalogItem) combo.getSelectedItem();
                Request req = new Request(Actions.ADD_WISHLIST_ITEM)
                        .put("catalogItemId", chosen.getId())
                        .put("note", noteField.getText().trim());
                ServerCall.run(this, req, r2 -> {
                    if (r2.isSuccess()) refresh(); else ServerCall.showError(this, r2.getMessage());
                });
            }
        });
    }

    private void editSelectedNote() {
        WishlistItem wi = getSelected();
        if (wi == null) {
            ServerCall.showInfo(this, "Select an item first.");
            return;
        }
        String newNote = JOptionPane.showInputDialog(this, "Note for \"" + wi.getItemName() + "\":", wi.getNote());
        if (newNote != null) {
            Request req = new Request(Actions.UPDATE_WISHLIST_ITEM_NOTE)
                    .put("wishlistItemId", wi.getId())
                    .put("note", newNote.trim());
            ServerCall.run(this, req, resp -> {
                if (resp.isSuccess()) refresh(); else ServerCall.showError(this, resp.getMessage());
            });
        }
    }

    private void deleteSelected() {
        WishlistItem wi = getSelected();
        if (wi == null) {
            ServerCall.showInfo(this, "Select an item first.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove \"" + wi.getItemName() + "\" from your wish list?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Request req = new Request(Actions.DELETE_WISHLIST_ITEM).put("wishlistItemId", wi.getId());
            ServerCall.run(this, req, resp -> {
                if (resp.isSuccess()) refresh(); else ServerCall.showError(this, resp.getMessage());
            });
        }
    }

    private WishlistItem getSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= currentItems.size()) return null;
        return currentItems.get(row);
    }
}
