package com.github.gavvydizzle.playerislands.island;

public enum MemberType {
    MEMBER(1, "Member", "member"),
    OFFICER(2, "Officer", "officer"),
    CO_OWNER(3, "Co-owner", "co-owner"),
    OWNER(4, "Owner", "owner");

    public final int weight;
    public final String uppercaseName, lowercaseName;

    MemberType(int weight, String uppercaseName, String lowercaseName) {
        this.weight = weight;
        this.uppercaseName = uppercaseName;
        this.lowercaseName = lowercaseName;
    }


    public static MemberType getTypeByWeight(int weight) {
        for (MemberType memberType : MemberType.values()) {
            if (memberType.weight == weight) return memberType;
        }
        return null;
    }

    public static int getLowestWeight() {
        int min = OWNER.weight;
        for (MemberType memberType : MemberType.values()) {
            if (min > memberType.weight) min = memberType.weight;
        }
        return min;
    }

    public static MemberType getLowerRank(MemberType memberType) {
        MemberType lower = getTypeByWeight(memberType.weight-1);
        if (lower == null) return memberType;
        return lower;
    }

    public static MemberType getHigherRank(MemberType memberType) {
        MemberType higher = getTypeByWeight(memberType.weight+1);
        if (higher == null || memberType.weight >= MemberType.OWNER.weight) return memberType;
        return higher;
    }
}
