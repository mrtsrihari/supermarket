package supermarket.model;

import java.util.regex.Pattern;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String email;
    private boolean member;
    private int loyaltyPoints;

    public Customer(int customerId, String name, String phone, String email, boolean member) {
        this(customerId, name, phone, email, member, 0);
    }

    public Customer(int customerId, String name, String phone, String email, boolean member, int loyaltyPoints) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.member = member;
        this.loyaltyPoints = loyaltyPoints;
    }

    public Customer(String name, String phone, String email, boolean member) {
        this(0, name, phone, email, member, 0);
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isMember() { return member; }
    public void setMember(boolean member) { this.member = member; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
    public void addLoyaltyPoints(int points) { if (points > 0) this.loyaltyPoints += points; }

    public String getMembershipTier() {
        if (loyaltyPoints >= 500) return "Platinum (10% Tier Discount)";
        if (loyaltyPoints >= 200) return "Gold (7% Tier Discount)";
        if (member) return "Silver (5% Member Discount)";
        return "Standard";
    }

    public double getMemberDiscountRate() {
        if (loyaltyPoints >= 500) return 0.10;
        if (loyaltyPoints >= 200) return 0.07;
        if (member) return 0.05;
        return 0.0;
    }

    public boolean isValidPhone() {
        return phone != null && Pattern.matches("[6-9][0-9]{9}", phone);
    }

    public boolean isValidEmail() {
        return email != null && Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    @Override
    public String toString() {
        return String.format("%-6d | %-16s | %-12s | %-24s | Tier: %-10s | Points: %d",
                customerId, name, phone, email, getMembershipTier(), loyaltyPoints);
    }
}
