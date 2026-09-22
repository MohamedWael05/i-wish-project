package com.iwish.common;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class Contribution implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int wishlistItemId;
    private int contributorId;
    private String contributorUsername;
    private BigDecimal amount;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getWishlistItemId() { return wishlistItemId; }
    public void setWishlistItemId(int wishlistItemId) { this.wishlistItemId = wishlistItemId; }

    public int getContributorId() { return contributorId; }
    public void setContributorId(int contributorId) { this.contributorId = contributorId; }

    public String getContributorUsername() { return contributorUsername; }
    public void setContributorUsername(String contributorUsername) { this.contributorUsername = contributorUsername; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
