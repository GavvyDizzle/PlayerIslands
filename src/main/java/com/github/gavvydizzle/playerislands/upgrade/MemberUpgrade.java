package com.github.gavvydizzle.playerislands.upgrade;

import com.github.mittenmc.serverutils.Numbers;

public class MemberUpgrade extends Upgrade {

    private final int maxMembers;

    public MemberUpgrade(long price, int level, String permission, int maxMembers) {
        super(price, level, permission);
        this.maxMembers = maxMembers;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    @Override
    public String toString() {
        return "MemberUpgrade " + getUpgradeLevel() + "\n" +
                "Price: $" + Numbers.withSuffix(getPrice()) + "\n" +
                "Member Size: " + maxMembers;
    }
}
