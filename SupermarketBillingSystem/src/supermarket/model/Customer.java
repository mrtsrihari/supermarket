package supermarket.model;

import java.util.regex.Pattern;

public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String email;
    private boolean member;

    public Customer(int customerId, String name, String phone, String email, boolean member) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.member = member;
    }

    public Customer(String name, String phone, String email, boolean member) {
        this(0, name, phone, email, member);
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

    public boolean isValidPhone() {
        return phone != null && Pattern.matches("[6-9][0-9]{9}", phone);
    }

    public boolean isValidEmail() {
        return email != null && Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email);
    }

    @Override
    public String toString() {
        return customerId + " | " + name + " | " + phone + " | " + email + " | Member: " + member;
    }
}
