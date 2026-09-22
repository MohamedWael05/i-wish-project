package com.iwish.client;

import com.iwish.common.Actions;
import com.iwish.common.Request;
import com.iwish.common.WishlistItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;

public class ContributeDialog extends JDialog {

    public interface OnDone { void run(); }

    public ContributeDialog(Window owner, WishlistItem item, OnDone onDone) {
        super(owner, "Contribute", ModalityType.APPLICATION_MODAL);
        setSize(360, 260);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        root.setBackground(UiTheme.BACKGROUND);
        setContentPane(root);

        JLabel title = new JLabel("\uD83C\uDF81 " + item.getItemName());
        title.setFont(UiTheme.FONT_HEADER);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel info = new JLabel("<html>Price: $" + item.getPrice() + "<br>Already funded: $"
                + item.getFundedAmount() + "<br>Remaining: $" + item.getRemaining() + "</html>");
        info.setFont(UiTheme.FONT_BODY);
        info.setForeground(UiTheme.TEXT_MUTED);
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.setBorder(new EmptyBorder(8, 0, 16, 0));

        JLabel amountLabel = new JLabel("Amount you want to contribute ($):");
        amountLabel.setFont(UiTheme.FONT_SMALL);
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField amountField = new JTextField(item.getRemaining().toPlainString());
        amountField.setFont(UiTheme.FONT_BODY);
        amountField.setMaximumSize(new Dimension(2000, 32));
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDCD6F7), 1, true), new EmptyBorder(4, 10, 4, 10)));

        JLabel statusLabel = new JLabel(" ");
        statusLabel.setForeground(UiTheme.WARN);
        statusLabel.setFont(UiTheme.FONT_SMALL);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        RoundedButton confirmBtn = new RoundedButton("Contribute \uD83D\uDC9D", UiTheme.PRIMARY, Color.WHITE);
        confirmBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmBtn.addActionListener(e -> {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                statusLabel.setText("Please enter a valid amount.");
                return;
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                statusLabel.setText("Amount must be positive.");
                return;
            }
            Request req = new Request(Actions.CONTRIBUTE)
                    .put("wishlistItemId", item.getId())
                    .put("amount", amount.toPlainString());
            confirmBtn.setEnabled(false);
            ServerCall.run(this, req, resp -> {
                confirmBtn.setEnabled(true);
                if (resp.isSuccess()) {
                    ServerCall.showInfo(this, resp.getMessage());
                    if (onDone != null) onDone.run();
                    dispose();
                } else {
                    statusLabel.setText(resp.getMessage());
                }
            });
        });

        root.add(title);
        root.add(info);
        root.add(amountLabel);
        root.add(Box.createVerticalStrut(4));
        root.add(amountField);
        root.add(Box.createVerticalStrut(12));
        root.add(confirmBtn);
        root.add(Box.createVerticalStrut(8));
        root.add(statusLabel);
    }
}
