package com.technofuturtic.tournament_api.dl.enums;

import lombok.Getter;

@Getter
public enum RoundType {
    ROUND_OF_128(128),
    ROUND_OF_64(64),
    ROUND_OF_32(32),
    ROUND_OF_16(16),
    QUARTER_FINAL(8),
    SEMI_FINAL(4),
    FINAL(2);

    private final int bracketSize;

    RoundType(int bracketSize) {
        this.bracketSize = bracketSize;
    }

}

