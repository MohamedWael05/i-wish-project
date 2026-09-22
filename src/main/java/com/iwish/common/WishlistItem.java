package com.iwish.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class WishlistItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private int id;
    private int ownerId;
    private String ownerUsername;
    private int catalogItemId;
    private String itemName;
    private BigDecimal price;
    private String note;
    private BigDecimal fundedAmount;
    private String status;
    private Timestamp createdAt;

    public WishlistItem() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public int getCatalogItemId() { return catalogItemId; }
    public void setCatalogItemId(int catalogItemId) { this.catalogItemId = catalogItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public BigDecimal getFundedAmount() { return fundedAmount; }
    public void setFundedAmount(BigDecimal fundedAmount) { this.fundedAmount = fundedAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public BigDecimal getRemaining() {
        BigDecimal funded = fundedAmount == null ? BigDecimal.ZERO : fundedAmount;
        BigDecimal rem = price.subtract(funded);
        return rem.max(BigDecimal.ZERO);
    }
}
