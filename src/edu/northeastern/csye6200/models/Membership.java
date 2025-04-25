package edu.northeastern.csye6200.models;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Membership class for storing member information and subscription details
 */
public class Membership {
    private String membershipId;
    private User user;
    private MembershipTier tier;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private double discountRate; // Percentage discount on parking fees
    
    public enum MembershipTier {
        BASIC("Basic", 5.0), 
        SILVER("Silver", 10.0), 
        GOLD("Gold", 15.0), 
        PLATINUM("Platinum", 20.0);
        
        private final String name;
        private final double discountRate;
        
        MembershipTier(String name, double discountRate) {
            this.name = name;
            this.discountRate = discountRate;
        }
        
        public String getName() {
            return name;
        }
        
        public double getDiscountRate() {
            return discountRate;
        }
    }
    
    public Membership(User user, MembershipTier tier, LocalDate startDate, LocalDate endDate) {
        this.membershipId = generateMembershipId();
        this.user = user;
        this.tier = tier;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = true;
        this.discountRate = tier.getDiscountRate();
    }
    
    private String generateMembershipId() {
        return "MEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return active && !today.isBefore(startDate) && !today.isAfter(endDate);
    }
    
    public void extend(int months) {
        this.endDate = this.endDate.plusMonths(months);
    }
    
    public void upgrade(MembershipTier newTier) {
        this.tier = newTier;
        this.discountRate = newTier.getDiscountRate();
    }
    
    // Getters
    public String getMembershipId() {
        return membershipId;
    }
    
    public User getUser() {
        return user;
    }
    
    public MembershipTier getTier() {
        return tier;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public double getDiscountRate() {
        return discountRate;
    }
    
    // Setters
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "Membership[" + membershipId + "] " + user.getFullName() + " - " + 
               tier.getName() + " (" + startDate + " to " + endDate + ")";
    }
}